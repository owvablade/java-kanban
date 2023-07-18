package ru.yandex.service;

import ru.yandex.model.*;
import ru.yandex.service.interfaces.HistoryManager;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.storage.InMemoryTaskStorage;
import ru.yandex.storage.PriorityStorageManager;
import ru.yandex.storage.interfaces.PriorityStorage;
import ru.yandex.storage.interfaces.TaskStorage;
import ru.yandex.util.Managers;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected int id;
    protected final HistoryManager history;
    protected final TaskStorage<Task> taskStorage;
    protected final TaskStorage<Epic> epicStorage;
    protected final TaskStorage<Subtask> subtaskStorage;
    protected final PriorityStorage priorityStorage;

    public InMemoryTaskManager() {
        history = Managers.getDefaultHistory();
        taskStorage = new InMemoryTaskStorage<>();
        epicStorage = new InMemoryTaskStorage<>();
        subtaskStorage = new InMemoryTaskStorage<>();
        priorityStorage = new PriorityStorageManager();
    }

    @Override
    public void addTask(Task task) {
        if (task == null) return;
        task.setId(id);
        taskStorage.add(task);
        priorityStorage.add(task);
        id++;
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic == null) return;
        epic.setId(id);
        epicStorage.add(epic);
        StatusChecker.checkEpicStatus(epic);
        id++;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) return;
        Epic epicOfSubtask = epicStorage.get(subtask.getEpicId());
        if (epicOfSubtask == null) return;
        subtask.setId(id);
        subtaskStorage.add(subtask);
        epicOfSubtask.addSubtask(subtask);
        StatusChecker.checkEpicStatus(epicOfSubtask);
        priorityStorage.add(subtask);
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
        priorityStorage.update(task);
        taskStorage.update(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) return;
        for (Subtask subtask : epic.getSubtasks()) {
            updateSubtask(subtask);
        }
        epicStorage.update(epic);
        StatusChecker.checkEpicStatus(epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) return;
        Epic epicOfSubtask = epicStorage.get(subtask.getEpicId());
        if (epicOfSubtask == null) return;
        epicOfSubtask.changeSubtask(subtask);
        subtaskStorage.update(subtask);
        StatusChecker.checkEpicStatus(epicStorage.get(subtask.getEpicId()));
        priorityStorage.update(subtask);
    }

    @Override
    public void deleteTask(int id) {
        Task task = taskStorage.get(id);
        if (task == null) return;
        priorityStorage.remove(task);
        history.remove(id);
        taskStorage.delete(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epicStorage.get(id);
        if (epic == null) return;
        List<Subtask> subtasks = epic.getSubtasks();
        for (Subtask subtask : subtasks) {
            priorityStorage.remove(subtask);
            history.remove(subtask.getId());
            subtaskStorage.delete(subtask.getId());
        }
        history.remove(id);
        epicStorage.delete(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtaskStorage.get(id);
        if (subtask == null) return;
        priorityStorage.remove(subtask);
        epicStorage.get(subtask.getEpicId()).deleteSubtask(subtask);
        history.remove(id);
        subtaskStorage.delete(id);
        StatusChecker.checkEpicStatus(epicStorage.get(subtask.getEpicId()));
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
        Epic epic = epicStorage.get(id);
        if (epic == null) return Collections.emptyList();
        return epic.getSubtasks();
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : taskStorage.getAll()) {
            priorityStorage.remove(task);
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
            priorityStorage.remove(subtask);
            history.remove(subtask.getId());
        }
        subtaskStorage.deleteAll();
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return priorityStorage.getPrioritizedTasks();
    }
}
