package ru.mail.polis.mikhail.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.mikhail.Helpers.*;
import ru.mail.polis.mikhail.DAO.MyDAO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

public class EntityHandler extends BaseHandler implements HttpMethods {

    private static final String ID = "?id=";
    private final static String PATH_INNER = "/v0/inner";
    private static final String PATH = "http://localhost";

    private final int port;
    @NotNull
    private final InnerHandler instanceOfIH;
    @NotNull
    private final CompletionService<Response> completionService;

    public EntityHandler(@NotNull MyDAO dao, @NotNull Set<String> topology, @NotNull InnerHandler innerHandler, int port) {
        super(dao, topology);
        this.port = port;
        this.instanceOfIH = innerHandler;
        Executor executor = Executors.newFixedThreadPool(topology.size());
        this.completionService = new ExecutorCompletionService<>(executor);
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        try {
            Query query = Parser.getQuery(http.getRequestURI().getQuery(), topology);
            switch (http.getRequestMethod()) {
                case GET_REQUEST:
                    get(http, query);
                    break;
                case PUT_REQUEST:
                    final byte[] value = getByteArray(http.getRequestBody());
                    put(http, query, value);
                    break;
                case DELETE_REQUEST:
                    delete(http, query);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendHttpResponse(http, Code.CODE_BAD_REQUEST.getCode(), e.getMessage());
        }
    }

    @NotNull
    @Override
    public Response get(@NotNull HttpExchange http,
                        @NotNull Query query)
            throws IOException {
        try {
            String id = query.getId();
            List<String> nodes = getNodesById(id, query.getFrom());
            doTasks(http, query, nodes, null);
            int amountOK = 0;
            int amountNF = 0;
            byte[] value = null;
            for (int i = 0; i < query.getFrom(); i++) {
                try {
                    Response response = completionService.take().get();
                    if (response.getCode() == Code.CODE_OK.getCode()) {
                        amountOK++;
                        value = response.getValue();
                    } else if (response.getCode() == Code.CODE_NOT_FOUND.getCode()) {
                        amountNF++;
                    }
                } catch (Exception e) {
                    code = Code.CODE_SERVER_ERROR.getCode();
                    sendHttpResponse(http, code, Message.MES_SERVER_ERROR.toString());
                    return new Response(code);
                }
            }
            boolean hasHere = instanceOfIH.get(http, query).getCode() == Code.CODE_OK.getCode();
            if (!hasHere && amountOK > 0 && amountNF == 1) {
                instanceOfIH.put(http, query, value);
                amountNF--;
                amountOK++;
            }
            if (amountOK + amountNF < query.getAck()) {
                code = Code.CODE_NOT_ENOUGH_REPLICAS.getCode();
                sendHttpResponse(http, code, Message.MES_NOT_ENOUGH_REPLICAS.toString());
                return new Response(code);
            } else if (amountOK < query.getAck()) {
                code = Code.CODE_NOT_FOUND.getCode();
                sendHttpResponse(http, code, Message.MES_NOT_FOUND.toString());
                return new Response(code);
            } else {
                code = Code.CODE_OK.getCode();
                sendHttpResponse(http, code, value);
                return new Response(code, value);
            }
        } catch (IllegalArgumentException e) {
            code = Code.CODE_BAD_REQUEST.getCode();
            sendHttpResponse(http, code, e.getMessage());
            return new Response(code, e.getMessage());
        } catch (NoSuchElementException e) {
            code = Code.CODE_NOT_FOUND.getCode();
            sendHttpResponse(http, code, e.getMessage());
            return new Response(code, e.getMessage());
        }
    }

    @NotNull
    @Override
    public Response put(@NotNull HttpExchange http,
                        @NotNull Query query,
                        @NotNull byte[] value)
            throws IOException {
        try {
            List<String> nodes = getNodesById(query.getId(), query.getFrom());
            doTasks(http, query, nodes, value);
            int success = 0;
            for (int i = 0; i < query.getFrom() && success < query.getAck(); i++) {
                try {
                    Response response = completionService.take().get();
                    if (response.getCode() == Code.CODE_CREATED.getCode()) {
                        success++;
                    }
                } catch (Exception e) {
                    code = Code.CODE_SERVER_ERROR.getCode();
                    sendHttpResponse(http, code, Message.MES_SERVER_ERROR.toString());
                    return new Response(code);
                }
            }
            if (success < query.getAck()) {
                code = Code.CODE_NOT_ENOUGH_REPLICAS.getCode();
                sendHttpResponse(http, code, Message.MES_NOT_ENOUGH_REPLICAS.toString());
                return new Response(code);
            } else {
                code = Code.CODE_CREATED.getCode();
                sendHttpResponse(http, code, Message.MES_CREATED.toString());
                return new Response(code);
            }
        } catch (IllegalArgumentException e) {
            code = Code.CODE_BAD_REQUEST.getCode();
            sendHttpResponse(http, code, e.getMessage());
            return new Response(code, e.getMessage());
        } catch (NoSuchElementException e) {
            code = Code.CODE_NOT_FOUND.getCode();
            sendHttpResponse(http, code, e.getMessage());
            return new Response(code, e.getMessage());
        }
    }

    @NotNull
    @Override
    public Response delete(@NotNull HttpExchange http,
                           @NotNull Query query)
            throws IOException {
        try {
            List<String> nodes = getNodesById(query.getId(), query.getFrom());
            doTasks(http, query, nodes, null);
            int success = 0;
            for (int i = 0; i < query.getFrom() && success < query.getAck(); i++) {
                try {
                    Response response = completionService.take().get();
                    if (response.getCode() == Code.CODE_ACCEPTED.getCode()) {
                        success++;
                    }
                } catch (Exception e) {
                    code = Code.CODE_SERVER_ERROR.getCode();
                    sendHttpResponse(http, code, Message.MES_SERVER_ERROR.toString());
                    return new Response(code);
                }
            }
            if (success < query.getAck()) {
                code = Code.CODE_NOT_ENOUGH_REPLICAS.getCode();
                sendHttpResponse(http, code, Message.MES_NOT_ENOUGH_REPLICAS.toString());
                return new Response(code);
            } else {
                code = Code.CODE_ACCEPTED.getCode();
                sendHttpResponse(http, code, Message.MES_ACCEPTED.toString());
                return new Response(code);
            }
        } catch (IllegalArgumentException e) {
            code = Code.CODE_BAD_REQUEST.getCode();
            sendHttpResponse(http, code, e.getMessage());
            return new Response(code, e.getMessage());
        } catch (NoSuchElementException e) {
            code = Code.CODE_NOT_FOUND.getCode();
            sendHttpResponse(http, code, e.getMessage());
            return new Response(code, e.getMessage());
        }
    }

    private void doTasks(@NotNull HttpExchange http,
                         @NotNull Query query,
                         @NotNull List<String> nodes,
                         @Nullable byte[] value)
            throws IOException {
        String address = PATH + ":" + port;
        for (String node : nodes) {
            String method = http.getRequestMethod();
            if (node.equals(address)) {
                switch (method) {
                    case GET_REQUEST:
                        completionService.submit(() -> instanceOfIH.get(http, query));
                        break;
                    case PUT_REQUEST:
                        completionService.submit(() -> instanceOfIH.put(http, query, value));
                        break;
                    case DELETE_REQUEST:
                        completionService.submit(() -> instanceOfIH.delete(http, query));
                        break;
                    default:
                        sendHttpResponse(http, Code.CODE_NOT_ALLOWED.getCode(), Message.MES_NOT_ALLOWED.toString());
                        throw new IllegalArgumentException(Message.MES_NOT_ALLOWED.toString());
                }
            } else {
                String path = node + PATH_INNER;
                String id = ID + query.getId();
                completionService.submit(() -> makeRequest(http, path, id, value));
            }
        }
    }

    private Response makeRequest(@NotNull HttpExchange http,
                                 @NotNull String path,
                                 @NotNull String id,
                                 @Nullable byte[] value) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(path + id);
            String method = http.getRequestMethod();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(PUT_REQUEST.equals(method));
            connection.connect();
            if (PUT_REQUEST.equals(method)) {
                connection.getOutputStream().write(value);
                connection.getOutputStream().flush();
                connection.getOutputStream().close();
            }
            int code = connection.getResponseCode();
            if (GET_REQUEST.equals(method) && Code.CODE_OK.getCode() == code) {
                byte[] inputData = getByteArray(connection.getInputStream());
                return new Response(code, inputData);
            }
            return new Response(code);
        } catch (IOException e) {
            return new Response(Code.CODE_SERVER_ERROR.getCode());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
