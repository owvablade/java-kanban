package ru.yandex.util;

import ru.yandex.service.InMemoryHistoryManager;
import ru.yandex.service.model.HistoryManager;
import ru.yandex.service.model.TaskManager;
import ru.yandex.service.InMemoryTaskManager;

public class Managers {

    public static TaskManager getDefaultManager() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
