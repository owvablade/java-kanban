package ru.yandex.kanban.service;

import ru.yandex.kanban.model.*;
import ru.yandex.kanban.storage.EpicStorage;

import java.util.ArrayList;

public class KanbanStatusChecker {

    private final EpicStorage epicStorage;

    public KanbanStatusChecker(EpicStorage epicStorage) {
        this.epicStorage = epicStorage;
    }

    public void checkEpicStatus(int id) {
        Epic epic = epicStorage.get(id);
        ArrayList<Subtask> epicSubtasks = epic.getSubtasks();
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Task.getNewStatus());
            return;
        }
        boolean isNew = true;
        boolean isDone = true;
        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus().equals(Task.getInProgressStatus())) {
                epic.setStatus(Task.getInProgressStatus());
                return;
            }
            if (subtask.getStatus().equals(Task.getNewStatus())) {
                isDone = false;
            }
            if (subtask.getStatus().equals(Task.getDoneStatus())) {
                isNew = false;
            }
        }
        if (isNew) {
            epic.setStatus(Task.getNewStatus());
        } else if (isDone) {
            epic.setStatus(Task.getDoneStatus());
        }
    }
}
