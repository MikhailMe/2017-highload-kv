package ru.mail.polis.mikhail;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.NoSuchElementException;

interface MyDAO {
    @NotNull
    byte[] get(@NotNull final String key) throws NoSuchElementException, IllegalArgumentException, IOException;

    @NotNull
    void upsert(@NotNull final String key, @NotNull final byte[] value) throws IllegalArgumentException, IOException;

    @NotNull
    void delete(@NotNull final String key) throws IllegalArgumentException, IOException;

}