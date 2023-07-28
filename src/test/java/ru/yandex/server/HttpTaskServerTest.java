package ru.yandex.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.adapter.DurationAdapter;
import ru.yandex.adapter.LocalDateAdapter;
import ru.yandex.model.Epic;
import ru.yandex.model.Status;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {

    private static final String API_URL = "http://localhost:8080";
    private static final String KV_SERVER_URL = "http://localhost:8078";
    private static final LocalDateTime START_TIME_1
            = LocalDateTime.of(2023, 7, 17, 10, 0);
    private static final LocalDateTime START_TIME_2
            = LocalDateTime.of(2023, 7, 17, 11, 0);
    private static final LocalDateTime START_TIME_3
            = LocalDateTime.of(2023, 7, 17, 12, 0);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter().nullSafe())
            .registerTypeAdapter(Duration.class, new DurationAdapter().nullSafe())
            .create();
    private KVServer kvServer;
    private HttpTaskServer httpTaskServer;
    private HttpClient client;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void initializeTasks() {
        task = new Task(START_TIME_1, 20)
                .setId(0)
                .setName("Task")
                .setStatus(Status.NEW)
                .setDescription("Task description");
        epic = (Epic) new Epic(START_TIME_2, 20)
                .setId(1)
                .setName("Epic")
                .setStatus(Status.NEW)
                .setDescription("Epic description");
        subtask = (Subtask) new Subtask(START_TIME_3, 30)
                .setEpicId(1)
                .setId(2)
                .setName("Subtask")
                .setStatus(Status.NEW)
                .setDescription("Subtask description");
    }

    @BeforeEach
    void initializeAndStartServers() throws IOException, InterruptedException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer(KV_SERVER_URL);
        httpTaskServer.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void stopServers() {
        httpTaskServer.stop();
        kvServer.stop();
    }

    @Test
    void shouldGetTasks() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/task/");
        String json = GSON.toJson(task);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        HttpResponse<String> getResponse = makeGetRequest(uri);
        String expectedJsonTaskArray = GSON.toJson(List.of(task));
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(expectedJsonTaskArray, getResponse.body())
        );
    }

    @Test
    void shouldGetZeroTasksFromEmptyDatabase() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/task/");
        HttpResponse<String> response = makeGetRequest(uri);
        assertAll(
                () -> assertEquals(200, response.statusCode()),
                () -> assertEquals("[]", response.body())
        );
    }

    @Test
    void shouldGetTaskWithCorrectId() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/task/");
        String json = GSON.toJson(task);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/task/?id=0");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(json, getResponse.body())
        );
    }

    @Test
    void shouldNotGetTaskWithIncorrectId() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/task/");
        String json = GSON.toJson(task);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/task/?id=" + Integer.MAX_VALUE);
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertEquals(400, getResponse.statusCode());
    }

    @Test
    void shouldNotGetTaskWithIncorrectQuery() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/task/");
        String json = GSON.toJson(task);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/task/?id=");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertEquals(400, getResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/task/?id=query");
        HttpResponse<String> getResponseWithAnotherWrongQuery = makeGetRequest(uri);
        assertEquals(400, getResponseWithAnotherWrongQuery.statusCode());
    }

    @Test
    void shouldAddTask() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/task/");
        String json = GSON.toJson(task);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());
    }

    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/task/");
        String json = GSON.toJson(task);
        HttpResponse<String> postResponseForAdd = makePostRequest(uri, json);
        assertEquals(201, postResponseForAdd.statusCode());

        task = task.setName("New task").setStatus(Status.DONE);
        json = GSON.toJson(task);
        HttpResponse<String> postResponseForUpdate = makePostRequest(uri, json);
        assertEquals(200, postResponseForUpdate.statusCode());

        uri = URI.create(API_URL + "/tasks/task/?id=0");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        String expectedJson = json;
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(expectedJson, getResponse.body())
        );
    }

    @Test
    void shouldDeleteTask() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/task/");
        String json = GSON.toJson(task);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/task/?id=0");
        HttpResponse<String> deleteResponse = makeDeleteRequest(uri);
        assertEquals(200, deleteResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/task/?id=0");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertEquals(400, getResponse.statusCode());
    }

    @Test
    void shouldDeleteAllTasks() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/task/");
        String json = GSON.toJson(task);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        task.setId(1);
        json = GSON.toJson(task);
        postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/task/");
        HttpResponse<String> deleteResponse = makeDeleteRequest(uri);
        assertEquals(200, deleteResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/task/");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals("[]", getResponse.body())
        );
    }

    @Test
    void shouldGetEpics() throws IOException, InterruptedException {
        epic.setId(0);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        HttpResponse<String> getResponse = makeGetRequest(uri);
        String expectedJsonEpicArray = GSON.toJson(List.of(epic));
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(expectedJsonEpicArray, getResponse.body())
        );
    }

    @Test
    void shouldGetZeroEpicsFromEmptyDatabase() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/epic/");
        HttpResponse<String> response = makeGetRequest(uri);
        assertAll(
                () -> assertEquals(200, response.statusCode()),
                () -> assertEquals("[]", response.body())
        );
    }

    @Test
    void shouldGetEpicWithCorrectId() throws IOException, InterruptedException {
        epic.setId(0);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/?id=0");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(json, getResponse.body())
        );
    }

    @Test
    void shouldNotGetEpicWithIncorrectId() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/?id=" + Integer.MAX_VALUE);
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertEquals(400, getResponse.statusCode());
    }

    @Test
    void shouldNotGetEpicWithIncorrectQuery() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/?id=");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertEquals(400, getResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/?id==query");
        HttpResponse<String> getResponseWithAnotherWrongQuery = makeGetRequest(uri);
        assertEquals(400, getResponseWithAnotherWrongQuery.statusCode());
    }

    @Test
    void shouldAddEpic() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());
    }

    @Test
    void shouldUpdateEpic() throws IOException, InterruptedException {
        epic.setId(0);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponseForAdd = makePostRequest(uri, json);
        assertEquals(201, postResponseForAdd.statusCode());

        epic = (Epic) epic.setName("New epic");
        json = GSON.toJson(epic);
        HttpResponse<String> postResponseForUpdate = makePostRequest(uri, json);
        assertEquals(200, postResponseForUpdate.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/?id=0");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        String expectedJson = json;
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(expectedJson, getResponse.body())
        );
    }

    @Test
    void shouldDeleteEpic() throws IOException, InterruptedException {
        epic.setId(0);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/?id=0");
        HttpResponse<String> deleteResponse = makeDeleteRequest(uri);
        assertEquals(200, deleteResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/?id=0");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertEquals(400, getResponse.statusCode());
    }

    @Test
    void shouldDeleteAllEpics() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        epic.setId(1);
        json = GSON.toJson(epic);
        postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/");
        HttpResponse<String> deleteResponse = makeDeleteRequest(uri);
        assertEquals(200, deleteResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals("[]", getResponse.body())
        );
    }

    @Test
    void shouldGetSubtasks() throws IOException, InterruptedException {
        epic.setId(0);
        subtask.setEpicId(0).setId(1);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSubtask.statusCode());

        HttpResponse<String> getResponse = makeGetRequest(uri);
        String expectedJsonSubtaskArray = GSON.toJson(List.of(subtask));
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(expectedJsonSubtaskArray, getResponse.body())
        );
    }

    @Test
    void shouldGetZeroSubtasksFromEmptyDatabase() throws IOException, InterruptedException {
        URI uri = URI.create(API_URL + "/tasks/subtask/");
        HttpResponse<String> response = makeGetRequest(uri);
        assertAll(
                () -> assertEquals(200, response.statusCode()),
                () -> assertEquals("[]", response.body())
        );
    }

    @Test
    void shouldGetSubtaskWithCorrectIdAndEpicId() throws IOException, InterruptedException {
        epic.setId(0);
        subtask.setEpicId(0).setId(1);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSubtask.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/?id=1");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        String expectedJsonSubtask = GSON.toJson(subtask);
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(expectedJsonSubtask, getResponse.body())
        );
    }

    @Test
    void shouldNotGetSubtaskWithIncorrectId() throws IOException, InterruptedException {
        epic.setId(0);
        subtask.setEpicId(0).setId(1);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSubtask.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/?id=" + Integer.MAX_VALUE);
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertEquals(400, getResponse.statusCode());
    }

    @Test
    void shouldNotGetSubtaskWithIncorrectQuery() throws IOException, InterruptedException {
        epic.setId(0);
        subtask.setEpicId(0).setId(1);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSubtask.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/?id==asd");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertEquals(400, getResponse.statusCode());
    }

    @Test
    void shouldAddSubtask() throws IOException, InterruptedException {
        epic.setId(0);
        subtask.setEpicId(0).setId(1);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSubtask.statusCode());
    }

    @Test
    void shouldUpdateSubtask() throws IOException, InterruptedException {
        epic.setId(0);
        subtask.setEpicId(0).setId(1);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSubtask.statusCode());

        subtask = (Subtask) subtask.setName("New subtask");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForUpdate = makePostRequest(uri, json);
        assertEquals(200, postResponseForUpdate.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/?id=1");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        String expectedJson = json;
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(expectedJson, getResponse.body())
        );
    }

    @Test
    void shouldDeleteSubtask() throws IOException, InterruptedException {
        epic.setId(0);
        subtask.setEpicId(0).setId(1);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponse = makePostRequest(uri, json);
        assertEquals(201, postResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSubtask.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/?id=1");
        HttpResponse<String> deleteResponse = makeDeleteRequest(uri);
        assertEquals(200, deleteResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/?id=1");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertEquals(400, getResponse.statusCode());
    }

    @Test
    void shouldDeleteAllSubtasks() throws IOException, InterruptedException {
        epic.setId(0);
        subtask.setEpicId(0).setId(1);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponseForEpic = makePostRequest(uri, json);
        assertEquals(201, postResponseForEpic.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForFirstSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForFirstSubtask.statusCode());

        subtask.setId(2);
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSecondSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSecondSubtask.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        HttpResponse<String> deleteResponse = makeDeleteRequest(uri);
        assertEquals(200, deleteResponse.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals("[]", getResponse.body())
        );
    }

    @Test
    void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        epic.setId(0);
        subtask.setEpicId(0).setId(1);
        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponseForEpic = makePostRequest(uri, json);
        assertEquals(201, postResponseForEpic.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForFirstSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForFirstSubtask.statusCode());

        subtask.setId(2);
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSecondSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSecondSubtask.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/?id=0");
        HttpResponse<String> getResponseForEpic = makeGetRequest(uri);
        assertEquals(200, getResponseForEpic.statusCode());
        Epic newEpic = GSON.fromJson(getResponseForEpic.body(), Epic.class);

        uri = URI.create(API_URL + "/tasks/epic/subtask/?id=0");
        HttpResponse<String> getResponseForEpicSubtasks = makeGetRequest(uri);
        assertEquals(200, getResponseForEpicSubtasks.statusCode());

        String expectedJsonSubtasks = GSON.toJson(newEpic.getSubtasks());
        assertAll(
                () -> assertEquals(200, getResponseForEpicSubtasks.statusCode()),
                () -> assertEquals(expectedJsonSubtasks, getResponseForEpicSubtasks.body())
        );
    }

    @Test
    void shouldGetHistory() throws IOException, InterruptedException {
        epic.setId(0);
        task.setId(1);
        subtask.setId(2);
        subtask.setEpicId(0);
        epic.addSubtask(subtask);
        String expectedJsonHistory = GSON.toJson(List.of(epic, task, subtask));

        URI uri = URI.create(API_URL + "/tasks/epic/");
        String json = GSON.toJson(epic);
        HttpResponse<String> postResponseForEpic = makePostRequest(uri, json);
        assertEquals(201, postResponseForEpic.statusCode());

        uri = URI.create(API_URL + "/tasks/task/");
        json = GSON.toJson(task);
        HttpResponse<String> postResponseForTask = makePostRequest(uri, json);
        assertEquals(201, postResponseForTask.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSubtask.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/?id=0");
        HttpResponse<String> getResponseForEpic = makeGetRequest(uri);
        assertEquals(200, getResponseForEpic.statusCode());

        uri = URI.create(API_URL + "/tasks/task/?id=1");
        HttpResponse<String> getResponseForTask = makeGetRequest(uri);
        assertEquals(200, getResponseForTask.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/?id=2");
        HttpResponse<String> getResponseForSubtask = makeGetRequest(uri);
        assertEquals(200, getResponseForSubtask.statusCode());

        uri = URI.create(API_URL + "/tasks/history/");
        HttpResponse<String> getResponse = makeGetRequest(uri);

        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(expectedJsonHistory, getResponse.body())
        );
    }

    @Test
    void shouldGetPrioritizedTasks() throws IOException, InterruptedException {
        String expectedJsonPrioritizedTasks = GSON.toJson(List.of(task, subtask));

        URI uri = URI.create(API_URL + "/tasks/task/");
        String json = GSON.toJson(task);
        HttpResponse<String> postResponseForTask = makePostRequest(uri, json);
        assertEquals(201, postResponseForTask.statusCode());

        uri = URI.create(API_URL + "/tasks/epic/");
        json = GSON.toJson(epic);
        HttpResponse<String> postResponseForEpic = makePostRequest(uri, json);
        assertEquals(201, postResponseForEpic.statusCode());

        uri = URI.create(API_URL + "/tasks/subtask/");
        json = GSON.toJson(subtask);
        HttpResponse<String> postResponseForSubtask = makePostRequest(uri, json);
        assertEquals(201, postResponseForSubtask.statusCode());

        uri = URI.create(API_URL + "/tasks/");
        HttpResponse<String> getResponse = makeGetRequest(uri);
        assertAll(
                () -> assertEquals(200, getResponse.statusCode()),
                () -> assertEquals(expectedJsonPrioritizedTasks, getResponse.body())
        );
    }

    private HttpResponse<String> makeGetRequest(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        return client.send(request, handler);
    }

    private HttpResponse<String> makePostRequest(URI uri, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        return client.send(request, handler);
    }

    private HttpResponse<String> makeDeleteRequest(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        return client.send(request, handler);
    }
}