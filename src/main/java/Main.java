import ru.yandex.model.*;
import ru.yandex.service.FileBackedTaskManager;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.util.Managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Main {

    private static final Random random = new Random();
    private static final String PATH = "src/main/java/ru/yandex/resources/tasks.csv";
    private static TaskManager manager = Managers.getFileBackedManager(PATH);
    private static final LocalDateTime START_TIME_1
            = LocalDateTime.of(2023, 7, 17, 10, 0);
    private static final LocalDateTime START_TIME_2
            = LocalDateTime.of(2023, 7, 17, 12, 0);
    private static final LocalDateTime START_TIME_3
            = LocalDateTime.of(2023, 7, 17, 13, 0);

    public static void main(String[] args) {
        Task task1 = new Task(START_TIME_1, 20)
                .setId(random.nextInt())
                .setName("Task")
                .setStatus(Status.NEW)
                .setDescription("Task description");
        manager.addTask(task1);
        Task task2 = new Task(START_TIME_2, 100)
                .setId(task1.getId())
                .setName("Task")
                .setStatus(Status.NEW)
                .setDescription("Task description");
        manager.deleteTask(task1.getId());

        Epic epic1 = (Epic) new Epic(START_TIME_2, 30)
                .setId(random.nextInt())
                .setName("Epic")
                .setStatus(Status.NEW)
                .setDescription("Epic description");
        manager.addEpic(epic1);
        Subtask epic1subtask1 = (Subtask) new Subtask(START_TIME_3, 40)
                .setId(random.nextInt())
                .setName("Subtask")
                .setStatus(Status.NEW)
                .setDescription("Subtask description")
                .setDuration(Duration.ofMinutes(20));
        epic1subtask1.setEpicId(epic1.getId());
        manager.getEpic(epic1.getId());
        manager.getTask(task1.getId());
        manager.addSubtask(epic1subtask1);

        System.out.println("Содержимое файла tasks.csv после работы main() в Main:");
        try {
            System.out.println(Files.readString(Paths.get(PATH)));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        manager = FileBackedTaskManager.loadFromFile(new File(PATH));
        List<Task> priority = manager.getPrioritizedTasks();
        for (Task task : priority) {
            System.out.println(task);
        }
    }
}
