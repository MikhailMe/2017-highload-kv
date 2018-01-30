package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.Helpers.Code;
import ru.mail.polis.mikhail.DAO.MyDAO;
import ru.mail.polis.mikhail.Helpers.Parser;
import ru.mail.polis.mikhail.Helpers.Query;

import java.io.IOException;
import java.util.Set;

public class EntityHandler extends BaseHandler implements HttpMethods{

    public EntityHandler(@NotNull MyDAO dao, @NotNull Set<String> topology) {
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
                    break;
                case DELETE_REQUEST:
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendHttpResponse(http, Code.CODE_BAD_REQUEST.getCode(), e.getMessage());
        }
    }

    // FIXME
    @Override
    public void get(@NotNull HttpExchange http,
                    @NotNull Query query)
            throws IOException {

    }

    // FIXME
    @Override
    public void put(@NotNull HttpExchange http,
                    @NotNull Query query,
                    @NotNull byte[] value)
            throws IOException {

    }

    // FIXME
    @Override
    public void delete(@NotNull HttpExchange http,
                       @NotNull Query query)
            throws IOException {

    }

}

