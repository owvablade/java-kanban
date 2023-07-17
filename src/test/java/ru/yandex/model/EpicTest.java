package ru.yandex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.service.StatusChecker;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private static final LocalDateTime START_TIME
            = LocalDateTime.of(2023, 7, 17, 10, 0);
    private static Epic epic;
    private static Subtask firstSubtask;
    private static Subtask secondSubtask;

    @BeforeEach
    void startup() {
        epic = (Epic) new Epic(START_TIME, 20)
                .setId(1)
                .setName("Epic")
                .setDescription("Epic description");
        firstSubtask = (Subtask) new Subtask(START_TIME, 20)
                .setEpicId(1)
                .setId(2)
                .setName("First subtask")
                .setDescription("First subtask description");
        secondSubtask = (Subtask) new Subtask(START_TIME, 30)
                .setEpicId(1)
                .setId(3)
                .setName("Second subtask")
                .setDescription("Second subtask description");
    }

    @Test
    void getSubtasks() {
        final int expectedSize = 2;
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        final int actualSize = epic.getSubtasks().size();
        assertEquals(expectedSize, actualSize);
    }

    @Test
    void setSubtasks() {
        final int expectedSize = 2;
        List<Subtask> subtasks = List.of(firstSubtask, secondSubtask);
        epic.setSubtasks(subtasks);
        final int actualSize = epic.getSubtasks().size();
        assertEquals(expectedSize, actualSize);
    }

    @Test
    void addSubtask() {
        final int expectedSize = 1;
        epic.addSubtask(firstSubtask);
        final int actualSize = epic.getSubtasks().size();
        assertEquals(expectedSize, actualSize);
    }

    @Test
    void changeSubtask() {
        final String expectedName = secondSubtask.getName();
        epic.addSubtask(firstSubtask);
        secondSubtask.setId(firstSubtask.getId());
        epic.changeSubtask(secondSubtask);
        final String actualName = epic.getSubtasks().get(0).getName();
        assertEquals(expectedName, actualName);
    }

    @Test
    void deleteSubtask() {
        final int expectedSize = 1;
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        epic.deleteSubtask(firstSubtask);
        final int actualSize = epic.getSubtasks().size();
        assertEquals(expectedSize, actualSize);
    }

    @Test
    void shouldBeNewWithNoSubtasks() {
        StatusChecker.checkEpicStatus(epic);
        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void shouldBeNewWithAllNewSubtasks() {
        firstSubtask.setStatus(Status.NEW);
        secondSubtask.setStatus(Status.NEW);
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        StatusChecker.checkEpicStatus(epic);
        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void shouldBeDoneWithAllDoneSubtasks() {
        firstSubtask.setStatus(Status.DONE);
        secondSubtask.setStatus(Status.DONE);
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        StatusChecker.checkEpicStatus(epic);
        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void shouldBeInProgressWithAtLeastOneInProgressSubtask() {
        firstSubtask.setStatus(Status.NEW);
        secondSubtask.setStatus(Status.IN_PROGRESS);
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        StatusChecker.checkEpicStatus(epic);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldBeNewWithNewAndDoneSubtasks() {
        firstSubtask.setStatus(Status.NEW);
        secondSubtask.setStatus(Status.DONE);
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        StatusChecker.checkEpicStatus(epic);
        assertEquals(Status.NEW, epic.getStatus());
    }
}