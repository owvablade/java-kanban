package ru.yandex.server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.model.Epic;
import ru.yandex.server.handlers.model.Endpoint;
import ru.yandex.server.handlers.util.HandlerUtil;
import ru.yandex.server.handlers.util.ServerUtil;
import ru.yandex.service.interfaces.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicHandler implements HttpHandler {

    private final Gson gson;
    private final TaskManager manager;

    public EpicHandler(TaskManager manager, Gson gson) {
        this.gson = gson;
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEpicEndpoint(exchange.getRequestURI(), exchange.getRequestMethod());
        switch (endpoint) {
            case GET_EPIC:
            case GET_ALL_EPIC_SUBTASKS:
                getEpic(exchange, endpoint);
                break;
            case ADD_UPDATE_EPIC:
                addOrUpdateEpic(exchange);
                break;
            case DELETE_EPIC:
                deleteEpic(exchange);
                break;
            case GET_ALL_EPICS:
                getAllEpics(exchange);
                break;
            case DELETE_ALL_EPICS:
                deleteAllEpics(exchange);
                break;
            default:
                break;
        }
    }

    private Endpoint getEpicEndpoint(URI uri, String requestMethod) {
        String query = uri.getQuery();
        String[] pathParts = uri.getPath().split("/");
        if (pathParts.length == 4 && "subtask".equals(pathParts[3]) && query != null) {
            return Endpoint.GET_ALL_EPIC_SUBTASKS;
        }
        if ("GET".equals(requestMethod)) {
            if (query == null) {
                return Endpoint.GET_ALL_EPICS;
            } else {
                return Endpoint.GET_EPIC;
            }
        } else if ("POST".equals(requestMethod)) {
            return Endpoint.ADD_UPDATE_EPIC;
        } else if ("DELETE".equals(requestMethod)) {
            if (query == null) {
                return Endpoint.DELETE_ALL_EPICS;
            } else {
                return Endpoint.DELETE_EPIC;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private void getEpic(HttpExchange exchange, Endpoint endpoint) throws IOException {
        Integer id = HandlerUtil.getId(exchange).orElse(null);
        if (id == null) {
            ServerUtil.writeResponse(exchange, "Неверное id для Epic", 400);
            return;
        }
        Epic epic = manager.getEpic(id);
        if (epic == null) {
            ServerUtil.writeResponse(exchange, "Epic с таким id не существует", 400);
            return;
        }
        String response;
        if (endpoint == Endpoint.GET_ALL_EPIC_SUBTASKS) {
            response = gson.toJson(epic.getSubtasks());
        } else {
            response = gson.toJson(epic);
        }
        ServerUtil.writeResponse(exchange, response, 200);
    }

    private void addOrUpdateEpic(HttpExchange exchange) throws IOException {
        String jsonTask = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic;
        try {
            epic = gson.fromJson(jsonTask, Epic.class);
        } catch (JsonSyntaxException e) {
            ServerUtil.writeResponse(exchange, "Получен некорректный JSON", 400);
            return;
        }
        if (epic == null) {
            ServerUtil.writeResponse(exchange, "Epic пуст", 400);
            return;
        }
        if (manager.getEpic(epic.getId()) == null) {
            manager.addEpic(epic);
            ServerUtil.writeResponse(exchange, "Epic успешно добавлена", 200);
        } else {
            manager.updateEpic(epic);
            ServerUtil.writeResponse(exchange, "Epic успешно обновлена", 200);
        }
    }

    private void deleteEpic(HttpExchange exchange) throws IOException {
        Integer id = HandlerUtil.getId(exchange).orElse(null);
        if (id == null) {
            ServerUtil.writeResponse(exchange, "Неверное id для Epic", 400);
            return;
        }
        if (manager.getEpic(id) == null) {
            ServerUtil.writeResponse(exchange, "Epic с таким id не существует", 400);
            return;
        }
        manager.deleteEpic(id);
        ServerUtil.writeResponse(exchange, "Epic с id=" + id + " успешно удалена", 200);
    }

    private void getAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = manager.getAllEpics();
        String response = gson.toJson(epics);
        ServerUtil.writeResponse(exchange, response, 200);
    }

    private void deleteAllEpics(HttpExchange exchange) throws IOException {
        manager.deleteAllEpics();
        ServerUtil.writeResponse(exchange, "Все Epic успешно удалены", 200);
    }
}

