package ru.mail.polis.mikhail.handlers;

import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.Helpers.Query;
import ru.mail.polis.mikhail.Helpers.Response;

import java.io.IOException;

public interface HttpMethods {

    @NotNull
    Response get(@NotNull final Query query) throws IOException;

    @NotNull
    Response put(@NotNull final Query query, @NotNull final byte[] value) throws IOException;

    @NotNull
    Response delete(@NotNull final Query query) throws IOException;

}
