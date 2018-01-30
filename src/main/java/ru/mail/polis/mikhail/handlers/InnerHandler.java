package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.Helpers.Code;
import ru.mail.polis.mikhail.DAO.MyDAO;
import ru.mail.polis.mikhail.Helpers.Message;
import ru.mail.polis.mikhail.Helpers.Parser;
import ru.mail.polis.mikhail.Helpers.Query;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;

public class InnerHandler extends BaseHandler implements HttpMethods {

    public InnerHandler(@NotNull MyDAO dao, @NotNull Set<String> topology) {
        super(dao, topology);
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        try {
            Query query = Parser.getQuery(http.getRequestURI().getQuery(), topology);
            switch (http.getRequestMethod()) {
                case GET_REQUEST:
                    get(http, query);
                    break;
                case PUT_REQUEST:
                    final byte[] value = getByteArray(http.getRequestBody());
                    put(http, query, value);
                    break;
                case DELETE_REQUEST:
                    delete(http, query);
                    break;
                default:
                    sendHttpResponse(http, Code.CODE_NOT_ALLOWED.getCode(), Message.MES_NOT_ALLOWED.toString());
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendHttpResponse(http, Code.CODE_BAD_REQUEST.getCode(), e.getMessage());
        }
    }

    @Override
    public void get(@NotNull HttpExchange http,
                    @NotNull Query query)
            throws IOException {
        try {
            String id = query.getId();
            final byte[] getValue = dao.get(id);
            sendHttpResponse(http, Code.CODE_OK.getCode(), getValue);
        } catch (IllegalArgumentException e) {
            sendHttpResponse(http, Code.CODE_BAD_REQUEST.getCode(), e.getMessage());
        } catch (NoSuchElementException e) {
            sendHttpResponse(http, Code.CODE_NOT_FOUND.getCode(), e.getMessage());
        }
    }

    @Override
    public void put(@NotNull HttpExchange http,
                    @NotNull Query query,
                    @NotNull byte[] value)
            throws IOException {
        try {
            String id = query.getId();
            dao.upsert(id, value);
            sendHttpResponse(http, Code.CODE_CREATED.getCode(), Message.MES_CREATED.toString());
        } catch (IOException | IllegalArgumentException e) {
            sendHttpResponse(http, Code.CODE_BAD_REQUEST.getCode(), e.getMessage());
        }
    }

    @Override
    public void delete(@NotNull HttpExchange http,
                       @NotNull Query query)
            throws IOException {
        try {
            String id = query.getId();
            dao.delete(id);
            sendHttpResponse(http, Code.CODE_ACCEPTED.getCode(), Message.MES_ACCEPTED.toString());
        } catch (IllegalArgumentException e) {
            sendHttpResponse(http, Code.CODE_BAD_REQUEST.getCode(), e.getMessage());
        }
    }
}
