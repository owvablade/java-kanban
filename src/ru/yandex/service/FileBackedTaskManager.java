package ru.yandex.service;

import ru.yandex.comparators.TaskIdComparator;
import ru.yandex.exceptions.ManagerLoadException;
import ru.yandex.exceptions.ManagerSaveException;
import ru.yandex.model.*;
import ru.yandex.service.interfaces.HistoryManager;
import ru.yandex.service.interfaces.TaskManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final String path;

    public FileBackedTaskManager(String path) {
        this.path = path;
    }

    public static FileBackedTaskManager loadFromFile(File file) throws ManagerLoadException {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        try {
            String csv = Files.readString(Paths.get(manager.path));
            String[] csvEntries = csv.split("\n");
            List<Task> tasks = manager.deserializeTasks(csvEntries);
            tasks.sort(new TaskIdComparator());
            if (!tasks.isEmpty()) {
                manager.id = tasks.get(0).getId();
            }
            for (Task task : tasks) {
                if (task instanceof Subtask) {
                    manager.addSubtask((Subtask) task);
                } else if (task instanceof Epic) {
                    manager.addEpic((Epic) task);
                } else {
                    manager.addTask(task);
                }
            }
            List<Integer> history = FileBackedTaskManager.historyFromString(csvEntries[csvEntries.length - 1]);
            for (Integer historyItem : history) {
                manager.getTask(historyItem);
                manager.getEpic(historyItem);
                manager.getSubtask(historyItem);
            }
            manager.save();
        } catch (IOException e) {
            throw new ManagerLoadException(e.getMessage());
        }
        return manager;
    }

    private void save() throws ManagerSaveException {
        List<Task> tasks = super.getAllTasks();
        tasks.addAll(super.getAllEpics());
        tasks.addAll(super.getAllSubtasks());
        tasks.sort(new TaskIdComparator());
        try {
            String serializedTasks = serializeTasks(tasks);
            String serializedHistory = historyToString(super.history);
            Files.writeString(Paths.get(path), serializedTasks + serializedHistory);
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    private String serializeTasks(List<Task> tasks) {
        final String header = "id,type,name,status,description,epic\n";
        StringBuilder sb = new StringBuilder(header);
        for (Task task : tasks) {
            sb.append(toString(task)).append("\n");
        }
        return sb.toString();
    }

    private List<Task> deserializeTasks(String[] values) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i < values.length - 2; i++) {
            tasks.add(fromString(values[i]));
        }
        return tasks;
    }

    private String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        if (task instanceof Epic) {
            sb.append(TaskType.EPIC).append(",");
        } else if (task instanceof Subtask) {
            sb.append(TaskType.SUBTASK).append(",");
        } else {
            sb.append(TaskType.TASK).append(",");
        }
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");
        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId());
        }
        return sb.toString();
    }

    private Task fromString(String value) {
        String[] fields = value.split(",");
        Task task;
        if (TaskType.valueOf(fields[1]) == TaskType.TASK) {
            task = new Task();
        } else if (TaskType.valueOf(fields[1]) == TaskType.EPIC) {
            task = new Epic();
        } else {
            task = new Subtask();
            ((Subtask) task).setEpicId(Integer.parseInt(fields[5]));
        }
        task.setId(Integer.parseInt(fields[0]));
        task.setName(fields[2]);
        task.setStatus(Status.valueOf(fields[3]));
        task.setDescription(fields[4]);
        return task;
    }

    private static String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        StringBuilder sb = new StringBuilder("\n");
        for (Task task : history) {
            sb.append(task.getId()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static List<Integer> historyFromString(String value) {
        String[] fields = value.split(",");
        List<Integer> history = new ArrayList<>();
        for (String field : fields) {
            history.add(Integer.parseInt(field));
        }
        return history;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        try {
            save();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        String path = "src/ru/yandex/resources/tasks.csv";
        try {
            TaskManager manager = FileBackedTaskManager.loadFromFile(new File(path));
            System.out.println("Содержимое файла tasks.csv после работы main() в FileBackedTaskManager:");
            try {
                System.out.println(Files.readString(Paths.get("src/ru/yandex/resources/tasks.csv")));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("Проверка содержимого в новом FileBackedTaskManager:");
            for (Task task : manager.getAllTasks()) {
                System.out.println(task);
            }
            for (Task epic : manager.getAllEpics()) {
                System.out.println(epic);
            }
            for (Task subtask : manager.getAllSubtasks()) {
                System.out.println(subtask);
            }
        } catch (ManagerLoadException e) {
            System.out.println(e.getMessage() + "\nНе удалось загрузить файл " + path);
        }
    }
}
