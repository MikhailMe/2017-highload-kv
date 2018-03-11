package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.DAO.MyDAO;
import ru.mail.polis.mikhail.Helpers.Code;
import ru.mail.polis.mikhail.Helpers.Message;
import ru.mail.polis.mikhail.Helpers.Query;
import ru.mail.polis.mikhail.Helpers.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BaseHandler implements HttpHandler {

    static final String GET_REQUEST = "GET";
    static final String PUT_REQUEST = "PUT";
    static final String DELETE_REQUEST = "DELETE";

    static final String ID = "?id=";
    static final String PATH_INNER = "/v0/inner";
    static final String PATH_STATUS = "/v0/status";
    static final String PATH = "http://localhost:";

    @NotNull
    final MyDAO dao;
    @NotNull
    final List<String> topology;
    @NotNull
    private Map<String, List<String>> cache;

    BaseHandler(@NotNull final MyDAO dao, @NotNull final List<String> topology) {
        this.dao = dao;
        this.topology = topology;
        this.cache = new ConcurrentHashMap<>();
    }

    void sendHttpResponse(@NotNull HttpExchange http, Response response) throws IOException {
        AtomicInteger code = new AtomicInteger(response.getCode());
        byte[] value = response.getValue();
        if (response.hasValue()) {
            http.sendResponseHeaders(code.get(), value.length);
            http.getResponseBody().write(response.getValue());
        } else {
            http.sendResponseHeaders(code.get(), 0);
        }
        http.getResponseBody().close();
        http.close();
    }

    @NotNull
    List<String> getNodesById(@NotNull final String id, int from) {
        if (cache.containsKey(id + from)) {
            return cache.get(topology + id + from);
        }
        List<String> nodes = new ArrayList<>();
        int hash = Math.abs(id.hashCode());
        for (int i = 0; i < from; i++) {
            int index = (hash + i) % topology.size();
            nodes.add(topology.get(index));
        }
        cache.put(topology + id + from, nodes);
        return nodes;
    }

    @NotNull
    byte[] getByteArray(@NotNull final InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int len; (len = is.read(buffer, 0, BUFFER_SIZE)) != -1; ) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos.toByteArray();
        }
    }

    @NotNull
    Response choiceMethod(@NotNull final HttpExchange http,
                          @NotNull final Query query,
                          @NotNull final Function<Query, Response> get,
                          @NotNull final MyFunction<Query, byte[], Response> put,
                          @NotNull final Function<Query, Response> delete) throws IOException {
        switch (http.getRequestMethod()) {
            case GET_REQUEST:
                return get.apply(query);
            case PUT_REQUEST:
                final byte[] value = getByteArray(http.getRequestBody());
                return put.apply(query, value);
            case DELETE_REQUEST:
                return delete.apply(query);
            default:
                return new Response(Code.CODE_NOT_ALLOWED.getCode(), Message.MES_NOT_ALLOWED.toString());
        }
    }

    @FunctionalInterface
    interface MyFunction<FirstParam, SecondParam, ReturnType> {
        ReturnType apply(FirstParam param1, SecondParam param2);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // nothing to do
    }
}
