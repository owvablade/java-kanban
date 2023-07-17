package ru.yandex.storage.interfaces;

import ru.yandex.model.Task;

import java.util.List;

public interface PriorityStorage {

    void add(Task task);

    void update(Task task);

    void remove(Task task);

    List<Task> getPrioritizedTasks();
}
