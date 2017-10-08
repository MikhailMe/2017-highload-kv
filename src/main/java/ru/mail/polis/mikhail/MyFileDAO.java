package ru.mail.polis.mikhail;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.NoSuchElementException;

public class MyFileDAO implements MyDAO {

    @NotNull
    private final File directory;

    public MyFileDAO(@NotNull final File dir) {
        this.directory = dir;
    }

    @NotNull
    private File getFile(@NotNull final String key) {
        return new File(directory, key);
    }

    @NotNull
    @Override
    public byte[] get(@NotNull final String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        final File file = getFile(key);
        final byte[] value = new byte[(int) file.length()];
        try (InputStream is = new FileInputStream(file)) {
            if (is.read(value) != file.length())
                throw new IOException("can't read file");
        }
        return value;
    }

    @NotNull
    @Override
    public void upsert(@NotNull final String key, @NotNull final byte[] value) throws IllegalArgumentException, IOException {
        try (OutputStream os = new FileOutputStream(getFile(key))) {
            os.write(value);
        }
    }

    @NotNull
    @Override
    public void delete(@NotNull final String key) throws IllegalArgumentException, IOException {
        //noinspection ResultOfMethodCallIgnored
        try {
            getFile(key).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}