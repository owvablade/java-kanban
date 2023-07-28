import ru.yandex.model.*;
import ru.yandex.server.HttpTaskServer;
import ru.yandex.server.KVServer;
import ru.yandex.service.interfaces.TaskManager;
import ru.yandex.util.Managers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class Main {

    private static final Random random = new Random();
    private static final String URL = "http://localhost:8078";
    private static final LocalDateTime START_TIME_1
            = LocalDateTime.of(2023, 7, 17, 10, 0);
    private static final LocalDateTime START_TIME_2
            = LocalDateTime.of(2023, 7, 17, 12, 0);
    private static final LocalDateTime START_TIME_3
            = LocalDateTime.of(2023, 7, 17, 13, 0);

    public static void main(String[] args) throws IOException, InterruptedException {
        new KVServer().start();
        new HttpTaskServer(URL).start();
        TaskManager manager = Managers.getDefault(URL);
        Task task1 = new Task(START_TIME_1, 20)
                .setId(random.nextInt())
                .setName("Task")
                .setStatus(Status.NEW)
                .setDescription("Task description");
        manager.addTask(task1);
        Epic epic1 = (Epic) new Epic(START_TIME_2, 30)
                .setId(random.nextInt())
                .setName("Epic")
                .setStatus(Status.NEW)
                .setDescription("Epic description");
        manager.addEpic(epic1);
        Subtask epic1subtask1 = (Subtask) new Subtask(START_TIME_3, 180)
                .setId(random.nextInt())
                .setName("Subtask")
                .setStatus(Status.NEW)
                .setDescription("Subtask description");
        epic1subtask1.setEpicId(epic1.getId());
        manager.getEpic(epic1.getId());
        manager.getTask(task1.getId());
        manager.addSubtask(epic1subtask1);
    }
}
