package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.mail.polis.mikhail.Code;
import ru.mail.polis.mikhail.Message;

import java.io.IOException;

public class StatusHandler extends BaseHandler{

    @Override
    public void handle(HttpExchange http) throws IOException {
        sendHttpResponse(http, Code.CODE_OK.getCode(), Message.MES_OK.toString());
    }
}
