package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.Helpers.*;
import ru.mail.polis.mikhail.DAO.MyDAO;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class InnerHandler extends BaseHandler implements HttpMethods {

    @NotNull
    private Map<String, byte[]> cache;

    public InnerHandler(@NotNull MyDAO dao, @NotNull List<String> topology) {
        super(dao, topology);
        this.cache = new LinkedHashMap<>();
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        try {
            Query query = Parser.getQuery(http.getRequestURI().getQuery(), topology);
            Response response = choiceMethod(http, query, this::get, this::put, this::delete);
            sendHttpResponse(http, response);
            http.close();
        } catch (IllegalArgumentException e) {
            sendHttpResponse(http, new Response(Code.CODE_BAD_REQUEST.getCode(), Message.MES_BAD_REQUEST.toString()));
        }
    }

    @NotNull
    @Override
    public Response get(@NotNull Query query) {
        try {
            String id = query.getId();
            byte[] value;
            if (null != (value = cache.get(id))) {
            } else {
                value = dao.get(id);
                cache.put(id, value);
            }
            return new Response(Code.CODE_OK.getCode(), value);
        } catch (IOException e) {
            return new Response(Code.CODE_SERVER_ERROR.getCode(), Message.MES_SERVER_ERROR.toString());
        } catch (NoSuchElementException e) {
            return new Response(Code.CODE_NOT_FOUND.getCode(), Message.MES_NOT_FOUND.toString());
        }
    }

    @NotNull
    @Override
    public Response put(@NotNull Query query,
                        @NotNull byte[] value) {
        try {
            String id = query.getId();
            cache.put(id, value);
            dao.upsert(id, value);
            return new Response(Code.CODE_CREATED.getCode(), Message.MES_CREATED.toString());
        } catch (IOException | IllegalArgumentException e) {
            return new Response(Code.CODE_NOT_FOUND.getCode(), Message.MES_NOT_FOUND.toString());
        }
    }

    @NotNull
    @Override
    public Response delete(@NotNull Query query) {
        try {
            String id = query.getId();
            cache.remove(id);
            dao.delete(id);
            return new Response(Code.CODE_ACCEPTED.getCode(), Message.MES_ACCEPTED.toString());
        } catch (IOException e) {
            return new Response(Code.CODE_SERVER_ERROR.getCode(), Message.MES_SERVER_ERROR.toString());
        }
    }
}
