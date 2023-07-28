package ru.yandex.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.yandex.adapter.DurationAdapter;
import ru.yandex.adapter.LocalDateAdapter;
import ru.yandex.client.KVTaskClient;
import ru.yandex.client.interfaces.TaskClient;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTaskManager {

    private final Gson gson;
    private final TaskClient client;

    public HttpTaskManager(String url) throws IOException, InterruptedException {
        super(url);
        client = new KVTaskClient(url);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter().nullSafe())
                .registerTypeAdapter(Duration.class, new DurationAdapter().nullSafe())
                .create();
    }

    public static HttpTaskManager loadFromServer(String url) throws IOException, InterruptedException {
        HttpTaskManager manager = new HttpTaskManager(url);
        String jsonIds = manager.client.load("ids");
        if (jsonIds == null) {
            return manager;
        }
        List<String> ids = List.of(jsonIds.split(","));
        loadAllTasks(manager, ids);
        loadAllEpics(manager, ids);
        loadAllSubtasks(manager, ids);
        if (ids.contains("history")) {
            loadHistory(manager);
        }
        manager.id++;
        return manager;
    }

    private static void loadAllTasks(HttpTaskManager manager, List<String> ids)
            throws IOException, InterruptedException {
        List<String> onlyTaskIds = ids
                .stream()
                .filter(t -> t.contains("Task"))
                .collect(Collectors.toList());
        for (String id : onlyTaskIds) {
            String jsonTask = manager.client.load(id);
            Task task = manager.gson.fromJson(jsonTask, Task.class);
            manager.taskStorage.add(task);
            manager.priorityStorage.add(task);
            if (manager.id < task.getId()) {
                manager.id = task.getId();
            }
        }
    }

    private static void loadAllEpics(HttpTaskManager manager, List<String> ids)
            throws IOException, InterruptedException {
        List<String> onlyEpicIds = ids
                .stream()
                .filter(t -> t.contains("Epic"))
                .collect(Collectors.toList());
        for (String id : onlyEpicIds) {
            String jsonEpic = manager.client.load(id);
            Epic epic = manager.gson.fromJson(jsonEpic, Epic.class);
            manager.epicStorage.add(epic);
            StatusChecker.checkEpicStatus(epic);
            if (manager.id < epic.getId()) {
                manager.id = epic.getId();
            }
        }
    }

    private static void loadAllSubtasks(HttpTaskManager manager, List<String> ids)
            throws IOException, InterruptedException {
        List<String> onlySubtaskIds = ids
                .stream()
                .filter(t -> t.contains("Subtask"))
                .collect(Collectors.toList());
        for (String id : onlySubtaskIds) {
            String jsonSubtask = manager.client.load(id);
            Subtask subtask = manager.gson.fromJson(jsonSubtask, Subtask.class);
            manager.subtaskStorage.add(subtask);
            Epic epicOfSubtask = manager.epicStorage.get(subtask.getEpicId());
            epicOfSubtask.addSubtask(subtask);
            StatusChecker.checkEpicStatus(epicOfSubtask);
            manager.priorityStorage.add(subtask);
            if (manager.id < subtask.getId()) {
                manager.id = subtask.getId();
            }
        }
    }

    private static void loadHistory(HttpTaskManager manager) throws IOException, InterruptedException {
        String jsonHistory = manager.client.load("history");
        int[] historyIds = manager.gson.fromJson(jsonHistory, int[].class);
        for (int id : historyIds) {
            manager.getTask(id);
            manager.getEpic(id);
            manager.getSubtask(id);
        }
    }

    private void save(Task task) {
        String currentId;
        if (task instanceof Epic) {
            currentId = "Epic";
        } else if (task instanceof Subtask) {
            currentId = "Subtask";
        } else {
            currentId = "Task";
        }
        currentId += id;
        String jsonTasks = gson.toJson(task);
        try {
            client.put(currentId, jsonTasks);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void delete(Task task) {
        String currentId;
        if (task instanceof Epic) {
            currentId = "Epic";
        } else if (task instanceof Subtask) {
            currentId = "Subtask";
        } else {
            currentId = "Task";
        }
        currentId += task.getId();
        try {
            client.delete(currentId);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveHistory() {
        List<Integer> historyIds = history.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        String jsonHistory = gson.toJson(historyIds);
        try {
            client.put("history", jsonHistory);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addTask(Task task) {
        task.setId(id);
        taskStorage.add(task);
        priorityStorage.add(task);
        save(task);
        id++;
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(id);
        epicStorage.add(epic);
        StatusChecker.checkEpicStatus(epic);
        save(epic);
        id++;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        Epic epicOfSubtask = epicStorage.get(subtask.getEpicId());
        subtask.setId(id);
        subtaskStorage.add(subtask);
        epicOfSubtask.addSubtask(subtask);
        StatusChecker.checkEpicStatus(epicOfSubtask);
        priorityStorage.add(subtask);
        save(subtask);
        id++;
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        if (task != null) {
            saveHistory();
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        if (epic != null) {
            saveHistory();
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        if (subtask != null) {
            saveHistory();
        }
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) return;
        priorityStorage.update(task);
        taskStorage.update(task);
        save(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        for (Subtask subtask : epic.getSubtasks()) {
            updateSubtask(subtask);
        }
        epicStorage.update(epic);
        StatusChecker.checkEpicStatus(epic);
        save(epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Epic epicOfSubtask = epicStorage.get(subtask.getEpicId());
        epicOfSubtask.changeSubtask(subtask);
        subtaskStorage.update(subtask);
        StatusChecker.checkEpicStatus(epicStorage.get(subtask.getEpicId()));
        priorityStorage.update(subtask);
        save(subtask);
    }

    @Override
    public void deleteTask(int id) {
        Task task = taskStorage.get(id);
        priorityStorage.remove(task);
        history.remove(id);
        taskStorage.delete(id);
        delete(task);
        saveHistory();
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epicStorage.get(id);
        List<Subtask> subtasks = epic.getSubtasks();
        for (Subtask subtask : subtasks) {
            priorityStorage.remove(subtask);
            history.remove(subtask.getId());
            subtaskStorage.delete(subtask.getId());
            delete(subtask);
        }
        history.remove(id);
        epicStorage.delete(id);
        saveHistory();
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtaskStorage.get(id);
        priorityStorage.remove(subtask);
        epicStorage.get(subtask.getEpicId()).deleteSubtask(subtask);
        history.remove(id);
        subtaskStorage.delete(id);
        delete(subtask);
        StatusChecker.checkEpicStatus(epicStorage.get(subtask.getEpicId()));
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : taskStorage.getAll()) {
            priorityStorage.remove(task);
            history.remove(task.getId());
            delete(task);
        }
        taskStorage.deleteAll();
        saveHistory();
    }

    @Override
    public void deleteAllEpics() {
        this.deleteAllSubtasks();
        for (Epic epic : epicStorage.getAll()) {
            history.remove(epic.getId());
            delete(epic);
        }
        epicStorage.deleteAll();
        saveHistory();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtaskStorage.getAll()) {
            epicStorage.get(subtask.getEpicId()).deleteSubtask(subtask);
            priorityStorage.remove(subtask);
            history.remove(subtask.getId());
            delete(subtask);
        }
        subtaskStorage.deleteAll();
        saveHistory();
    }
}
