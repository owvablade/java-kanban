package ru.yandex.util;

import ru.yandex.service.FileBackedTaskManager;
import ru.yandex.service.HttpTaskManager;
import ru.yandex.service.InMemoryHistoryManager;
import ru.yandex.service.interfaces.HistoryManager;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.service.InMemoryTaskManager;

import java.io.File;
import java.io.IOException;

public class Managers {

    public static TaskManager getDefault(String url) throws IOException, InterruptedException {
        return HttpTaskManager.loadFromServer(url);
    }

    public static TaskManager getInMemoryManager() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileBackedManager(String path) {
        return FileBackedTaskManager.loadFromFile(new File(path));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
