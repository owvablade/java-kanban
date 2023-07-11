package ru.yandex.storage.interfaces;

import ru.yandex.model.Task;

import java.util.List;

public interface TaskStorage<T extends Task> {

    void add(T task);

    T get(int id);

    void update(T task);

    void delete(int id);

    List<T> getAll();

    void deleteAll();
}
