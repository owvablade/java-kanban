package ru.yandex.exceptions;

import java.io.IOException;

public class ManagerLoadException extends IOException {

    public ManagerLoadException(final String message) {
        super(message);
    }
}
