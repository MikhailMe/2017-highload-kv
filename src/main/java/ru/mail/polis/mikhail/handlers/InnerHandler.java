package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.Helpers.*;
import ru.mail.polis.mikhail.DAO.MyDAO;

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
            Response response;
            Query query = Parser.getQuery(http.getRequestURI().getQuery(), topology);
            switch (http.getRequestMethod()) {
                case GET_REQUEST:
                    response = get(http, query);
                    break;
                case PUT_REQUEST:
                    final byte[] value = getByteArray(http.getRequestBody());
                    response = put(http, query, value);
                    break;
                case DELETE_REQUEST:
                    response = delete(http, query);
                    break;
                default:
                    response = new Response(Code.CODE_NOT_ALLOWED.getCode(), Message.MES_NOT_ALLOWED.toString());
                    break;
            }
            sendHttpResponse(http, response);
        } catch (IllegalArgumentException e) {
            sendHttpResponse(http, Code.CODE_BAD_REQUEST.getCode(), e.getMessage());
        }
    }

    @NotNull
    @Override
    public Response get(@NotNull HttpExchange http,
                        @NotNull Query query)
            throws IOException {
        try {
            String id = query.getId();
            final byte[] getValue = dao.get(id);
            code = Code.CODE_OK.getCode();
            return new Response(code, getValue);
        } catch (IllegalArgumentException e) {
            code = Code.CODE_BAD_REQUEST.getCode();
            return new Response(code, e.getMessage());
        } catch (NoSuchElementException e) {
            code = Code.CODE_NOT_FOUND.getCode();
            return new Response(code, e.getMessage());
        }
    }

    @NotNull
    @Override
    public Response put(@NotNull HttpExchange http,
                        @NotNull Query query,
                        @NotNull byte[] value) {
        try {
            String id = query.getId();
            dao.upsert(id, value);
            code = Code.CODE_CREATED.getCode();
            return new Response(code);
        } catch (IOException | IllegalArgumentException e) {
            code = Code.CODE_BAD_REQUEST.getCode();
            return new Response(code, e.getMessage());
        }
    }

    @NotNull
    @Override
    public Response delete(@NotNull HttpExchange http,
                           @NotNull Query query)
            throws IOException {
        try {
            String id = query.getId();
            dao.delete(id);
            code = Code.CODE_ACCEPTED.getCode();
            return new Response(code);
        } catch (IllegalArgumentException e) {
            code = Code.CODE_BAD_REQUEST.getCode();
            return new Response(code, e.getMessage());
        }
    }
}
