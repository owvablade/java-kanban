package ru.yandex.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.server.KVServer;
import ru.yandex.util.Managers;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    private static final String URL = "http://localhost:8078";
    private static KVServer server;

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        server = new KVServer();
        server.start();
        manager = Managers.getDefault(URL);
    }

    @AfterEach
    void afterEach(){
        server.stop();
    }

    @Test
    void shouldLoadTasksWithHistoryFromServer() throws IOException, InterruptedException {
        List<Task> expectedListOfTasks = List.of(task);
        List<Epic> expectedListOfEpics = List.of(epic);
        List<Subtask> expectedListOfSubtasks = List.of(subtask);
        List<Task> expectedHistory = List.of(task, epic, subtask);
        manager.addTask(task);
        manager.addEpic(epic);
        manager.addSubtask(subtask);
        manager.getTask(task.getId());
        manager.getEpic(epic.getId());
        manager.getSubtask(subtask.getId());
        manager = HttpTaskManager.loadFromServer(URL);
        assertAll(
                () -> assertEquals(expectedListOfTasks, manager.getAllTasks()),
                () -> assertEquals(expectedListOfEpics, manager.getAllEpics()),
                () -> assertEquals(expectedListOfSubtasks, manager.getAllSubtasks()),
                () -> assertEquals(expectedHistory, manager.getHistory())
        );
    }

    @Test
    void shouldLoadTasksWithoutHistoryFromServer() throws IOException, InterruptedException {
        List<Task> expectedListOfTasks = List.of(task);
        List<Epic> expectedListOfEpics = List.of(epic);
        List<Subtask> expectedListOfSubtasks = List.of(subtask);
        List<Task> expectedHistory = List.of();
        manager.addTask(task);
        manager.addEpic(epic);
        manager.addSubtask(subtask);
        manager = HttpTaskManager.loadFromServer(URL);
        assertAll(
                () -> assertEquals(expectedListOfTasks, manager.getAllTasks()),
                () -> assertEquals(expectedListOfEpics, manager.getAllEpics()),
                () -> assertEquals(expectedListOfSubtasks, manager.getAllSubtasks()),
                () -> assertEquals(expectedHistory, manager.getHistory())
        );
    }

    @Test
    void shouldLoadEmptyServer() throws IOException, InterruptedException {
        List<Task> expectedListOfTasks = List.of();
        List<Epic> expectedListOfEpics = List.of();
        List<Subtask> expectedListOfSubtasks = List.of();
        List<Task> expectedHistory = List.of();
        manager = HttpTaskManager.loadFromServer(URL);
        assertAll(
                () -> assertEquals(expectedListOfTasks, manager.getAllTasks()),
                () -> assertEquals(expectedListOfEpics, manager.getAllEpics()),
                () -> assertEquals(expectedListOfSubtasks, manager.getAllSubtasks()),
                () -> assertEquals(expectedHistory, manager.getHistory())
        );
    }
}