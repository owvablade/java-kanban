package ru.yandex.util;

import ru.yandex.service.FileBackedTaskManager;
import ru.yandex.service.InMemoryHistoryManager;
import ru.yandex.service.interfaces.HistoryManager;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.service.InMemoryTaskManager;

public class Managers {

    public static TaskManager getInMemoryManager() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileBackedManager(String path) {
        return new FileBackedTaskManager(path);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
