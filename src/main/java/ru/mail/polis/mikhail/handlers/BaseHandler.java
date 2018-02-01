package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.DAO.MyDAO;
import ru.mail.polis.mikhail.Helpers.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BaseHandler implements HttpHandler {

    final static String GET_REQUEST = "GET";
    final static String PUT_REQUEST = "PUT";
    final static String DELETE_REQUEST = "DELETE";

    int code;
    final MyDAO dao;
    final Set<String> topology;

    BaseHandler(@NotNull final MyDAO dao, @NotNull final Set<String> topology) {
        this.dao = dao;
        this.topology = topology;
    }

    void sendHttpResponse(@NotNull HttpExchange http, int code, String response) throws IOException {
        http.sendResponseHeaders(code, response.getBytes().length);
        http.getResponseBody().write(response.getBytes());
        http.getResponseBody().close();
        http.close();
    }

    void sendHttpResponse(@NotNull HttpExchange http, int code, byte[] response) throws IOException {
        http.sendResponseHeaders(code, response.length);
        http.getResponseBody().write(response);
        http.getResponseBody().close();
        http.close();
    }

    void sendHttpResponse(@NotNull HttpExchange http, Response response) throws IOException {
        int code = response.getCode();
        byte[] value = response.getValue();
        if (response.hasValue()) {
            sendHttpResponse(http, code, new String(value));
        } else {
            http.sendResponseHeaders(code, 0);
        }
        http.close();
    }

    @NotNull
    List<String> getNodesById(@NotNull final String id, int from) {
        List<String> nodes = new ArrayList<>();
        int hash = Math.abs(id.hashCode());
        List<String> temp = new ArrayList<>(topology);
        for (int i = 0; i < from; i++) {
            int idx = (hash + i) % temp.size();
            nodes.add(temp.get(idx));
        }
        return nodes;
    }

    byte[] getByteArray(@NotNull InputStream inputStream) throws IOException {
        int size = 1024;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[size];
            for (int len; (len = inputStream.read(buffer, 0, size)) != -1; ) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos.toByteArray();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }
}
