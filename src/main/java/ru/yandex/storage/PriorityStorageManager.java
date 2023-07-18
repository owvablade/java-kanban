package ru.yandex.storage;

import ru.yandex.model.Task;
import ru.yandex.storage.interfaces.PriorityStorage;

import java.time.LocalDateTime;
import java.util.*;

public class PriorityStorageManager implements PriorityStorage {

    private final List<Task> priority;

    public PriorityStorageManager() {
        priority = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        checkTaskForCrossingTime(task);
        priority.add(task);
        priority.sort(
                Comparator.nullsLast(
                        Comparator.comparing(t -> t.getStartTime().orElse(null),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                )
        );
    }

    @Override
    public void update(Task task) {
        if (priority.removeIf(t -> t.getId() == task.getId())) {
            add(task);
        }
    }

    @Override
    public void remove(Task task) {
        priority.remove(task);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return priority;
    }

    private void checkTaskForCrossingTime(Task task) {
        LocalDateTime taskStartTime = task.getStartTime().orElse(null);
        if (taskStartTime == null) return;
        for (Task anotherTask : priority) {
            LocalDateTime anotherTaskStartTime = anotherTask.getStartTime().orElse(null);
            if (anotherTaskStartTime == null) return;
            if (taskStartTime.isEqual(anotherTaskStartTime)) {
                task.setStartTime(null);
            }
        }
    }
}
