package ru.yandex.storage;

import ru.yandex.model.Task;
import ru.yandex.storage.interfaces.TaskStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskStorage<T extends Task> implements TaskStorage<T> {

    private final Map<Integer, T> tasks;

    public InMemoryTaskStorage() {
        tasks = new HashMap<>();
    }

    @Override
    public void add(T task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public T get(int id) {
        return tasks.get(id);
    }

    @Override
    public void update(T task) {
        tasks.replace(task.getId(), task);
    }

    @Override
    public void delete(int id) {
        tasks.remove(id);
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAll() {
        tasks.clear();
    }
}
