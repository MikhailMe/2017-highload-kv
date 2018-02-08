package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.DAO.MyDAO;
import ru.mail.polis.mikhail.Helpers.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseHandler implements HttpHandler {

    final static String GET_REQUEST = "GET";
    final static String PUT_REQUEST = "PUT";
    final static String DELETE_REQUEST = "DELETE";

    int code;
    final MyDAO dao;
    final List<String> topology;

    BaseHandler(@NotNull final MyDAO dao, @NotNull final List<String> topology) {
        this.dao = dao;
        this.topology = topology;
    }

    void sendHttpResponse(@NotNull HttpExchange http, Response response) throws IOException {
        int code = response.getCode();
        byte[] value = response.getValue();
        if (response.hasValue()) {
            http.sendResponseHeaders(code, value.length);
            http.getResponseBody().write(response.getValue());
        } else {
            http.sendResponseHeaders(code, 0);
        }
        http.getResponseBody().close();
        http.close();
    }

    @NotNull
    List<String> getNodesById(@NotNull final String id, int from) {
        List<String> nodes = new ArrayList<>();
        int hash = Math.abs(id.hashCode());
        for (int i = 0; i < from; i++) {
            int index = (hash + i) % topology.size();
            nodes.add(topology.get(index));
        }
        return nodes;
    }

    byte[] getByteArray(@NotNull InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            for (int len; (len = is.read(buffer, 0, 1024)) != -1; ) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            System.out.println(baos.size());
            return baos.toByteArray();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }
}
