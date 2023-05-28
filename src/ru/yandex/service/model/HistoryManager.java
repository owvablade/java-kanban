package ru.yandex.service.model;

import ru.yandex.model.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task task);

    List<Task> getHistory();
}
