package ru.yandex.server.handlers.util;

import com.sun.net.httpserver.HttpExchange;

import java.util.Optional;

public class HandlerUtil {

    private HandlerUtil() {

    }

    public static Optional<Integer> getId(HttpExchange exchange) {
        String[] query = exchange.getRequestURI().getQuery().split("=");
        try {
            return Optional.of(Integer.parseInt(query[1]));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }
}
