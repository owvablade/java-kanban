package ru.yandex.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Epic extends Task {

    private List<Subtask> subtasks;
    protected LocalDateTime endTime;

    public Epic() {
        super();
        subtasks = new ArrayList<>();
    }

    public Epic(LocalDateTime startTime, long duration) {
        super(startTime, duration);
        subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        this.getEndTime();
    }

    public void changeSubtask(Subtask subtask) {
        for (int i = 0; i < subtasks.size(); i++) {
            if (subtasks.get(i).getId() == subtask.getId()) {
                subtasks.set(i, subtask);
            }
        }
    }

    public void deleteSubtask(Subtask subtask) {
        subtasks.remove(subtask);
    }

    @Override
    public LocalDateTime getEndTime() {
        startTime = subtasks.stream()
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(startTime);
        endTime = subtasks.stream()
                .map(Task::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(startTime.plus(duration));
        duration = subtasks.stream()
                .map(Task::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
        return endTime.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasks, epic.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", startTime='" + startTime + '\'' +
                ", duration='" + duration + '\'' +
                ", endTime='" + endTime + '\'' +
                ", epicSubtasks=" + Arrays.toString(subtasks.toArray()) +
                '}';
    }
}
