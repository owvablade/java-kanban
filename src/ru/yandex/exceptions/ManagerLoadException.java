package ru.yandex.exceptions;

public class ManagerLoadException extends RuntimeException {

    public ManagerLoadException(final String message) {
        super(message);
    }
}
