package ru.yandex.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Status;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

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
        manager = new FileBackedTaskManager(PATH);
    }

    @AfterEach
    void afterEach() {
        try (PrintWriter writer = new PrintWriter(PATH)) {
            writer.write("");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    void shouldSaveTasksWithHistory() {
        final String expectedFileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,TASK,Task,NEW,Task description,2023-07-17T10:00,PT20M,2023-07-17T10:20,\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,2023-07-17T10:30,\n" +
                "2,SUBTASK,Subtask,NEW,Subtask description,2023-07-17T10:00,PT30M,2023-07-17T10:30,1\n" +
                "\n" +
                "1,0";
        manager.addTask(task);
        manager.addEpic(epic);
        manager.getEpic(epic.getId());
        manager.getTask(task.getId());
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        try {
            final String actualFileContent = Files.readString(Paths.get(PATH));
            assertEquals(expectedFileContent, actualFileContent);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    void shouldSaveTasksWithoutHistory() {
        final String expectedFileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,TASK,Task,NEW,Task description,2023-07-17T10:00,PT20M,2023-07-17T10:20,\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,2023-07-17T10:30,\n" +
                "2,SUBTASK,Subtask,NEW,Subtask description,2023-07-17T10:00,PT30M,2023-07-17T10:30,1\n";
        manager.addTask(task);
        manager.addEpic(epic);
        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);
        try {
            final String actualFileContent = Files.readString(Paths.get(PATH));
            assertEquals(expectedFileContent, actualFileContent);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    void shouldSaveEmptyListOfTasks() {
        final String expectedFileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n";
        manager.addTask(task);
        manager.deleteTask(task.getId());
        try {
            final String actualFileContent = Files.readString(Paths.get(PATH));
            assertEquals(expectedFileContent, actualFileContent);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    void shouldSaveEpicWithoutSubtask() {
        final String expectedFileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT20M,,\n";
        manager.addEpic(epic);
        try {
            final String actualFileContent = Files.readString(Paths.get(PATH));
            assertEquals(expectedFileContent, actualFileContent);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    void shouldLoadTasks() {
        final String fileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,TASK,Task,NEW,Task description,2023-07-17T10:00,PT20M,2023-07-17T10:20,\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,2023-07-17T10:30,\n" +
                "2,SUBTASK,Subtask,NEW,Subtask description,2023-07-17T10:00,PT30M,2023-07-17T10:30,1\n" +
                "\n" +
                "1,0";
        try (PrintWriter writer = new PrintWriter(PATH)) {
            writer.write(fileContent);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
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
    void shouldLoadTasksWithoutHistory() {
        final String fileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "0,TASK,Task,NEW,Task description,2023-07-17T10:00,PT20M,2023-07-17T10:20,\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,2023-07-17T10:30,\n" +
                "2,SUBTASK,Subtask,NEW,Subtask description,2023-07-17T10:00,PT30M,2023-07-17T10:30,1\n";
        try (PrintWriter writer = new PrintWriter(PATH)) {
            writer.write(fileContent);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
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
    void shouldLoadEmptyFile() {
        final int expectedSize = 0;
        final String fileContent = "id,type,name,status,description,epic\n";
        try (PrintWriter writer = new PrintWriter(PATH)) {
            writer.write(fileContent);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
        manager = FileBackedTaskManager.loadFromFile(new File(PATH));
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllTasks().size()),
                () -> assertEquals(expectedSize, manager.getAllEpics().size()),
                () -> assertEquals(expectedSize, manager.getAllSubtasks().size()),
                () -> assertEquals(expectedSize, manager.getHistory().size())
        );
    }

    @Test
    void shouldLoadEpicWithoutSubtasks() {
        final String fileContent = "id,type,name,status,description,startTime,duration,endTime,epic\n" +
                "1,EPIC,Epic,NEW,Epic description,2023-07-17T10:00,PT30M,2023-07-17T10:30,\n";
        try (PrintWriter writer = new PrintWriter(PATH)) {
            writer.write(fileContent);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
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

    @Test
    void shouldAddTask() {
        super.addTask();
    }

    @Test
    void shouldNotAddNullTask() {
        super.addNullTask();
    }

    @Test
    void shouldAddTaskWithIncorrectId() {
        super.addTaskWithIncorrectId();
    }

    @Test
    void shouldAddEpicWithNoSubtaskAndSetNewStatus() {
        super.addEpicWithNoSubtasks();
    }

    @Test
    void shouldAddEpicWithNewSubtaskAndSetNewStatus() {
        super.addEpicWithNewSubtask();
    }

    @Test
    void shouldAddEpicWithDoneSubtaskAndSetDoneStatus() {
        super.addEpicWithDoneSubtask();
    }

    @Test
    void shouldAddEpicWithInProgressSubtaskAndSetInProgressStatus() {
        final int expectedSize = 1;
        super.addEpicWithInProgressSubtask();
        assertAll(
                () -> assertEquals(expectedSize, manager.getAllEpics().size()),
                () -> assertEquals(Status.IN_PROGRESS, epic.getStatus())
        );
    }

    @Test
    void shouldNotAddNullEpic() {
        super.addNullEpic();
    }

    @Test
    void shouldAddEpicWithIncorrectId() {
        super.addEpicWithIncorrectId();
    }

    @Test
    void shouldAddSubtask() {
        super.addSubtaskWithEpicId();
    }

    @Test
    void shouldNotAddNullSubtask() {
        super.addNullSubtask();
    }

    @Test
    void shouldNotAddSubtaskWithIncorrectEpicId() {
        super.addSubtaskWithIncorrectEpicId();
    }

    @Test
    void shouldReturnExistingTask() {
        super.getExistingTask();
    }

    @Test
    void shouldReturnNullTask() {
        super.getNonExistingTask();
    }

    @Test
    void shouldReturnExistingEpic() {
        super.getExistingEpic();
    }

    @Test
    void shouldReturnNullEpic() {
        super.getNonExistingEpic();
    }

    @Test
    void shouldReturnExistingSubtask() {
        super.getExistingSubtask();
    }

    @Test
    void shouldReturnNullSubtask() {
        super.getNonExistingSubtask();
    }

    @Test
    void shouldUpdateTask() {
        super.updateTaskWithSameId();
    }

    @Test
    void shouldNotUpdateTaskWithDifferentId() {
        super.updateTaskWithDifferentId();
    }

    @Test
    void shouldUpdateEpicWithNoSubtask() {
        super.updateEpicWithNoSubtask();
    }

    @Test
    void shouldUpdateEpicWithSubtask() {
        super.updateEpicWithSubtask();
    }

    @Test
    void shouldNotUpdateEpicWithWrongId() {
        super.updateEpicWithWrongId();
    }

    @Test
    void shouldUpdateSubtaskWithEpicId() {
        super.updateSubtaskWithEpicId();
    }

    @Test
    void shouldNotUpdateSubtaskWithWrongEpicId() {
        super.updateSubtaskWithWrongEpicId();
    }

    @Test
    void shouldDeleteTaskWithId() {
        super.deleteTaskWithId();
    }

    @Test
    void shouldNotDeleteTaskWithWrongId() {
        super.deleteTaskWithWrongId();
    }

    @Test
    void shouldDeleteEpicWithIdAndNoSubtask() {
        super.deleteEpicWithIdAndNoSubtasks();
    }

    @Test
    void shouldDeleteEpicWithIdAndSubtasks() {
        super.deleteEpicWithIdAndSubtasks();
    }

    @Test
    void shouldNotDeleteEpicWithWrongId() {
        super.deleteEpicWithWrongId();
    }

    @Test
    void shouldDeleteSubtaskWithIdAndEpicId() {
        super.deleteSubtaskWithIdAndEpicId();
    }

    @Test
    void shouldNotDeleteSubtaskWithWrongId() {
        super.deleteSubtaskWithWrongId();
    }

    @Test
    void getAllTasks() {
        super.getAllTasks();
    }

    @Test
    void getAllEpics() {
        super.getAllEpics();
    }

    @Test
    void getAllSubtasks() {
        super.getAllSubtasks();
    }

    @Test
    void getAllEpicSubtasks() {
        super.getAllEpicSubtasks();
    }

    @Test
    void deleteAllTasks() {
        super.deleteAllTasks();
    }

    @Test
    void deleteAllEpics() {
        super.deleteAllEpics();
    }

    @Test
    void deleteAllSubtasks() {
        super.deleteAllSubtasks();
    }

    @Test
    void getHistory() {
        super.getHistory();
    }

    @Test
    void shouldReturnPrioritizedTasks() {
        super.getPrioritizedTasks();
    }

    @Test
    void shouldUpdatePrioritizedTasks() {
        super.updatePrioritizedTasks();
    }

    @Test
    void shouldRemovePrioritizedTasks() {
        super.removePrioritizedTasks();
    }
}