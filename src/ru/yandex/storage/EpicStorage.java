package ru.yandex.storage;

import ru.yandex.model.Epic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EpicStorage {

    private final Map<Integer, Epic> epics;

    public EpicStorage() {
        epics = new HashMap<>();
    }

    public void add(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public Epic get(int id) {
        return epics.get(id);
    }

    public void update(Epic epic) {
        epics.replace(epic.getId(), epic);
    }

    public void delete(int id) {
        epics.remove(id);
    }

    public List<Epic> getAll() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAll() {
        epics.clear();
    }
}
