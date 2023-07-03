package ru.yandex.comparators;

import ru.yandex.model.Task;

import java.util.Comparator;

public class TaskIdComparator implements Comparator<Task> {

    @Override
    public int compare(Task o1, Task o2) {
        return o1.getId() - o2.getId();
    }
}
