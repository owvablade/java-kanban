package ru.yandex.util;

import ru.yandex.service.FileBackedTaskManager;
import ru.yandex.service.HttpTaskManager;
import ru.yandex.service.InMemoryHistoryManager;
import ru.yandex.service.InMemoryTaskManager;

import java.io.File;
import java.io.IOException;

public class Managers {

    public static HttpTaskManager getDefault(String url) throws IOException, InterruptedException {
        return HttpTaskManager.loadFromServer(url);
    }

    public static InMemoryTaskManager getInMemoryManager() {
        return new InMemoryTaskManager();
    }

    public static FileBackedTaskManager getFileBackedManager(String path) {
        return FileBackedTaskManager.loadFromFile(new File(path));
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
