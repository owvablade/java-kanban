package ru.yandex.util;

import ru.yandex.service.FileBackedTaskManager;
import ru.yandex.service.HttpTaskManager;
import ru.yandex.service.InMemoryHistoryManager;
import ru.yandex.service.InMemoryTaskManager;

public class Managers {

    public static HttpTaskManager getDefault(String url) {
        return HttpTaskManager.load(url);
    }

    public static InMemoryTaskManager getInMemoryManager() {
        return new InMemoryTaskManager();
    }

    public static FileBackedTaskManager getFileBackedManager(String filePath) {
        return FileBackedTaskManager.load(filePath);
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
