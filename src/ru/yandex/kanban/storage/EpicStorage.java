package ru.yandex.kanban.storage;

import ru.yandex.kanban.model.Epic;

import java.util.ArrayList;
import java.util.HashMap;

public class EpicStorage {

    private final HashMap<Integer, Epic> epics;

    public EpicStorage() {
        epics = new HashMap<>();
    }

    public void add(Epic Epic) {
        epics.put(Epic.getId(), Epic);
    }

    public Epic get(int id) {
        return epics.get(id);
    }

    public void update(Epic Epic) {
        epics.replace(Epic.getId(), Epic);
    }

    public void delete(int id) {
        epics.remove(id);
    }

    public ArrayList<Epic> getAll() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAll() {
        epics.clear();
    }
}
