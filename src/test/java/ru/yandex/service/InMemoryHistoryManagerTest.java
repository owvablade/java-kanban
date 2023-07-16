package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Status;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.service.interfaces.HistoryManager;
import ru.yandex.util.Managers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static Task task;
    private static Epic epic;
    private static Subtask subtask;
    private static HistoryManager history;

    @BeforeEach
    void beforeEach() {
        history = Managers.getDefaultHistory();
        task = new Task()
                .setId(0)
                .setName("Task")
                .setStatus(Status.NEW)
                .setDescription("Task description");
        epic = (Epic) new Epic().setId(1)
                .setName("Epic")
                .setStatus(Status.NEW)
                .setDescription("Epic description");
        subtask = (Subtask) new Subtask()
                .setEpicId(1)
                .setId(2)
                .setName("Subtask")
                .setStatus(Status.NEW)
                .setDescription("Subtask description");
    }

    @Test
    void shouldAddAllTasks() {
        List<Integer> expectedHistoryIds = List.of(0, 1, 2);
        history.add(task);
        history.add(epic);
        history.add(subtask);
        List<Integer> actualHistoryIds = history.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        assertEquals(expectedHistoryIds, actualHistoryIds);
    }

    @Test
    void shouldNotAddDuplicateTasks() {
        List<Integer> expectedHistoryIds = List.of(2, 1, 0);
        history.add(task);
        history.add(epic);
        history.add(subtask);
        history.add(subtask);
        history.add(epic);
        history.add(task);
        List<Integer> actualHistoryIds = history.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        assertEquals(expectedHistoryIds, actualHistoryIds);
    }

    @Test
    void shouldRemoveAllTasks() {
        List<Integer> expectedHistoryIds = Collections.emptyList();
        history.add(task);
        history.add(epic);
        history.add(subtask);
        history.remove(task.getId());
        history.remove(epic.getId());
        history.remove(subtask.getId());
        List<Integer> actualHistoryIds = history.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        assertEquals(expectedHistoryIds, actualHistoryIds);
    }

    @Test
    void shouldRemoveTasksFromBeginning() {
        List<Integer> expectedHistoryIds = List.of(1, 2);
        history.add(task);
        history.add(epic);
        history.add(subtask);
        history.remove(task.getId());
        List<Integer> actualHistoryIds = history.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        assertEquals(expectedHistoryIds, actualHistoryIds);
    }

    @Test
    void shouldRemoveTasksFromMiddle() {
        List<Integer> expectedHistoryIds = List.of(0, 2);
        history.add(task);
        history.add(epic);
        history.add(subtask);
        history.remove(epic.getId());
        List<Integer> actualHistoryIds = history.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        assertEquals(expectedHistoryIds, actualHistoryIds);
    }

    @Test
    void shouldRemoveTasksFromEnd() {
        List<Integer> expectedHistoryIds = List.of(0, 1);
        history.add(task);
        history.add(epic);
        history.add(subtask);
        history.remove(subtask.getId());
        List<Integer> actualHistoryIds = history.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        assertEquals(expectedHistoryIds, actualHistoryIds);
    }

    @Test
    void shouldGetEmptyHistory() {
        List<Integer> expectedHistoryIds = Collections.emptyList();
        List<Integer> actualHistoryIds = history.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        assertEquals(expectedHistoryIds, actualHistoryIds);
    }
}