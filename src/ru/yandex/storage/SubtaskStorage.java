package ru.yandex.storage;

import ru.yandex.model.Subtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubtaskStorage {

    private final Map<Integer, Subtask> subtasks;

    public SubtaskStorage() {
        subtasks = new HashMap<>();
    }

    public void add(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    public Subtask get(int id) {
        return subtasks.get(id);
    }

    public void update(Subtask subtask) {
        subtasks.replace(subtask.getId(), subtask);
    }

    public void delete(int id) {
        subtasks.remove(id);
    }

    public List<Subtask> getAll() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAll() {
        subtasks.clear();
    }
}
