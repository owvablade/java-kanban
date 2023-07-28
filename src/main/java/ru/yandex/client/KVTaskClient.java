package ru.yandex.client;

import ru.yandex.client.interfaces.TaskClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class KVTaskClient implements TaskClient {

    private final String url;
    private final String apiToken;
    private final HttpClient client;

    public KVTaskClient(String url) throws IOException, InterruptedException {
        this.url = url;
        this.client = HttpClient.newHttpClient();
        this.apiToken = getApiToken();
    }

    private String getApiToken() throws IOException, InterruptedException {
        URI uri = URI.create(url + "/register");
        return sendGetRequest(uri);
    }

    @Override
    public void put(String key, String json) throws IOException, InterruptedException {
        String putUrl = url + String.format("/save/%s?API_TOKEN=%s", key, apiToken);
        URI uri = URI.create(putUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public String load(String key) throws IOException, InterruptedException {
        String loadUrl = url + String.format("/load/%s?API_TOKEN=%s", key, apiToken);
        URI uri = URI.create(loadUrl);
        return sendGetRequest(uri);
    }

    @Override
    public void delete(String key) throws IOException, InterruptedException {
        String deleteUrl = url + String.format("/delete/%s?API_TOKEN=%s", key, apiToken);
        URI uri = URI.create(deleteUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String sendGetRequest(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, handler);
        if (response.statusCode() == 200) {
            return response.body();
        }
        return null;
    }
}
