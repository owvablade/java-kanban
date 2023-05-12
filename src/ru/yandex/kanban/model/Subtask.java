package ru.yandex.kanban.model;

import java.util.Objects;

public class Subtask extends Task {

    private int epicId;

    public Subtask() {
    }

    public int getEpicId() {
        return epicId;
    }

    public Subtask setEpicId(int epicId) {
        this.epicId = epicId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", id=" + super.getId() +
                ", name='" + super.getName() + '\'' +
                ", status='" + super.getStatus() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                '}';
    }
}
