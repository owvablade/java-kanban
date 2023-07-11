import ru.yandex.model.*;
import ru.yandex.service.FileBackedTaskManager;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.util.Managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static final Random random = new Random();
    private static final String PATH = "src/main/java/ru/yandex/resources/tasks.csv";
    private static final TaskManager manager = Managers.getFileBackedManager(PATH);

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
        manager.addEpic(epic1);
        Subtask epic1subtask1 = (Subtask) new Subtask()
                .setId(random.nextInt())
                .setName("Декомпозировать задачу")
                .setStatus(Status.DONE)
                .setDescription("ЯП ФП");
        epic1subtask1.setEpicId(epic1.getId());
        manager.addSubtask(epic1subtask1);

        Epic epic2 = (Epic) new Epic()
                .setId(random.nextInt())
                .setName("Приготовить еду")
                .setStatus(Status.IN_PROGRESS)
                .setDescription("Дом");
        manager.addEpic(epic2);

        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());
        manager.getSubtask(epic1subtask1.getId());
        manager.getTask(task2.getId());

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
        epic1subtask2.setEpicId(epic1.getId());
        epic1subtask3.setEpicId(epic1.getId());
        manager.addSubtask(epic1subtask2);
        manager.addSubtask(epic1subtask3);

        System.out.println("Содержимое файла tasks.csv после работы main() в Main:");
        try {
            System.out.println(Files.readString(Paths.get(PATH)));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        FileBackedTaskManager.main(new String[]{});
    }
}
