package ru.yandex.service;

import ru.yandex.exceptions.ManagerLoadException;
import ru.yandex.exceptions.ManagerSaveException;
import ru.yandex.model.*;
import ru.yandex.service.interfaces.HistoryManager;
import ru.yandex.service.interfaces.TaskManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final String path;

    public FileBackedTaskManager(String path) {
        this.path = path;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        try {
            String csv = Files.readString(Paths.get(file.getPath()));
            String[] csvEntries = csv.split("\n");
            List<Task> tasks = manager.deserializeTasks(csvEntries);
            tasks.sort(Comparator.comparingInt(Task::getId));
            if (!tasks.isEmpty()) {
                manager.id = tasks.get(0).getId();
            }
            for (Task task : tasks) {
                if (task instanceof Subtask) {
                    Subtask subtask = (Subtask) task;
                    manager.subtaskStorage.add(subtask);
                    manager.epicStorage.get(subtask.getEpicId()).addSubtask(subtask);
                    manager.statusChecker.checkEpicStatus(subtask.getEpicId());
                    manager.id = subtask.getId();
                } else if (task instanceof Epic) {
                    Epic epic = (Epic) task;
                    manager.epicStorage.add(epic);
                    manager.statusChecker.checkEpicStatus(epic.getId());
                    manager.id = epic.getId();
                } else {
                    manager.taskStorage.add(task);
                    manager.id = task.getId();
                }
            }
            manager.id++;
            for (Integer historyItem : FileBackedTaskManager.historyFromString(csvEntries[csvEntries.length - 1])) {
                manager.getTask(historyItem);
                manager.getEpic(historyItem);
                manager.getSubtask(historyItem);
            }
        } catch (IOException e) {
            throw new ManagerLoadException(e.getMessage());
        }
        return manager;
    }

    private void save() {
        List<Task> tasks = getAllTasks();
        tasks.addAll(getAllEpics());
        tasks.addAll(getAllSubtasks());
        tasks.sort(Comparator.comparingInt(Task::getId));
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
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    public static void main(String[] args) {
        final Random random = new Random();
        String path = "src/ru/yandex/resources/tasks.csv";
        TaskManager manager = FileBackedTaskManager.loadFromFile(new File(path));

        System.out.println("Содержимое файла tasks.csv после работы main() в FileBackedTaskManager:");
        printFileContent(path);

        printManagerContent(manager);

        Task task1 = new Task()
                .setId(random.nextInt())
                .setName("Почитать книгу")
                .setStatus(Status.NEW)
                .setDescription("Личное");
        manager.addTask(task1);

        Epic epic1 = (Epic) new Epic()
                .setId(random.nextInt())
                .setName("Сделать ФП6")
                .setStatus(Status.NEW)
                .setDescription("ЯП");
        manager.addEpic(epic1);

        List<Task> tasks = manager.getAllTasks();
        tasks.addAll(manager.getAllEpics());
        tasks.addAll(manager.getAllSubtasks());
        Collections.shuffle(tasks);
        for (Task task : tasks) {
            manager.getTask(task.getId());
            manager.getEpic(task.getId());
            manager.getSubtask(task.getId());
        }

        Subtask epic1subtask1 = (Subtask) new Subtask()
                .setId(random.nextInt())
                .setName("Декомпозировать задачу")
                .setStatus(Status.DONE)
                .setDescription("ЯП ФП");
        epic1subtask1.setEpicId(epic1.getId());
        manager.addSubtask(epic1subtask1);

        System.out.println("\nСодержимое файла tasks.csv после формирования новой истории и " +
                "добавления новых задач в новый FileBackedTaskManager:");
        printFileContent(path);

        printManagerContent(manager);
    }

    private static void printFileContent(String path) {
        try {
            System.out.println(Files.readString(Paths.get(path)));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void printManagerContent(TaskManager manager) {
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
    }
}
