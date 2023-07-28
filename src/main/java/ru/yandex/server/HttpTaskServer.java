package ru.yandex.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.adapter.DurationAdapter;
import ru.yandex.adapter.LocalDateAdapter;
import ru.yandex.model.*;
import ru.yandex.server.handlers.EpicHandler;
import ru.yandex.server.handlers.SubtaskHandler;
import ru.yandex.server.handlers.TaskHandler;
import ru.yandex.server.handlers.util.ServerUtil;
import ru.yandex.service.HttpTaskManager;
import ru.yandex.util.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final Gson gson;
    private final HttpServer server;
    private final HttpTaskManager manager;

    public HttpTaskServer(String url) throws IOException, InterruptedException {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter().nullSafe())
                .registerTypeAdapter(Duration.class, new DurationAdapter().nullSafe())
                .create();
        manager = Managers.getDefault(url);
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", this::handlePrioritizedTasks);
        server.createContext("/tasks/task", new TaskHandler(manager, gson));
        server.createContext("/tasks/epic", new EpicHandler(manager, gson));
        server.createContext("/tasks/subtask", new SubtaskHandler(manager, gson));
        server.createContext("/tasks/history", this::handleHistory);
    }

    private void handlePrioritizedTasks(HttpExchange exchange) throws IOException {
        String response;
        int responseCode;
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Task> tasks = manager.getPrioritizedTasks();
            response = gson.toJson(tasks);
            responseCode = 200;
        } else {
            response = "Ожидался GET запрос для получения всех задач";
            responseCode = 400;
        }
        ServerUtil.writeResponse(exchange, response, responseCode);
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        String response;
        int responseCode;
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Task> tasks = manager.getHistory();
            response = gson.toJson(tasks);
            responseCode = 200;
        } else {
            response = "Ожидался GET запрос для получения истории";
            responseCode = 400;
        }
        ServerUtil.writeResponse(exchange, response, responseCode);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
