package ru.yandex.service;

import ru.yandex.model.*;
import ru.yandex.storage.EpicStorage;

import java.util.List;

public class StatusChecker {

    private final EpicStorage epicStorage;

    public StatusChecker(EpicStorage epicStorage) {
        this.epicStorage = epicStorage;
    }

    public void checkEpicStatus(int id) {
        Epic epic = epicStorage.get(id);
        List<Subtask> epicSubtasks = epic.getSubtasks();
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        boolean isNew = true;
        boolean isDone = true;
        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() == Status.IN_PROGRESS) {
                epic.setStatus(Status.IN_PROGRESS);
                return;
            }
            if (subtask.getStatus() == Status.NEW) {
                isDone = false;
            }
            if (subtask.getStatus() == Status.DONE) {
                isNew = false;
            }
        }
        if (isNew) {
            epic.setStatus(Status.NEW);
        } else if (isDone) {
            epic.setStatus(Status.DONE);
        }
    }
}
