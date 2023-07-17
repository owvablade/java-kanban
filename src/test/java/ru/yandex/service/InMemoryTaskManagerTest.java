package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void beforeEach() {
        manager = new InMemoryTaskManager();
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
        super.addEpicWithInProgressSubtask();
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
