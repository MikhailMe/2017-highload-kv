package ru.mail.polis.mikhail;

import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.mikhail.DAO.MyDAO;
import ru.mail.polis.mikhail.DAO.MyFileDAO;
import ru.mail.polis.mikhail.handlers.EntityHandler;
import ru.mail.polis.mikhail.handlers.InnerHandler;
import ru.mail.polis.mikhail.handlers.StatusHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

public class MikhailService implements KVService {

    @NotNull
    private final HttpServer server;

    private final static String PATH_INNER = "/v0/inner";
    private final static String PATH_STATUS = "/v0/status";
    private final static String PATH_ENTITY = "/v0/entity";

    public MikhailService(final int port,
                          @NotNull final File data,
                          @NotNull final Set<String> topology)
            throws IOException {
        MyDAO dao = new MyFileDAO(data);
        InnerHandler innerHandler = new InnerHandler(dao, topology);
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext(PATH_INNER, innerHandler);
        this.server.createContext(PATH_STATUS, new StatusHandler(dao, topology));
        this.server.createContext(PATH_ENTITY, new EntityHandler(dao, topology, innerHandler, port));
    }

    @Override
    public void start() {
        this.server.start();
    }

    @Override
    public void stop() {
        this.server.stop(0);
    }

}
