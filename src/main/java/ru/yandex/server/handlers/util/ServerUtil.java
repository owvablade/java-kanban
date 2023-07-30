package ru.yandex.server.handlers.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ServerUtil {

    private ServerUtil() {

    }

    public static void writeResponse(HttpExchange exchange,
                                     String responseString,
                                     int responseCode) throws IOException {
        byte[] response = responseString.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(responseCode, response.length);
            os.write(response);
        }
    }
}
