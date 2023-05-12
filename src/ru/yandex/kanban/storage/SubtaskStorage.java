package ru.yandex.kanban.storage;

import ru.yandex.kanban.model.Subtask;

import java.util.ArrayList;
import java.util.HashMap;

public class SubtaskStorage {

    private final HashMap<Integer, Subtask> subtasks;

    public SubtaskStorage() {
        subtasks = new HashMap<>();
    }

    public void add(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    public Subtask get(int id) {
        return subtasks.get(id);
    }

    public void update(Subtask task) {
        subtasks.replace(task.getId(), task);
    }

    public void delete(int id) {
        subtasks.remove(id);
    }

    public ArrayList<Subtask> getAll() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAll() {
        subtasks.clear();
    }
}
