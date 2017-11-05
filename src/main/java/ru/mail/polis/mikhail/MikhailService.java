package ru.mail.polis.mikhail;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

public class MikhailService implements KVService {

    @NotNull
    private final HttpServer server;
    @NotNull
    private final MyDAO dao;

    private final static String PREFIX = "id=";
    private final static String STATUS = "/v0/status";
    private final static String ENTITY = "/v0/entity";
    private final static String GET_REQUEST = "GET";
    private final static String PUT_REQUEST = "PUT";
    private final static String DELETE_REQUEST = "DELETE";


    public MikhailService(int port,
                          @NotNull final MyDAO dao)
            throws IOException {
        // создали сервер на нужном порту
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        // задаём дао
        this.dao = dao;

        this.server.createContext(STATUS, http -> {
            if (GET_REQUEST.equals(http.getRequestMethod())) {
                final String response = "ONLINE";
                http.sendResponseHeaders(Code.OK.getResponseCode(), response.length());
                http.getResponseBody().write(response.getBytes());
            } else {
                http.sendResponseHeaders(Code.NOT_ALLOWED.getResponseCode(), 0);
            }
            http.close();
        });

        // вешаем обработчик на сервер
        this.server.createContext(ENTITY, http -> {
            // достали id
            String id;
            try {
                id = extractId(http.getRequestURI().getQuery());
            } catch (IllegalArgumentException e) {
                http.sendResponseHeaders(Code.BAD_REQUEST.getResponseCode(), 0);
                http.close();
                return;
            }

            switch (http.getRequestMethod()) {
                case GET_REQUEST:
                    get(http, id);
                    break;
                case DELETE_REQUEST:
                    delete(http, id);
                    break;
                case PUT_REQUEST:
                    put(http, id);
                    break;
                default:
                    http.sendResponseHeaders(Code.SERVICE_UNAVAILABLE.getResponseCode(), 0);
                    break;
            }
            http.close();
        });
    }

    // достаём id
    @NotNull
    private static String extractId(@NotNull final String query)
        throws IllegalArgumentException{
        if (!query.startsWith(PREFIX))
            throw new IllegalArgumentException("wrong string");
        String id = query.substring(PREFIX.length());
        if (id.isEmpty())
            throw new IllegalArgumentException("id is empty");
        return id;
    }

    private void get(@NotNull HttpExchange http,
                     @NotNull String id)
            throws IOException {
        try {
            final byte[] getValue = dao.get(id);
            http.sendResponseHeaders(Code.OK.getResponseCode(), getValue.length);
            http.getResponseBody().write(getValue);
        } catch (IOException e) {
            http.sendResponseHeaders(Code.NOT_FOUND.getResponseCode(), 0);
            http.close();
        }
    }

    private void put(@NotNull HttpExchange http,
                     @NotNull String id)
            throws IOException{
        try {
            final int contentLength = Integer.valueOf(http.getRequestHeaders().getFirst("Content-length"));
            final byte[] putValue = new byte[contentLength];
            if (contentLength != 0 && http.getRequestBody().read(putValue) != putValue.length)
                throw new IOException("can't read file at once");
            dao.upsert(id, putValue);
            http.sendResponseHeaders(Code.CREATED.getResponseCode(), contentLength);
            http.getResponseBody().write(putValue);
        } catch (IllegalArgumentException e) {
            http.sendResponseHeaders(Code.BAD_REQUEST.getResponseCode(), 0);
            http.close();
        } catch (NoSuchElementException e) {
            http.sendResponseHeaders(Code.NOT_FOUND.getResponseCode(), 0);
            http.close();
        }
    }

    private void delete(@NotNull HttpExchange http,
                        @NotNull String id)
            throws IOException{
        dao.delete(id);
        http.sendResponseHeaders(Code.ACCEPTED.getResponseCode(), 0);
    }

    @Override
    public void start() {
        this.server.start();
    }

    @Override
    public void stop() {
        //   даёт время запросам, которые начали обработку, завершить обработку
        this.server.stop(0);
    }
}
