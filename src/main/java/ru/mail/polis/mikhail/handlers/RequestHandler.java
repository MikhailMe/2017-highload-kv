package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;

import static ru.mail.polis.mikhail.Code.*;
import static ru.mail.polis.mikhail.Message.*;

public class RequestHandler extends BaseHandler {

    @Override
    public void handle(HttpExchange http) throws IOException {
        boolean master = false;
        String requestMethod = http.getRequestMethod();
        String query = http.getRequestURI().getQuery();
        String id;
        String replicas;
        int ack = -1;
        int from = -1;
        if (!http.getRequestHeaders().containsKey("Replication")) {
            master = true;
        }
        if (query.contains("&")) {
            String[] parameters = query.split("&");
            id = parameters[0];
            replicas = parameters[1];
            if (id.split("=").length == 1) {
                sendHttpResponse(http, CODE_BAD_REQUEST.getCode(), MES_EMPTY_ID.toString());
                return;
            }
            id = id.split("=")[1];
            if (replicas.split("=").length == 1) {
                sendHttpResponse(http, CODE_BAD_REQUEST.getCode(), MES_EMPTY_REPLICAS.toString());
                return;
            }
            replicas = replicas.split("=")[1];
            ack = Integer.valueOf(replicas.split("/")[0]);
            from = Integer.valueOf(replicas.split("/")[1]);
            if (ack > from || ack == 0 || from == 0) {
                sendHttpResponse(http, CODE_BAD_REQUEST.getCode(), MES_INVALID_PARAMETERS.toString());
                return;
            }
        } else {
            if (query.split("=").length == 1) {
                sendHttpResponse(http, CODE_BAD_REQUEST.getCode(), MES_EMPTY_ID.toString());
                return;
            }
            id = query.split("=")[1];
        }
        if (ack == -1 || from == -1) {
            ack = topology.size() / 2 + 1;
            from = topology.size();
        }
        File file = new File(data.getAbsolutePath() + delimiter + id);
        if (requestMethod.equalsIgnoreCase("GET")) {
            if (master) {
                ReplicationManager rm = new ReplicationManager(topology, ack, from, query, "GET", "http:/" + http.getLocalAddress().toString(), null);
                int status = rm.replication();
                if (status == 0) {
                    if (!file.exists()) {
                        sendHttpResponse(http, CODE_NOT_FOUND.getCode(), MES_NOT_FOUND.toString());
                        return;
                    }
                    sendHttpResponse(http, CODE_OK.getCode(), file);
                } else if (status == -1) {
                    sendHttpResponse(http, CODE_MES_NOT_ENOUGH_REPLICAS.getCode(), MES_NOT_ENOUGH_REPLICAS.toString());
                } else {
                    sendHttpResponse(http, CODE_NOT_FOUND.getCode(), MES_NOT_FOUND.toString());
                }
            } else {
                if (!file.exists()) {
                    sendHttpResponse(http, CODE_NOT_FOUND.getCode(), MES_NOT_FOUND.toString());
                    return;
                }
                sendHttpResponse(http, CODE_OK.getCode(), MES_OK.toString());
            }
        } else if (requestMethod.equalsIgnoreCase("PUT")) {
            if (!file.exists()) file.createNewFile();
            byte[] buffer = new byte[1024];
            InputStream is = http.getRequestBody();
            BufferedOutputStream bs = new BufferedOutputStream(new FileOutputStream(file));
            for (int n = is.read(buffer); n > 0; n = is.read(buffer)) bs.write(buffer);
            bs.close();
            if (master) {
                ReplicationManager rm = new ReplicationManager(topology, ack, from, query, "PUT", "http:/" + http.getLocalAddress().toString(), buffer);
                if (rm.replication() == 0) {
                    sendHttpResponse(http, CODE_CREATED.getCode(), MES_CREATED.toString());
                } else {
                    sendHttpResponse(http, CODE_MES_NOT_ENOUGH_REPLICAS.getCode(), MES_NOT_ENOUGH_REPLICAS.toString());
                }
            } else {
                sendHttpResponse(http, CODE_CREATED.getCode(), "Created");
            }
        } else if (requestMethod.equalsIgnoreCase("DELETE")) {
            if (!file.exists() || file.delete()) {
                if (master) {
                    ReplicationManager rm = new ReplicationManager(topology, ack, from, query, "DELETE", "http:/" + http.getLocalAddress().toString(), null);
                    if (rm.replication() == 0) {
                        sendHttpResponse(http, 202, "Accepted");
                    } else {
                        sendHttpResponse(http, 504, "Not Enough Replicas");
                    }
                } else {
                    sendHttpResponse(http, 202, "Accepted");
                }
            } else {
                throw new IOException();
            }
        } else {
            throw new IOException();
        }
    }
}