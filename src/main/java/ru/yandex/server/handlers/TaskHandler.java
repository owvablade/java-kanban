package ru.yandex.server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.model.Task;
import ru.yandex.server.handlers.model.Endpoint;
import ru.yandex.server.handlers.util.HandlerUtil;
import ru.yandex.server.handlers.util.ServerUtil;
import ru.yandex.service.HttpTaskManager;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler implements HttpHandler {

    private final Gson gson;
    private final HttpTaskManager manager;

    public TaskHandler(HttpTaskManager manager, Gson gson) {
        this.gson = gson;
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getTaskEndpoint(exchange.getRequestURI(), exchange.getRequestMethod());
        switch (endpoint) {
            case GET_TASK:
                getTask(exchange);
                break;
            case ADD_TASK:
                addTask(exchange);
                break;
            case UPDATE_TASK:
                updateTask(exchange);
                break;
            case DELETE_TASK:
                deleteTask(exchange);
                break;
            case GET_ALL_TASKS:
                getAllTasks(exchange);
                break;
            case DELETE_ALL_TASKS:
                deleteAllTasks(exchange);
                break;
            default:
                ServerUtil.writeResponse(exchange, "Неизвестный запрос", 404);
                break;
        }
    }

    private Endpoint getTaskEndpoint(URI uri, String requestMethod) {
        String query = uri.getQuery();
        String[] pathParts = uri.getPath().split("/");
        if ("GET".equals(requestMethod)) {
            if (query == null) {
                return Endpoint.GET_ALL_TASKS;
            } else {
                return Endpoint.GET_TASK;
            }
        } else if ("POST".equals(requestMethod)) {
            if (pathParts.length == 4 && "add".equals(pathParts[3])) {
                return Endpoint.ADD_TASK;
            } else if (pathParts.length == 4 && "update".equals(pathParts[3])) {
                return Endpoint.UPDATE_TASK;
            } else {
                return Endpoint.UNKNOWN;
            }
        } else if ("DELETE".equals(requestMethod)) {
            if (query == null) {
                return Endpoint.DELETE_ALL_TASKS;
            } else {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private void getTask(HttpExchange exchange) throws IOException {
        Integer id = HandlerUtil.getId(exchange).orElse(null);
        if (id == null) {
            ServerUtil.writeResponse(exchange, "Неверное id для Task", 400);
            return;
        }
        Task task = manager.getTask(id);
        if (task == null) {
            ServerUtil.writeResponse(exchange, "Task с таким id не существует", 400);
            return;
        }
        String response = gson.toJson(task);
        ServerUtil.writeResponse(exchange, response, 200);
    }

    private void addTask(HttpExchange exchange) throws IOException {
        Task task = parseTaskJson(exchange);
        if (task == null) {
            ServerUtil.writeResponse(exchange, "Получен некорректный JSON", 400);
            return;
        }
        manager.addTask(task);
        ServerUtil.writeResponse(exchange, "Task успешно добавлена", 201);
    }

    private void updateTask(HttpExchange exchange) throws IOException {
        Task task = parseTaskJson(exchange);
        if (task == null) {
            ServerUtil.writeResponse(exchange, "Получен некорректный JSON", 400);
            return;
        }
        if (manager.containsTask(task.getId())) {
            manager.updateTask(task);
            ServerUtil.writeResponse(exchange, "Task успешно обновлена", 200);
        } else {
            ServerUtil.writeResponse(exchange, "Task c id= " + task.getId() + "не найден", 200);
        }
    }

    private void deleteTask(HttpExchange exchange) throws IOException {
        Integer id = HandlerUtil.getId(exchange).orElse(null);
        if (id == null) {
            ServerUtil.writeResponse(exchange, "Неверное id для Task", 400);
            return;
        }
        if (manager.getTask(id) == null) {
            ServerUtil.writeResponse(exchange, "Task с таким id не существует", 400);
            return;
        }
        manager.deleteTask(id);
        ServerUtil.writeResponse(exchange, "Task с id=" + id + " успешно удалена", 200);
    }

    private void getAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = manager.getAllTasks();
        String response = gson.toJson(tasks);
        ServerUtil.writeResponse(exchange, response, 200);
    }

    private void deleteAllTasks(HttpExchange exchange) throws IOException {
        manager.deleteAllTasks();
        ServerUtil.writeResponse(exchange, "Все Task успешно удалены", 200);
    }

    private Task parseTaskJson(HttpExchange exchange) throws IOException {
        String jsonTask = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task;
        try {
            task = gson.fromJson(jsonTask, Task.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
        return task;
    }
}
