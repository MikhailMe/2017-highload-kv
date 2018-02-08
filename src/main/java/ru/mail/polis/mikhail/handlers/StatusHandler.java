package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.mikhail.Helpers.Code;
import ru.mail.polis.mikhail.DAO.MyDAO;
import ru.mail.polis.mikhail.Helpers.Message;
import ru.mail.polis.mikhail.Helpers.Response;

import java.io.IOException;
import java.util.List;

public class StatusHandler extends BaseHandler {

    public StatusHandler(@NotNull MyDAO dao, @NotNull List<String> topology) {
        super(dao, topology);
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        sendHttpResponse(http, new Response(Code.CODE_OK.getCode(), Message.MES_OK.toString()));
        //sendHttpResponse(http, Code.CODE_OK.getCode(), Message.MES_OK.toString());
    }
}
