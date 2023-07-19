package ru.yandex.storage;

import ru.yandex.model.Task;
import ru.yandex.storage.interfaces.PriorityStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
                Comparator.comparing(
                        t -> t.getStartTime().orElse(null),
                        Comparator.nullsLast(Comparator.naturalOrder())
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
        List<Task> tasksWithTime = priority.stream()
                .filter(t -> Objects.nonNull(task.getStartTime().orElse(null)))
                .collect(Collectors.toList());
        if (tasksWithTime.size() == 0) return;
        if (isBoundaryTime(tasksWithTime, task)) return;
        if (isEqualToBoundaryTime(tasksWithTime, task)) {
            task.setStartTime(null);
            return;
        }
        boolean isCrossing = true;
        LocalDateTime taskEndTime = task.getEndTime().orElseThrow();
        for (int i = 0; i < tasksWithTime.size() - 1; i++) {
            LocalDateTime mustBeAfterThisTime = tasksWithTime.get(i).getEndTime().orElseThrow();
            LocalDateTime mustBeBeforeThisTime = tasksWithTime.get(i + 1).getStartTime().orElseThrow();
            if (taskStartTime.isAfter(mustBeAfterThisTime) && taskEndTime.isBefore(mustBeBeforeThisTime)) {
                isCrossing = false;
                break;
            }
        }
        if (isCrossing) task.setStartTime(null);
    }

    private boolean isBoundaryTime(List<Task> tasksWithTime, Task task) {
        LocalDateTime taskStartTime = task.getStartTime().orElseThrow();
        LocalDateTime taskEndTime = task.getEndTime().orElseThrow();
        LocalDateTime firstTaskStartTime = tasksWithTime.get(0).getStartTime().orElseThrow();
        LocalDateTime lastTaskEndTime = tasksWithTime.get(tasksWithTime.size() - 1).getEndTime().orElseThrow();
        return taskEndTime.isBefore(firstTaskStartTime) || taskStartTime.isAfter(lastTaskEndTime);
    }

    private boolean isEqualToBoundaryTime(List<Task> tasksWithTime, Task task) {
        LocalDateTime taskStartTime = task.getStartTime().orElseThrow();
        LocalDateTime taskEndTime = task.getEndTime().orElseThrow();
        LocalDateTime firstTaskStartTime = tasksWithTime.get(0).getStartTime().orElseThrow();
        LocalDateTime lastTaskEndTime = tasksWithTime.get(tasksWithTime.size() - 1).getEndTime().orElseThrow();
        return taskEndTime.isEqual(firstTaskStartTime) || taskStartTime.isEqual(lastTaskEndTime);
    }
}
