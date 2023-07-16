package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import ru.yandex.model.Epic;
import ru.yandex.model.Status;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.service.interfaces.TaskManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;
    protected Task task;
    protected Epic epic;
    protected Subtask subtask;

    @BeforeEach
    public void initializeTasks() {
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

    void addTask() {
        manager.addTask(task);
        final int expectedSize = 1;
        assertEquals(expectedSize, manager.getAllTasks().size());
    }

    void addNullTask() {
        manager.addTask(null);
        final int expectedSize = 0;
        assertEquals(expectedSize, manager.getAllTasks().size());
    }

    void addTaskWithIncorrectId() {
        task.setId(Integer.MIN_VALUE);
        manager.addTask(task);
        final int expectedSize = 1;
        assertEquals(expectedSize, manager.getAllTasks().size());
    }

    void addEpicWithNoSubtasks() {
        manager.addEpic(epic);
        final int expectedSize = 1;
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllEpics().size()),
                () -> assertEquals(Status.NEW, epic.getStatus())
        );
    }

    void addEpicWithNewSubtask() {
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId()).setStatus(Status.NEW);
        manager.addSubtask(subtask);
        final int expectedSize = 1;
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllEpics().size()),
                () -> assertEquals(Status.NEW, epic.getStatus())
        );
    }

    void addEpicWithDoneSubtask() {
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId()).setStatus(Status.DONE);
        manager.addSubtask(subtask);
        final int expectedSize = 1;
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllEpics().size()),
                () -> assertEquals(Status.DONE, epic.getStatus())
        );
    }

    void addEpicWithInProgressSubtask() {
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId()).setStatus(Status.IN_PROGRESS);
        manager.addSubtask(subtask);
        final int expectedSize = 1;
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllEpics().size()),
                () -> assertEquals(Status.IN_PROGRESS, epic.getStatus())
        );
    }

    void addNullEpic() {
        manager.addEpic(null);
        final int expectedSize = 0;
        assertEquals(expectedSize, manager.getAllEpics().size());
    }

    void addEpicWithIncorrectId() {
        epic.setId(Integer.MIN_VALUE);
        manager.addEpic(epic);
        final int expectedSize = 1;
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllEpics().size()),
                () -> assertEquals(Status.NEW, epic.getStatus())
        );
    }

    void addSubtaskWithEpicId() {
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        final int expectedSize = 1;
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllSubtasks().size()),
                () -> assertEquals(epic.getId(), subtask.getEpicId())
        );
    }

    void addNullSubtask() {
        manager.addSubtask(null);
        final int expectedSize = 0;
        assertEquals(expectedSize, manager.getAllSubtasks().size());
    }

    void addSubtaskWithIncorrectEpicId() {
        manager.addEpic(epic);
        subtask.setEpicId(Integer.MIN_VALUE);
        manager.addSubtask(subtask);
        final int expectedSize = 0;
        assertEquals(expectedSize, manager.getAllSubtasks().size());
    }

    void getExistingTask() {
        manager.addTask(task);
        final Task expectedTask = task;
        assertEquals(expectedTask, manager.getTask(task.getId()));
    }

    void getNonExistingTask() {
        assertNull(manager.getTask(Integer.MIN_VALUE));
    }

    void getExistingEpic() {
        manager.addEpic(epic);
        final Epic expectedEpic = epic;
        assertEquals(expectedEpic, manager.getEpic(epic.getId()));
    }

    void getNonExistingEpic() {
        assertNull(manager.getEpic(Integer.MIN_VALUE));
    }

    void getExistingSubtask() {
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        final Subtask expectedSubtask = subtask;
        assertEquals(expectedSubtask, manager.getSubtask(subtask.getId()));
    }

    void getNonExistingSubtask() {
        assertNull(manager.getSubtask(Integer.MIN_VALUE));
    }

    void updateTaskWithSameId() {
        final String expectedName = "New task";
        manager.addTask(task);
        Task newTask = new Task()
                .setId(task.getId())
                .setName(expectedName);
        manager.updateTask(newTask);
        final String actualName = manager.getTask(task.getId()).getName();
        assertEquals(expectedName, actualName);
    }

    void updateTaskWithDifferentId() {
        final String expectedName = "Task";
        manager.addTask(task);
        Task newTask = new Task()
                .setId(Integer.MIN_VALUE)
                .setName("New task");
        manager.updateTask(newTask);
        final String actualName = manager.getTask(task.getId()).getName();
        assertEquals(expectedName, actualName);
    }

    void updateEpicWithNoSubtask() {
        final String expectedName = "New epic";
        manager.addEpic(epic);
        Epic newEpic = (Epic) new Epic()
                .setId(task.getId())
                .setName(expectedName);
        manager.updateEpic(newEpic);
        final String actualName = manager.getEpic(epic.getId()).getName();
        assertEquals(expectedName, actualName);
    }

    void updateEpicWithSubtask() {
        final String expectedNameForEpic = "New epic";
        final String expectedNameForSubtask = "New subtask";
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        Epic newEpic = (Epic) new Epic()
                .setId(epic.getId())
                .setName(expectedNameForEpic);
        Subtask newSubtask = (Subtask) new Subtask()
                .setEpicId(newEpic.getId())
                .setId(subtask.getId())
                .setName("New subtask")
                .setStatus(Status.IN_PROGRESS);
        newEpic.addSubtask(newSubtask);
        manager.updateEpic(newEpic);
        final String actualNameForEpic = manager.getEpic(epic.getId()).getName();
        final String actualNameForSubtask = manager.getSubtask(subtask.getId()).getName();
        assertAll(
                () -> assertEquals(expectedNameForEpic, actualNameForEpic),
                () -> assertEquals(expectedNameForSubtask, actualNameForSubtask),
                () -> assertEquals(Status.IN_PROGRESS, epic.getStatus())
        );
    }

    void updateEpicWithWrongId() {
        manager.addEpic(epic);
        Epic newEpic = (Epic) new Epic()
                .setId(Integer.MIN_VALUE)
                .setName("New epic");
        manager.updateEpic(newEpic);
        final Epic actualEpic = manager.getEpic(newEpic.getId());
        assertNull(actualEpic);
    }

    void updateSubtaskWithEpicId() {
        final String expectedNameForSubtask = "New subtask";
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        Subtask newSubtask = (Subtask) new Subtask()
                .setEpicId(epic.getId())
                .setId(subtask.getId())
                .setName("New subtask")
                .setStatus(Status.DONE);
        manager.updateSubtask(newSubtask);
        final String actualNameForSubtask = manager.getSubtask(newSubtask.getId()).getName();
        assertAll(
                () -> assertEquals(expectedNameForSubtask, actualNameForSubtask),
                () -> assertEquals(Status.DONE, epic.getStatus())
        );
    }

    void updateSubtaskWithWrongEpicId() {
        final String expectedNameForSubtask = "Subtask";
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        Subtask newSubtask = (Subtask) new Subtask()
                .setEpicId(Integer.MIN_VALUE)
                .setId(subtask.getId())
                .setName("New subtask");
        manager.updateSubtask(newSubtask);
        final String actualNameForSubtask = manager.getSubtask(newSubtask.getId()).getName();
        assertEquals(expectedNameForSubtask, actualNameForSubtask);
    }

    void deleteTaskWithId() {
        final int expectedSize = 0;
        manager.addTask(task);
        manager.deleteTask(task.getId());
        final int actualSize = manager.getAllTasks().size();
        assertEquals(expectedSize, actualSize);
    }

    void deleteTaskWithWrongId() {
        final int expectedSize = 1;
        manager.addTask(task);
        manager.deleteTask(Integer.MIN_VALUE);
        final int actualSize = manager.getAllTasks().size();
        assertEquals(expectedSize, actualSize);
    }

    void deleteEpicWithIdAndNoSubtasks() {
        final int expectedSize = 0;
        manager.addEpic(epic);
        manager.deleteEpic(epic.getId());
        final int actualSize = manager.getAllEpics().size();
        assertEquals(expectedSize, actualSize);
    }

    void deleteEpicWithIdAndSubtasks() {
        final int expectedSizeForEpics = 0;
        final int expectedSizeForSubtasks = 0;
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        manager.deleteEpic(epic.getId());
        final int actualSizeForEpics = manager.getAllEpics().size();
        final int actualSizeForSubtasks = manager.getAllSubtasks().size();
        assertAll(
                () -> assertEquals(expectedSizeForEpics, actualSizeForEpics),
                () -> assertEquals(expectedSizeForSubtasks, actualSizeForSubtasks)
        );
    }

    void deleteEpicWithWrongId() {
        final int expectedSize = 1;
        manager.addEpic(epic);
        manager.deleteEpic(Integer.MIN_VALUE);
        final int actualSize = manager.getAllEpics().size();
        assertEquals(expectedSize, actualSize);
    }

    void deleteSubtaskWithIdAndEpicId() {
        final int expectedSize = 0;
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        manager.deleteSubtask(subtask.getId());
        final int actualSizeForEpic = epic.getSubtasks().size();
        final int actualSizeForManager = manager.getAllSubtasks().size();
        assertAll(
                () -> assertEquals(expectedSize, actualSizeForEpic),
                () -> assertEquals(expectedSize, actualSizeForManager)
        );
    }

    void deleteSubtaskWithWrongId() {
        final int expectedSize = 1;
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        manager.deleteSubtask(Integer.MIN_VALUE);
        final int actualSizeForEpic = epic.getSubtasks().size();
        final int actualSizeForManager = manager.getAllSubtasks().size();
        assertAll(
                () -> assertEquals(expectedSize, actualSizeForEpic),
                () -> assertEquals(expectedSize, actualSizeForManager)
        );
    }

    void getAllTasks() {
        assertEquals(0, manager.getAllTasks().size());
        final int expectedSize = 1;
        manager.addTask(task);
        final int actualSize = manager.getAllTasks().size();
        assertEquals(expectedSize, actualSize);
    }

    void getAllEpics() {
        assertEquals(0, manager.getAllEpics().size());
        final int expectedSize = 1;
        manager.addEpic(epic);
        final int actualSize = manager.getAllEpics().size();
        assertEquals(expectedSize, actualSize);
    }

    void getAllSubtasks() {
        assertEquals(0, manager.getAllSubtasks().size());
        final int expectedSize = 1;
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        final int actualSize = manager.getAllSubtasks().size();
        assertEquals(expectedSize, actualSize);
    }

    void getAllEpicSubtasks() {
        assertEquals(0, manager.getAllEpicSubtasks(Integer.MIN_VALUE).size());
        final int expectedSize = 1;
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        final int actualSize = manager.getAllEpicSubtasks(epic.getId()).size();
        assertEquals(expectedSize, actualSize);
    }

    void deleteAllTasks() {
        final int expectedSize = 0;
        manager.deleteAllTasks();
        assertEquals(expectedSize, manager.getAllTasks().size());
        manager.addTask(task);
        manager.deleteAllTasks();
        assertEquals(expectedSize, manager.getAllTasks().size());
    }

    void deleteAllEpics() {
        final int expectedSize = 0;
        manager.deleteAllEpics();
        assertEquals(expectedSize, manager.getAllEpics().size());
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        manager.deleteAllEpics();
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllEpics().size()),
                () -> assertEquals(expectedSize, manager.getAllSubtasks().size())
        );
    }

    void deleteAllSubtasks() {
        final int expectedSizeForEpics = 1;
        final int expectedSizeForSubtasks = 0;
        manager.deleteAllSubtasks();
        assertEquals(0, manager.getAllSubtasks().size());
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        manager.deleteAllSubtasks();
        assertAll(
                () -> assertEquals(expectedSizeForEpics, manager.getAllEpics().size()),
                () -> assertEquals(expectedSizeForSubtasks, manager.getAllSubtasks().size())
        );
    }

    void getHistory() {
        assertEquals(Collections.emptyList(), manager.getHistory());
        manager.addTask(task);
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        manager.getEpic(epic.getId());
        manager.getTask(task.getId());
        manager.getSubtask(subtask.getId());
        List<Integer> expectedHistoryId = List.of(epic.getId(), task.getId(), subtask.getId());
        List<Integer> actualHistoryId = manager.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        assertEquals(expectedHistoryId, actualHistoryId);
    }
}