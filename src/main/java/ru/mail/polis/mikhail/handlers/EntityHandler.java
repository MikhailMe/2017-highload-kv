package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.Code;
import ru.mail.polis.mikhail.DAO.MyDAO;
import ru.mail.polis.mikhail.Parser;
import ru.mail.polis.mikhail.Query;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;

public class EntityHandler extends BaseHandler {

    private final static String GET_REQUEST = "GET";
    private final static String PUT_REQUEST = "PUT";
    private final static String DELETE_REQUEST = "DELETE";

    private final MyDAO dao;
    private final Set<String> topology;

    public EntityHandler(@NotNull MyDAO dao,
                          @NotNull Set<String> topology) {
        this.dao = dao;
        this.topology = topology;
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        // FIXME 
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
            throws IOException {
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
            throws IOException {
        dao.delete(id);
        http.sendResponseHeaders(Code.CODE_ACCEPTED.getCode(), 0);
    }


}
