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
                .setName("Пройти 3 спринт")
                .setStatus(Status.IN_PROGRESS)
                .setDescription("ЯП");
        Task task2 = new Task()
                .setId(random.nextInt())
                .setName("Помыть посуду")
                .setStatus(Status.NEW)
                .setDescription("Дом");
        manager.addTask(task1);
        manager.addTask(task2);
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());

        printHistory();

        Epic epic1 = (Epic) new Epic()
                .setId(random.nextInt())
                .setName("Сделать ФП4")
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
        manager.addEpic(epic1);
        manager.getEpic(epic1.getId());
        epic1subtask1.setEpicId(epic1.getId());
        epic1subtask2.setEpicId(epic1.getId());
        manager.addSubtask(epic1subtask1);
        manager.addSubtask(epic1subtask2);
        manager.getSubtask(epic1subtask1.getId());
        manager.getSubtask(epic1subtask2.getId());

        printHistory();

        Epic epic2 = (Epic) new Epic()
                .setId(random.nextInt())
                .setName("Приготовить еду")
                .setStatus(Status.DONE)
                .setDescription("Дом");
        Subtask epic2subtask1 = (Subtask) new Subtask()
                .setId(random.nextInt())
                .setName("Купить продукты")
                .setStatus(Status.NEW)
                .setDescription("Дом");
        manager.addEpic(epic2);
        manager.getEpic(epic2.getId());
        epic2subtask1.setEpicId(epic2.getId());
        manager.addSubtask(epic2subtask1);
        manager.getSubtask(epic2subtask1.getId());

        printHistory();
        printAllTasks();

        System.out.println("Update");

        Task newTask2 = new Task()
                .setId(task2.getId())
                .setName(task2.getName())
                .setStatus(Status.DONE)
                .setDescription(task2.getDescription());
        manager.updateTask(newTask2);

        Subtask newEpic1Subtask2 = (Subtask) new Subtask()
                .setId(epic1subtask2.getId())
                .setName("Написать код")
                .setStatus(Status.DONE)
                .setDescription("ЯП ФП");
        newEpic1Subtask2.setEpicId(epic1.getId());
        manager.updateSubtask(newEpic1Subtask2);

        printAllTasks();

        System.out.println("-------------------------------------------------------------");
        System.out.println("Delete");

        manager.deleteTask(task2.getId());
        manager.deleteEpic(epic2.getId());
        manager.deleteSubtask(epic1subtask1.getId());

        System.out.println("Tasks: ");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Epics: ");
        for (Epic epic : manager.getAllEpics()) {
            List<Subtask> subtasks = manager.getAllEpicSubtasks(epic.getId());
            System.out.println(epic);
            System.out.println("Epic subtasks:");
            for (Subtask subtask : subtasks) {
                System.out.println("\t" + subtask);
            }
        }
        System.out.println("-------------------------------------------------------------");

        Task task3 = new Task()
                .setId(random.nextInt())
                .setName("Пройти 4 спринт")
                .setStatus(Status.IN_PROGRESS)
                .setDescription("ЯП");
        Task task4 = new Task()
                .setId(random.nextInt())
                .setName("Помыть пол")
                .setStatus(Status.NEW)
                .setDescription("Дом");
        Task task5 = new Task()
                .setId(random.nextInt())
                .setName("Пройти 1 модуль")
                .setStatus(Status.IN_PROGRESS)
                .setDescription("ЯП");
        Task task6 = new Task()
                .setId(random.nextInt())
                .setName("Протереть пыль")
                .setStatus(Status.NEW)
                .setDescription("Дом");
        Task task7 = new Task()
                .setId(random.nextInt())
                .setName("Проверить работу истории")
                .setStatus(Status.NEW)
                .setDescription("Дом");
        manager.addTask(task3);
        manager.addTask(task4);
        manager.addTask(task5);
        manager.addTask(task6);
        manager.addTask(task7);
        manager.getTask(task3.getId());
        manager.getTask(task4.getId());
        manager.getTask(task5.getId());
        manager.getTask(task6.getId());
        manager.getTask(task7.getId());

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
