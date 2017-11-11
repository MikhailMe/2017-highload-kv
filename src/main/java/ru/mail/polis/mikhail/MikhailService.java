package ru.mail.polis.mikhail;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.mikhail.DAO.MyDAO;
import ru.mail.polis.mikhail.DAO.MyFileDAO;
import ru.mail.polis.mikhail.Topology.MikhailTopology;
import ru.mail.polis.mikhail.Topology.Topology;
import ru.mail.polis.mikhail.handlers.StatusHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
import java.util.Set;

public class MikhailService implements KVService {

    @NotNull
    private final HttpServer server;
    @NotNull
    private final MyDAO dao;
    @NotNull
    private final Topology topology;

    private final static String ID = "id=";
    private final static String ADDRESS = "address=";
    private final static String REPLICAS = "replicas=";
    private final static String DELIMETER = "/";
    private final static String PATH_STATUS = "/v0/status";
    private final static String PATH_ENTITY = "/v0/entity";
    private final static String GET_REQUEST = "GET";
    private final static String PUT_REQUEST = "PUT";
    private final static String DELETE_REQUEST = "DELETE";


    public MikhailService(@NotNull int port,
                          @NotNull final File data,
                          @NotNull final Set<String> topology)
            throws IOException {
        // создали сервер на нужном порту
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        // задаём дао
        this.dao = new MyFileDAO(data);
        // задаём топологию
        this.topology = new MikhailTopology(topology);

        this.server.createContext(PATH_STATUS, new StatusHandler());

        // вешаем обработчик на сервер
        this.server.createContext(PATH_ENTITY, http -> {
            // достали id
            String id;
            try {
                id = extractId(http.getRequestURI().getQuery());
            } catch (IllegalArgumentException e) {
                http.sendResponseHeaders(Code.CODE_BAD_REQUEST.getCode(), 0);
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
                    http.sendResponseHeaders(Code.CODE_SERVICE_UNAVAILABLE.getCode(), 0);
                    break;
            }
            http.close();
        });
    }

    // достаём id
    @NotNull
    private static String extractId(@NotNull final String query)
            throws IllegalArgumentException{
        if (!query.startsWith(ID))
            throw new IllegalArgumentException("wrong string");
        String id = query.substring(ID.length());
        if (id.isEmpty())
            throw new IllegalArgumentException("id is empty");
        return id;
    }

    private void get(@NotNull HttpExchange http,
                     @NotNull String id)
            throws IOException {
        try {
            final byte[] getValue = dao.get(id);
            http.sendResponseHeaders(Code.CODE_OK.getCode(), getValue.length);
            http.getResponseBody().write(getValue);
        } catch (IOException e) {
            http.sendResponseHeaders(Code.CODE_NOT_FOUND.getCode(), 0);
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
            http.sendResponseHeaders(Code.CODE_CREATED.getCode(), contentLength);
            http.getResponseBody().write(putValue);
        } catch (IllegalArgumentException e) {
            http.sendResponseHeaders(Code.CODE_BAD_REQUEST.getCode(), 0);
            http.close();
        } catch (NoSuchElementException e) {
            http.sendResponseHeaders(Code.CODE_NOT_FOUND.getCode(), 0);
            http.close();
        }
    }

    private void delete(@NotNull HttpExchange http,
                        @NotNull String id)
            throws IOException{
        dao.delete(id);
        http.sendResponseHeaders(Code.CODE_ACCEPTED.getCode(), 0);
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
