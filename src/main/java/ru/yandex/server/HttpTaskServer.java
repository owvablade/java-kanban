package ru.yandex.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.model.Status;
import ru.yandex.model.*;
import ru.yandex.server.handlers.EpicHandler;
import ru.yandex.server.handlers.SubtaskHandler;
import ru.yandex.server.handlers.TaskHandler;
import ru.yandex.server.handlers.util.ServerUtil;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.util.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.List;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static final String URL = "http://localhost:8078";
    private final Gson gson;
    private final HttpServer server;
    private final TaskManager manager;

    public HttpTaskServer() throws IOException, InterruptedException {
        gson = new GsonBuilder().create();
        manager = Managers.getDefault(URL);
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", this::handlePrioritizedTasks);
        server.createContext("/tasks/task", new TaskHandler(manager, gson));
        server.createContext("/tasks/epic", new EpicHandler(manager, gson));
        server.createContext("/tasks/subtask", new SubtaskHandler(manager, gson));
        server.createContext("/tasks/history", this::handleHistory);
    }

    private void handlePrioritizedTasks(HttpExchange exchange) throws IOException {
        String response;
        int responseCode;
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Task> tasks = manager.getPrioritizedTasks();
            response = gson.toJson(tasks);
            responseCode = 200;
        } else {
            response = "Ожидался GET запрос для получения всех задач";
            responseCode = 400;
        }
        ServerUtil.writeResponse(exchange, response, responseCode);
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        String response;
        int responseCode;
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Task> tasks = manager.getHistory();
            response = gson.toJson(tasks);
            responseCode = 200;
        } else {
            response = "Ожидался GET запрос для получения истории";
            responseCode = 400;
        }
        ServerUtil.writeResponse(exchange, response, responseCode);
    }

    public void start() {
        final LocalDateTime START_TIME_1
                = LocalDateTime.of(2023, 7, 17, 10, 0);
        final LocalDateTime START_TIME_2
                = LocalDateTime.of(2023, 7, 17, 12, 0);
        final LocalDateTime START_TIME_3
                = LocalDateTime.of(2023, 7, 17, 13, 0);
        Task task1 = new Task(START_TIME_1, 20)
                .setId(0)
                .setName("Task")
                .setStatus(Status.NEW)
                .setDescription("Task description");
        manager.addTask(task1);
        Epic epic1 = (Epic) new Epic(START_TIME_2, 30)
                .setId(-123)
                .setName("Epic")
                .setStatus(Status.NEW)
                .setDescription("Epic description");
        manager.addEpic(epic1);
        Subtask epic1subtask1 = (Subtask) new Subtask(START_TIME_3, 40)
                .setId(-123)
                .setName("Subtask")
                .setStatus(Status.NEW)
                .setDescription("Subtask description");
        epic1subtask1.setEpicId(epic1.getId());
        manager.addSubtask(epic1subtask1);
        server.start();
    }
}
