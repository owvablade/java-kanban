package ru.yandex.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public class Task {

    protected int id;
    protected String name;
    protected Status status;
    protected String description;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task() {
    }

    public Task(LocalDateTime startTime, long duration) {
        this.startTime = startTime.truncatedTo(ChronoUnit.SECONDS);
        this.duration = Duration.ofMinutes(duration);
    }

    public int getId() {
        return id;
    }

    public Task setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Task setName(String name) {
        this.name = name;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public Task setStatus(Status status) {
        this.status = status;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Task setDescription(String description) {
        this.description = description;
        return this;
    }

    public Optional<Duration> getDuration() {
        return Optional.ofNullable(duration);
    }

    public Task setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public Task setStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            this.startTime = null;
            return this;
        }
        this.startTime = startTime.truncatedTo(ChronoUnit.SECONDS);
        return this;
    }

    public Optional<LocalDateTime> getEndTime() {
        if (startTime == null) return Optional.empty();
        if (duration == null) return Optional.of(startTime);
        return Optional.of(startTime.plus(duration));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id &&
                Objects.equals(name, task.name) &&
                Objects.equals(status, task.status) &&
                Objects.equals(description, task.description) &&
                Objects.equals(startTime, task.startTime) &&
                Objects.equals(duration, task.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, status, description, startTime, duration);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", startTime='" + startTime + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
