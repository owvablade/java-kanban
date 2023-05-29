package ru.yandex.storage;

import ru.yandex.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskStorage {

    private final Map<Integer, Task> tasks;

    public TaskStorage() {
        tasks = new HashMap<>();
    }

    public void add(Task task) {
        tasks.put(task.getId(), task);
    }

    public Task get(int id) {
        return tasks.get(id);
    }

    public void update(Task task) {
        tasks.replace(task.getId(), task);
    }

    public void delete(int id) {
        tasks.remove(id);
    }

    public List<Task> getAll() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAll() {
        tasks.clear();
    }
}
