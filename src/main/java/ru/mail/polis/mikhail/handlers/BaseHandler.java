package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class BaseHandler implements HttpHandler {

    private final static String ID = "id=";
    private final static String ADDRESS = "address=";
    private final static String REPLICAS = "replicas=";
    private final static String DELIMETER = "/";


    protected void sendHttpResponse(HttpExchange http, int code, File file) throws IOException {
        http.sendResponseHeaders(code, file.length());
        OutputStream outputStream = http.getResponseBody();
        Files.copy(file.toPath(), outputStream);
        outputStream.close();
    }

    protected void sendHttpResponse(HttpExchange http, int code, String response) throws IOException {
        http.sendResponseHeaders(code, response.getBytes().length);
        http.getResponseBody().write(response.getBytes());
        http.getResponseBody().close();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }

    @NotNull
    private String extractId(@NotNull final String query)
            throws IllegalArgumentException {
        for (String subQuery : query.split("&")) {
            if (subQuery.startsWith(ID))
                if (subQuery.length() == ID.length())
                    throw new IllegalArgumentException();
            return subQuery.substring(ID.length());
        }
        throw new IllegalArgumentException();
    }
}
