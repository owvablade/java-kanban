package ru.yandex.service;

import ru.yandex.model.*;
import ru.yandex.service.interfaces.HistoryManager;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.storage.EpicStorage;
import ru.yandex.storage.SubtaskStorage;
import ru.yandex.storage.TaskStorage;
import ru.yandex.util.Managers;

import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private int id;
    private final HistoryManager history;
    private final TaskStorage taskStorage;
    private final EpicStorage epicStorage;
    private final SubtaskStorage subtaskStorage;
    private final StatusChecker statusChecker;

    public InMemoryTaskManager() {
        taskStorage = new TaskStorage();
        epicStorage = new EpicStorage();
        subtaskStorage = new SubtaskStorage();
        history = Managers.getDefaultHistory();
        statusChecker = new StatusChecker(epicStorage);
    }

    @Override
    public void addTask(Task task) {
        task.setId(id);
        taskStorage.add(task);
        id++;
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(id);
        epicStorage.add(epic);
        statusChecker.checkEpicStatus(id);
        id++;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(id);
        subtaskStorage.add(subtask);
        epicStorage.get(subtask.getEpicId()).addSubtask(subtask);
        statusChecker.checkEpicStatus(subtask.getEpicId());
        id++;
    }

    @Override
    public Task getTask(int id) {
        history.add(taskStorage.get(id));
        return taskStorage.get(id);
    }

    @Override
    public Epic getEpic(int id) {
        history.add(epicStorage.get(id));
        return epicStorage.get(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        history.add(subtaskStorage.get(id));
        return subtaskStorage.get(id);
    }

    @Override
    public void updateTask(Task task) {
        taskStorage.update(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        for (Subtask subtask : epic.getSubtasks()) {
            updateSubtask(subtask);
        }
        epicStorage.update(epic);
        statusChecker.checkEpicStatus(epic.getId());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        epicStorage.get(subtask.getEpicId()).changeSubtask(subtask);
        subtaskStorage.update(subtask);
        statusChecker.checkEpicStatus(subtask.getEpicId());
    }

    @Override
    public void deleteTask(int id) {
        taskStorage.delete(id);
    }

    @Override
    public void deleteEpic(int id) {
        List<Subtask> subtasks = epicStorage.get(id).getSubtasks();
        for (Subtask subtask : subtasks) {
            subtaskStorage.delete(subtask.getId());
        }
        epicStorage.delete(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtaskStorage.get(id);
        epicStorage.get(subtask.getEpicId()).deleteSubtask(subtask);
        subtaskStorage.delete(id);
        statusChecker.checkEpicStatus(subtask.getEpicId());
    }

    @Override
    public List<Task> getAllTasks() {
        return taskStorage.getAll();
    }

    @Override
    public List<Epic> getAllEpics() {
        return epicStorage.getAll();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return subtaskStorage.getAll();
    }

    @Override
    public List<Subtask> getAllEpicSubtasks(int id) {
        return epicStorage.get(id).getSubtasks();
    }

    @Override
    public void deleteAllTasks() {
        taskStorage.deleteAll();
    }

    @Override
    public void deleteAllEpics() {
        deleteAllSubtasks();
        epicStorage.deleteAll();
    }

    @Override
    public void deleteAllSubtasks() {
        subtaskStorage.deleteAll();
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }
}
