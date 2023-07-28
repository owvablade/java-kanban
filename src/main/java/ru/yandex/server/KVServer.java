package ru.yandex.server;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class KVServer {
    public static final int PORT = 8078;
    private final String apiToken;
    private final HttpServer server;
    private final Map<String, String> data = new HashMap<>();

    public KVServer() throws IOException {
        apiToken = generateApiToken();
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/register", this::register);
        server.createContext("/save", this::save);
        server.createContext("/load", this::load);
        server.createContext("/delete", this::delete);
    }

    private void load(HttpExchange h) throws IOException {
        try {
            if (!hasAuth(h)) {
                h.sendResponseHeaders(403, 0);
                return;
            }
            if ("GET".equals(h.getRequestMethod())) {
                String key = h.getRequestURI().getPath().substring("/load/".length());
                if (key.isEmpty()) {
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                if ("ids".equals(key)) {
                    StringBuilder sb = new StringBuilder();
                    for (String currentKey : data.keySet()) {
                        sb.append(currentKey).append(",");
                    }
                    if (sb.length() == 0) {
                        h.sendResponseHeaders(400, 0);
                        return;
                    }
                    sendText(h, sb.deleteCharAt(sb.length() - 1).toString());
                    return;
                }
                String value = data.get(key);
                if (value == null) {
                    h.sendResponseHeaders(204, 0);
                    return;
                }
                sendText(h, value);
            } else {
                h.sendResponseHeaders(405, 0);
            }
        } finally {
            h.close();
        }
    }

    private void save(HttpExchange h) throws IOException {
        try {
            if (!hasAuth(h)) {
                h.sendResponseHeaders(403, 0);
                return;
            }
            if ("POST".equals(h.getRequestMethod())) {
                String key = h.getRequestURI().getPath().substring("/save/".length());
                if (key.isEmpty()) {
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                String value = readText(h);
                if (value.isEmpty()) {
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                data.put(key, value);
                h.sendResponseHeaders(200, 0);
            } else {
                h.sendResponseHeaders(405, 0);
            }
        } finally {
            h.close();
        }
    }

    private void delete(HttpExchange h) throws IOException {
        try {
            if (!hasAuth(h)) {
                h.sendResponseHeaders(403, 0);
                return;
            }
            if ("DELETE".equals(h.getRequestMethod())) {
                String key = h.getRequestURI().getPath().substring("/delete/".length());
                if (key.isEmpty()) {
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                String removeValue = data.remove(key);
                if (removeValue == null) {
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                h.sendResponseHeaders(200, 0);
            } else {
                h.sendResponseHeaders(405, 0);
            }
        } finally {
            h.close();
        }
    }

    private void register(HttpExchange h) throws IOException {
        try {
            if ("GET".equals(h.getRequestMethod())) {
                sendText(h, apiToken);
            } else {
                h.sendResponseHeaders(405, 0);
            }
        } finally {
            h.close();
        }
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private String generateApiToken() {
        return "" + System.currentTimeMillis();
    }

    protected boolean hasAuth(HttpExchange h) {
        String rawQuery = h.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_TOKEN=" + apiToken) || rawQuery.contains("API_TOKEN=DEBUG"));
    }

    protected String readText(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), UTF_8);
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }
}
