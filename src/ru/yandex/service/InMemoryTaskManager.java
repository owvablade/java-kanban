package ru.yandex.service;

import ru.yandex.model.*;
import ru.yandex.service.interfaces.HistoryManager;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.storage.InMemoryTaskStorage;
import ru.yandex.storage.interfaces.TaskStorage;
import ru.yandex.util.Managers;

import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    protected int id;
    protected final HistoryManager history;
    private final StatusChecker statusChecker;
    protected final TaskStorage<Task> taskStorage;
    protected final TaskStorage<Epic> epicStorage;
    protected final TaskStorage<Subtask> subtaskStorage;

    public InMemoryTaskManager() {
        history = Managers.getDefaultHistory();
        taskStorage = new InMemoryTaskStorage<>();
        epicStorage = new InMemoryTaskStorage<>();
        subtaskStorage = new InMemoryTaskStorage<>();
        statusChecker = new StatusChecker(epicStorage);
    }

    @Override
    public void addTask(Task task) {
        if (task == null) return;
        task.setId(id);
        taskStorage.add(task);
        id++;
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic == null) return;
        epic.setId(id);
        epicStorage.add(epic);
        statusChecker.checkEpicStatus(id);
        id++;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) return;
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
        if (task == null) return;
        taskStorage.update(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) return;
        for (Subtask subtask : epic.getSubtasks()) {
            updateSubtask(subtask);
        }
        epicStorage.update(epic);
        statusChecker.checkEpicStatus(epic.getId());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) return;
        epicStorage.get(subtask.getEpicId()).changeSubtask(subtask);
        subtaskStorage.update(subtask);
        statusChecker.checkEpicStatus(subtask.getEpicId());
    }

    @Override
    public void deleteTask(int id) {
        history.remove(id);
        taskStorage.delete(id);
    }

    @Override
    public void deleteEpic(int id) {
        List<Subtask> subtasks = epicStorage.get(id).getSubtasks();
        for (Subtask subtask : subtasks) {
            history.remove(subtask.getId());
            subtaskStorage.delete(subtask.getId());
        }
        history.remove(id);
        epicStorage.delete(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtaskStorage.get(id);
        epicStorage.get(subtask.getEpicId()).deleteSubtask(subtask);
        history.remove(id);
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
        for (Task task : taskStorage.getAll()) {
            history.remove(task.getId());
        }
        taskStorage.deleteAll();
    }

    @Override
    public void deleteAllEpics() {
        deleteAllSubtasks();
        for (Epic epic : epicStorage.getAll()) {
            history.remove(epic.getId());
        }
        epicStorage.deleteAll();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtaskStorage.getAll()) {
            epicStorage.get(subtask.getEpicId()).deleteSubtask(subtask);
            history.remove(subtask.getId());
        }
        subtaskStorage.deleteAll();
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }
}
