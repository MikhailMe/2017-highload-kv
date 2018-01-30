package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.Helpers.Query;

import java.io.IOException;

public interface HttpMethods {

    void get(@NotNull final HttpExchange http, @NotNull final Query query) throws IOException;

    void put(@NotNull final HttpExchange http, @NotNull final Query query, @NotNull final byte[] value) throws IOException;

    void delete(@NotNull final HttpExchange http, @NotNull final Query query) throws IOException;

}
