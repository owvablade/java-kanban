import ru.yandex.model.*;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.util.Managers;

import java.util.*;

public class Main {

    private static final Random random = new Random();
    private static final TaskManager manager = Managers.getDefaultManager();

    public static void main(String[] args) {
        Task task1 = new Task()
                .setId(random.nextInt())
                .setName("Пройти 5 спринт")
                .setStatus(Status.IN_PROGRESS)
                .setDescription("ЯП");
        Task task2 = new Task()
                .setId(random.nextInt())
                .setName("Помыть посуду")
                .setStatus(Status.NEW)
                .setDescription("Дом");
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = (Epic) new Epic()
                .setId(random.nextInt())
                .setName("Сделать ФП5")
                .setStatus(Status.NEW)
                .setDescription("ЯП");
        Subtask epic1subtask1 = (Subtask) new Subtask()
                .setId(random.nextInt())
                .setName("Декомпозировать задачу")
                .setStatus(Status.DONE)
                .setDescription("ЯП ФП");
        Subtask epic1subtask2 = (Subtask) new Subtask()
                .setId(random.nextInt())
                .setName("Написать код")
                .setStatus(Status.IN_PROGRESS)
                .setDescription("ЯП ФП");
        Subtask epic1subtask3 = (Subtask) new Subtask()
                .setId(random.nextInt())
                .setName("Отправить решение")
                .setStatus(Status.NEW)
                .setDescription("ЯП ФП");
        manager.addEpic(epic1);
        epic1subtask1.setEpicId(epic1.getId());
        epic1subtask2.setEpicId(epic1.getId());
        epic1subtask3.setEpicId(epic1.getId());
        manager.addSubtask(epic1subtask1);
        manager.addSubtask(epic1subtask2);
        manager.addSubtask(epic1subtask3);

        Epic epic2 = (Epic) new Epic()
                .setId(random.nextInt())
                .setName("Приготовить еду")
                .setStatus(Status.IN_PROGRESS)
                .setDescription("Дом");
        manager.addEpic(epic2);
        manager.getEpic(epic2.getId());

        printAllTasks();

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(epic1subtask1.getId());
        manager.getSubtask(epic1subtask2.getId());
        manager.getSubtask(epic1subtask3.getId());
        manager.getEpic(epic2.getId());

        printHistory();

        manager.getEpic(epic2.getId());
        manager.getTask(task1.getId());

        printHistory();

        manager.getSubtask(epic1subtask2.getId());
        manager.getEpic(epic2.getId());
        manager.getTask(task2.getId());
        manager.getEpic(epic1.getId());

        printHistory();

        manager.deleteTask(task2.getId());

        printHistory();

        manager.deleteSubtask(epic1subtask1.getId());

        printHistory();

        manager.deleteEpic(epic1.getId());

        printHistory();

        manager.deleteAllTasks();
        manager.deleteAllEpics();

        printHistory();
    }

    private static void printAllTasks() {
        System.out.println("-------------------------------------------------------------");
        System.out.println("All tasks");
        System.out.println("Tasks: ");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Epics: ");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("Subtasks: ");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
        System.out.println("-------------------------------------------------------------");
    }

    private static void printHistory() {
        System.out.println("-------------------------------------------------------------");
        System.out.println("History:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
        System.out.println("-------------------------------------------------------------");
    }
}
