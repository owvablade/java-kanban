package ru.yandex.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Status;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.util.Managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private static final String PATH = "src/test/java/ru/yandex/service/resources/tasks.csv";

    @BeforeEach
    void beforeEach() {
        manager = Managers.getFileBackedManager(PATH);
    }

    @AfterEach
    void afterEach() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(PATH);
        writer.write("");
        writer.close();
    }

    @Test
    void shouldSaveTasksWithHistory() throws IOException {
        final String expectedFileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,TASK,Task,NEW,Task description,2023-07-17T10:00,PT20M,2023-07-17T10:20,\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,,\n" +
                "2,SUBTASK,Subtask,NEW,Subtask description,,PT30M,,1\n" +
                "\n" +
                "1,0";
        manager.addTask(task);
        manager.addEpic(epic);
        manager.getEpic(epic.getId());
        manager.getTask(task.getId());
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        final String actualFileContent = Files.readString(Paths.get(PATH));
        assertEquals(expectedFileContent, actualFileContent);
    }

    @Test
    void shouldSaveTasksWithoutHistory() throws IOException {
        final String expectedFileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,TASK,Task,NEW,Task description,2023-07-17T10:00,PT20M,2023-07-17T10:20,\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,,\n" +
                "2,SUBTASK,Subtask,NEW,Subtask description,,PT30M,,1\n";
        manager.addTask(task);
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        final String actualFileContent = Files.readString(Paths.get(PATH));
        assertEquals(expectedFileContent, actualFileContent);
    }

    @Test
    void shouldSaveEmptyListOfTasks() throws IOException {
        final String expectedFileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n";
        manager.addTask(task);
        manager.deleteTask(task.getId());
        final String actualFileContent = Files.readString(Paths.get(PATH));
        assertEquals(expectedFileContent, actualFileContent);
    }

    @Test
    void shouldSaveEpicWithoutSubtask() throws IOException {
        final String expectedFileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT20M,,\n";
        manager.addEpic(epic);
        final String actualFileContent = Files.readString(Paths.get(PATH));
        assertEquals(expectedFileContent, actualFileContent);
    }

    @Test
    void shouldLoadTasks() throws FileNotFoundException {
        final String fileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,TASK,Task,NEW,Task description,2023-07-17T10:00,PT20M,2023-07-17T10:20,\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,2023-07-17T10:30,\n" +
                "2,SUBTASK,Subtask,NEW,Subtask description,2023-07-17T10:00,PT30M,2023-07-17T10:30,1\n" +
                "\n" +
                "1,0";
        PrintWriter writer = new PrintWriter(PATH);
        writer.write(fileContent);
        writer.close();
        manager = FileBackedTaskManager.loadFromFile(new File(PATH));
        final List<Integer> expectedHistory = List.of(1, 0);
        final List<Integer> actualHistory = manager.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        final Task actualTask = manager.getTask(0);
        final Epic actualEpic = manager.getEpic(1);
        final Subtask actualSubtask = manager.getSubtask(2);
        assertAll(
                () -> assertEquals(expectedHistory, actualHistory),
                () -> assertEquals(0, actualTask.getId()),
                () -> assertEquals("Task", actualTask.getName()),
                () -> assertEquals(Status.NEW, actualTask.getStatus()),
                () -> assertEquals("Task description", actualTask.getDescription()),
                () -> assertEquals(1, actualEpic.getId()),
                () -> assertEquals("Epic", actualEpic.getName()),
                () -> assertEquals(Status.NEW, actualEpic.getStatus()),
                () -> assertEquals("Epic description", actualEpic.getDescription()),
                () -> assertEquals(2, actualSubtask.getId()),
                () -> assertEquals("Subtask", actualSubtask.getName()),
                () -> assertEquals(Status.NEW, actualSubtask.getStatus()),
                () -> assertEquals("Subtask description", actualSubtask.getDescription()),
                () -> assertEquals(1, actualSubtask.getEpicId())
        );
    }

    @Test
    void shouldLoadTasksWithoutHistory() throws FileNotFoundException {
        final String fileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,TASK,Task,NEW,Task description,2023-07-17T10:00,PT20M,2023-07-17T10:20,\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,2023-07-17T10:30,\n" +
                "2,SUBTASK,Subtask,NEW,Subtask description,2023-07-17T10:00,PT30M,2023-07-17T10:30,1\n";
        PrintWriter writer = new PrintWriter(PATH);
        writer.write(fileContent);
        writer.close();
        manager = FileBackedTaskManager.loadFromFile(new File(PATH));
        final List<Integer> expectedHistory = List.of();
        final List<Integer> actualHistory = manager.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        final Task actualTask = manager.getTask(0);
        final Epic actualEpic = manager.getEpic(1);
        final Subtask actualSubtask = manager.getSubtask(2);
        assertAll(
                () -> assertEquals(expectedHistory, actualHistory),
                () -> assertEquals(0, actualTask.getId()),
                () -> assertEquals("Task", actualTask.getName()),
                () -> assertEquals(Status.NEW, actualTask.getStatus()),
                () -> assertEquals("Task description", actualTask.getDescription()),
                () -> assertEquals(1, actualEpic.getId()),
                () -> assertEquals("Epic", actualEpic.getName()),
                () -> assertEquals(Status.NEW, actualEpic.getStatus()),
                () -> assertEquals("Epic description", actualEpic.getDescription()),
                () -> assertEquals(2, actualSubtask.getId()),
                () -> assertEquals("Subtask", actualSubtask.getName()),
                () -> assertEquals(Status.NEW, actualSubtask.getStatus()),
                () -> assertEquals("Subtask description", actualSubtask.getDescription()),
                () -> assertEquals(1, actualSubtask.getEpicId())
        );
    }

    @Test
    void shouldLoadEmptyFile() throws FileNotFoundException {
        final int expectedSize = 0;
        final String fileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n";
        PrintWriter writer = new PrintWriter(PATH);
        writer.write(fileContent);
        writer.close();
        manager = FileBackedTaskManager.loadFromFile(new File(PATH));
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllTasks().size()),
                () -> assertEquals(expectedSize, manager.getAllEpics().size()),
                () -> assertEquals(expectedSize, manager.getAllSubtasks().size()),
                () -> assertEquals(expectedSize, manager.getHistory().size())
        );
    }

    @Test
    void shouldLoadEpicWithoutSubtasks() throws FileNotFoundException {
        final String fileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,2023-07-17T10:30,\n";
        PrintWriter writer = new PrintWriter(PATH);
        writer.write(fileContent);
        writer.close();
        manager = FileBackedTaskManager.loadFromFile(new File(PATH));
        final List<Integer> expectedHistory = List.of();
        final List<Integer> actualHistory = manager.getHistory()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        final Epic actualEpic = manager.getEpic(1);
        assertAll(
                () -> assertEquals(expectedHistory, actualHistory),
                () -> assertEquals(1, actualEpic.getId()),
                () -> assertEquals("Epic", actualEpic.getName()),
                () -> assertEquals(Status.NEW, actualEpic.getStatus()),
                () -> assertEquals("Epic description", actualEpic.getDescription())
        );
    }
}
