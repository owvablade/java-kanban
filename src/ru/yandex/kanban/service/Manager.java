package ru.yandex.kanban.service;

import ru.yandex.kanban.model.*;
import ru.yandex.kanban.storage.EpicStorage;
import ru.yandex.kanban.storage.SubtaskStorage;
import ru.yandex.kanban.storage.TaskStorage;

import java.util.ArrayList;

public class Manager {

    private static int id = 1;
    private final TaskStorage taskStorage;
    private final EpicStorage epicStorage;
    private final SubtaskStorage subtaskStorage;
    private final StatusChecker statusChecker;

    public Manager() {
        taskStorage = new TaskStorage();
        epicStorage = new EpicStorage();
        subtaskStorage = new SubtaskStorage();
        statusChecker = new StatusChecker(epicStorage);
    }

    public void addTask(Task task) {
        task.setId(id);
        taskStorage.add(task);
        id++;
    }

    public void addEpic(Epic epic) {
        epic.setId(id);
        epicStorage.add(epic);
        statusChecker.checkEpicStatus(id);
        id++;
    }

    public void addSubtask(Subtask subtask) {
        subtask.setId(id);
        subtaskStorage.add(subtask);
        epicStorage.get(subtask.getEpicId()).addSubtask(subtask);
        statusChecker.checkEpicStatus(subtask.getEpicId());
        id++;
    }

    public Task getTask(int id) {
        return taskStorage.get(id);
    }

    public Epic getEpic(int id) {
        return epicStorage.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtaskStorage.get(id);
    }

    public void updateTask(Task task) {
        taskStorage.update(task);
    }

    public void updateEpic(Epic epic) {
        for (Subtask subtask : epic.getSubtasks()) {
            updateSubtask(subtask);
        }
        epicStorage.update(epic);
        statusChecker.checkEpicStatus(epic.getId());
    }

    public void updateSubtask(Subtask subtask) {
        epicStorage.get(subtask.getEpicId()).changeSubtask(subtask);
        subtaskStorage.update(subtask);
        statusChecker.checkEpicStatus(subtask.getEpicId());
    }

    public void deleteTask(int id) {
        taskStorage.delete(id);
    }

    public void deleteEpic(int id) {
        ArrayList<Subtask> subtasks = getEpic(id).getSubtasks();
        for (Subtask subtask : subtasks) {
            subtaskStorage.delete(subtask.getId());
        }
        epicStorage.delete(id);
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtaskStorage.get(id);
        epicStorage.get(subtask.getEpicId()).deleteSubtask(subtask);
        subtaskStorage.delete(id);
        statusChecker.checkEpicStatus(subtask.getEpicId());
    }

    public ArrayList<Task> getAllTasks() {
        return taskStorage.getAll();
    }

    public ArrayList<Epic> getAllEpics() {
        return epicStorage.getAll();
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return subtaskStorage.getAll();
    }

    public ArrayList<Subtask> getAllEpicSubtasks(int id) {
        return epicStorage.get(id).getSubtasks();
    }

    public void deleteAllTasks() {
        taskStorage.deleteAll();
    }

    public void deleteAllEpics() {
        deleteAllSubtasks();
        epicStorage.deleteAll();
    }

    public void deleteAllSubtasks() {
        subtaskStorage.deleteAll();
    }
}
