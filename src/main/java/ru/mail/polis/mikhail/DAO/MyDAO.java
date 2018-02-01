package ru.mail.polis.mikhail.DAO;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.NoSuchElementException;

public interface MyDAO {
    @NotNull
    byte[] get(@NotNull final String key) throws NoSuchElementException, IllegalArgumentException, IOException;

    void upsert(@NotNull final String key, @NotNull final byte[] value) throws IllegalArgumentException, IOException;

    void delete(@NotNull final String key) throws IllegalArgumentException, IOException;
}
