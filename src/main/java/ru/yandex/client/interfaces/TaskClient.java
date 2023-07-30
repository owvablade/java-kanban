package ru.yandex.client.interfaces;

import java.io.IOException;

public interface TaskClient {

    void put(String key, String json) throws IOException, InterruptedException;

    String load(String key) throws IOException, InterruptedException;
}
