import ru.yandex.kanban.model.*;
import ru.yandex.kanban.service.Manager;

import java.util.ArrayList;
import java.util.Random;

public class Main {

    private static final Random random = new Random();
    private static final Manager manager = new Manager();

    public static void main(String[] args) {
        Task task1 = new Task()
                .setId(random.nextInt())
                .setName("Пройти 3 спринт")
                .setStatus(Task.getInProgressStatus())
                .setDescription("ЯП");
        Task task2 = new Task()
                .setId(random.nextInt())
                .setName("Помыть посуду")
                .setStatus(Task.getNewStatus())
                .setDescription("Дом");
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = (Epic) new Epic()
                .setId(random.nextInt())
                .setName("Сделать ФП4")
                .setStatus(Task.getNewStatus())
                .setDescription("ЯП");
        Subtask epic1subtask1 = (Subtask) new Subtask()
                .setId(random.nextInt())
                .setName("Декомпозировать задачу")
                .setStatus(Task.getDoneStatus())
                .setDescription("ЯП ФП");
        Subtask epic1subtask2 = (Subtask) new Subtask()
                .setId(random.nextInt())
                .setName("Написать код")
                .setStatus(Task.getInProgressStatus())
                .setDescription("ЯП ФП");
        manager.addEpic(epic1);
        epic1subtask1.setEpicId(epic1.getId());
        epic1subtask2.setEpicId(epic1.getId());
        manager.addSubtask(epic1subtask1);
        manager.addSubtask(epic1subtask2);

        Epic epic2 = (Epic) new Epic()
                .setId(random.nextInt())
                .setName("Приготовить еду")
                .setStatus(Task.getDoneStatus())
                .setDescription("Дом");
        Subtask epic2subtask1 = (Subtask) new Subtask()
                .setId(random.nextInt())
                .setName("Купить продукты")
                .setStatus(Task.getNewStatus())
                .setDescription("Дом");
        manager.addEpic(epic2);
        epic2subtask1.setEpicId(epic2.getId());
        manager.addSubtask(epic2subtask1);

        printAllTasks();

        System.out.println("-------------------------------------------------------------");
        System.out.println("Update");

        Task newTask2 = new Task()
                .setId(task2.getId())
                .setName(task2.getName())
                .setStatus(Task.getDoneStatus())
                .setDescription(task2.getDescription());
        manager.updateTask(newTask2);

        Subtask newEpic1Subtask2 = (Subtask) new Subtask()
                .setId(epic1subtask2.getId())
                .setName("Написать код")
                .setStatus(Task.getDoneStatus())
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
            ArrayList<Subtask> subtasks = manager.getAllEpicSubtasks(epic.getId());
            System.out.println(epic);
            System.out.println("Epic subtasks:");
            for (Subtask subtask : subtasks) {
                System.out.println("\t" + subtask);
            }
        }
    }

    private static void printAllTasks() {
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
    }
}
