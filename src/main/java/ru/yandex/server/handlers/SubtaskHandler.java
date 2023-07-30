package ru.yandex.server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.model.Subtask;
import ru.yandex.server.handlers.model.Endpoint;
import ru.yandex.server.handlers.util.HandlerUtil;
import ru.yandex.server.handlers.util.ServerUtil;
import ru.yandex.service.HttpTaskManager;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler implements HttpHandler {

    private final Gson gson;
    private final HttpTaskManager manager;

    public SubtaskHandler(HttpTaskManager manager, Gson gson) {
        this.gson = gson;
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getSubtaskEndpoint(exchange.getRequestURI(), exchange.getRequestMethod());
        switch (endpoint) {
            case GET_SUBTASK:
                getSubtask(exchange);
                break;
            case ADD_SUBTASK:
                addSubtask(exchange);
                break;
            case UPDATE_SUBTASK:
                updateSubtask(exchange);
                break;
            case DELETE_SUBTASK:
                deleteSubtask(exchange);
                break;
            case GET_ALL_SUBTASKS:
                getAllSubtasks(exchange);
                break;
            case DELETE_ALL_SUBTASKS:
                deleteAllSubtasks(exchange);
                break;
            default:
                ServerUtil.writeResponse(exchange, "Неизвестный запрос", 404);
                break;
        }
    }

    private Endpoint getSubtaskEndpoint(URI uri, String requestMethod) {
        String query = uri.getQuery();
        String[] pathParts = uri.getPath().split("/");
        if ("GET".equals(requestMethod)) {
            if (query == null) {
                return Endpoint.GET_ALL_SUBTASKS;
            } else {
                return Endpoint.GET_SUBTASK;
            }
        } else if ("POST".equals(requestMethod)) {
            if (pathParts.length == 4 && "add".equals(pathParts[3])) {
                return Endpoint.ADD_SUBTASK;
            } else if (pathParts.length == 4 && "update".equals(pathParts[3])) {
                return Endpoint.UPDATE_SUBTASK;
            } else {
                return Endpoint.UNKNOWN;
            }
        } else if ("DELETE".equals(requestMethod)) {
            if (query == null) {
                return Endpoint.DELETE_ALL_SUBTASKS;
            } else {
                return Endpoint.DELETE_SUBTASK;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private void getSubtask(HttpExchange exchange) throws IOException {
        Integer id = HandlerUtil.getId(exchange).orElse(null);
        if (id == null) {
            ServerUtil.writeResponse(exchange, "Неверное id для Subtask", 400);
            return;
        }
        Subtask subtask = manager.getSubtask(id);
        if (subtask == null) {
            ServerUtil.writeResponse(exchange, "Subtask с таким id не существует", 400);
            return;
        }
        String response = gson.toJson(subtask);
        ServerUtil.writeResponse(exchange, response, 200);
    }

    private void addSubtask(HttpExchange exchange) throws IOException {
        Subtask subtask = getSubtaskAfterChecking(exchange);
        if (subtask == null) return;
        manager.addSubtask(subtask);
        ServerUtil.writeResponse(exchange, "Subtask успешно добавлена", 201);
    }

    private void updateSubtask(HttpExchange exchange) throws IOException {
        Subtask subtask = getSubtaskAfterChecking(exchange);
        if (subtask == null) return;
        if (manager.containsSubtask(subtask.getId())) {
            manager.updateSubtask(subtask);
            ServerUtil.writeResponse(exchange, "Subtask успешно обновлена", 200);
        } else {
            ServerUtil.writeResponse(exchange,
                    "Subtask c id= " + subtask.getId() + "не найден",
                    200);
        }
    }

    private void deleteSubtask(HttpExchange exchange) throws IOException {
        Integer id = HandlerUtil.getId(exchange).orElse(null);
        if (id == null) {
            ServerUtil.writeResponse(exchange, "Неверное id для Subtask", 400);
            return;
        }
        if (manager.getSubtask(id) == null) {
            ServerUtil.writeResponse(exchange, "Subtask с таким id не существует", 400);
            return;
        }
        manager.deleteSubtask(id);
        ServerUtil.writeResponse(exchange, "Subtask с id=" + id + " успешно удалена", 200);
    }

    private void getAllSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = manager.getAllSubtasks();
        String response = gson.toJson(subtasks);
        ServerUtil.writeResponse(exchange, response, 200);
    }

    private void deleteAllSubtasks(HttpExchange exchange) throws IOException {
        manager.deleteAllSubtasks();
        ServerUtil.writeResponse(exchange, "Все Subtask успешно удалены", 200);
    }

    private Subtask parseSubtaskJson(HttpExchange exchange) throws IOException {
        String jsonSubtask = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask;
        try {
            subtask = gson.fromJson(jsonSubtask, Subtask.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
        return subtask;
    }

    private Subtask getSubtaskAfterChecking(HttpExchange exchange) throws IOException {
        Subtask subtask = parseSubtaskJson(exchange);
        if (subtask == null) {
            ServerUtil.writeResponse(exchange, "Получен некорректный JSON", 400);
            return null;
        }
        if (!manager.containsEpic(subtask.getEpicId())) {
            ServerUtil.writeResponse(exchange,
                    "Epic не существует для данного Subtask",
                    400);
            return null;
        }
        return subtask;
    }
}
