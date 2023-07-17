package ru.yandex.storage;

import ru.yandex.model.Task;
import ru.yandex.storage.interfaces.PriorityStorage;

import java.time.LocalDateTime;
import java.util.*;

public class PriorityStorageManager implements PriorityStorage {

    private final Set<Task> priority;
    private final List<Task> tasksWithoutStartTime;

    public PriorityStorageManager() {
        tasksWithoutStartTime = new ArrayList<>();
        priority = new TreeSet<>(Comparator.comparing(t -> t.getStartTime().orElseThrow()));
    }

    @Override
    public void add(Task task) {
        if (task.getStartTime().isPresent() && task.getDuration().isPresent()) {
            priority.add(task);
            return;
        }
        tasksWithoutStartTime.add(task);
    }

    @Override
    public void update(Task task) {
        if (priority.removeIf(t -> t.getId() == task.getId()) ||
                tasksWithoutStartTime.removeIf(t -> t.getId() == task.getId())) {
            add(task);
        }
    }

    @Override
    public void remove(Task task) {
        priority.remove(task);
        tasksWithoutStartTime.remove(task);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        List<Task> prioritizedTasks = new LinkedList<>(priority);
        checkTasksForCrossingTime(prioritizedTasks);
        prioritizedTasks.addAll(tasksWithoutStartTime);
        return prioritizedTasks;
    }

    private void checkTasksForCrossingTime(List<Task> tasks) {
        for (int i = 0; i < tasks.size() - 1; i++) {
            LocalDateTime endTimeForCurrentTask = tasks.get(i).getEndTime().orElseThrow();
            LocalDateTime startTimeForNextTask = tasks.get(i + 1).getStartTime().orElseThrow();
            if (endTimeForCurrentTask.isBefore(startTimeForNextTask)) continue;
            tasks.remove(i + 1);
        }
    }
}
