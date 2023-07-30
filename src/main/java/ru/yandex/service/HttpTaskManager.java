package ru.yandex.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.yandex.adapter.DurationAdapter;
import ru.yandex.adapter.LocalDateAdapter;
import ru.yandex.client.KVTaskClient;
import ru.yandex.client.interfaces.TaskClient;
import ru.yandex.exceptions.ManagerCreateException;
import ru.yandex.exceptions.ManagerLoadException;
import ru.yandex.exceptions.ManagerSaveException;
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

    public HttpTaskManager(String url) {
        super(url);
        try {
            client = new KVTaskClient(url);
        } catch (IOException | InterruptedException e) {
            throw new ManagerCreateException(e.getMessage());
        }
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter().nullSafe())
                .registerTypeAdapter(Duration.class, new DurationAdapter().nullSafe())
                .create();
    }

    public static HttpTaskManager load(String url) {
        HttpTaskManager manager = new HttpTaskManager(url);
        try {
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
        } catch (IOException | InterruptedException e) {
            throw new ManagerLoadException(e.getMessage());
        }
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

    @Override
    protected void save() {
        List<Task> tasks = getAllTasks();
        tasks.addAll(getAllEpics());
        tasks.addAll(getAllSubtasks());
        for (Task task : tasks) {
            String currentId;
            if (task instanceof Epic) {
                currentId = "Epic";
            } else if (task instanceof Subtask) {
                currentId = "Subtask";
            } else {
                currentId = "Task";
            }
            currentId += task.getId();
            String jsonTask = gson.toJson(task);
            try {
                client.put(currentId, jsonTask);
            } catch (IOException | InterruptedException e) {
                throw new ManagerSaveException(e.getMessage());
            }
        }
        saveHistory();
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
            throw new ManagerSaveException(e.getMessage());
        }
    }

    public boolean containsTask(int id) {
        return taskStorage.get(id) != null;
    }

    public boolean containsEpic(int id) {
        return epicStorage.get(id) != null;
    }

    public boolean containsSubtask(int id) {
        return subtaskStorage.get(id) != null;
    }
}
