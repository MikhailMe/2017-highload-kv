package ru.mail.polis.mikhail.handlers;

import com.google.common.io.ByteStreams;
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

    public EntityHandler(int port,
                         @NotNull MyDAO dao,
                         @NotNull List<String> topology,
                         @NotNull InnerHandler innerHandler) {
        super(dao, topology);
        this.port = port;
        this.instanceOfIH = innerHandler;
        Executor executor = Executors.newFixedThreadPool(topology.size());
        this.completionService = new ExecutorCompletionService<>(executor);
    }

    @Override
    public void handle(HttpExchange http) throws IOException {
        try {
            Response response;
            Query query = Parser.getQuery(http.getRequestURI().getQuery(), topology);
            switch (http.getRequestMethod()) {
                case GET_REQUEST:
                    response = get(query);
                    break;
                case PUT_REQUEST:
                    final byte[] value = ByteStreams.toByteArray(http.getRequestBody());
                    response = put(query, value);
                    break;
                case DELETE_REQUEST:
                    response = delete(query);
                    break;
                default:
                    response = new Response(Code.CODE_NOT_ALLOWED.getCode(), Message.MES_NOT_ALLOWED.toString());
                    break;
            }
            sendHttpResponse(http, response);
            http.close();
        } catch (IllegalArgumentException e) {
            sendHttpResponse(http, new Response(Code.CODE_BAD_REQUEST.getCode(), e.getMessage()));
        }
    }

    @NotNull
    @Override
    public Response get(@NotNull Query query)
            throws IOException {
        try {
            String id = query.getId();
            List<String> nodes = getNodesById(id, query.getFrom());
            doTasks(GET_REQUEST, query, nodes, null);
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
                    return new Response(code);
                }
            }
            boolean hasHere = instanceOfIH.get(query).getCode() == Code.CODE_OK.getCode();
            if (amountOK > 0 && amountNF == 1 && !hasHere) {
                instanceOfIH.put(query, value);
                amountNF--;
                amountOK++;
            }
            if (amountOK + amountNF < query.getAck()) {
                code = Code.CODE_NOT_ENOUGH_REPLICAS.getCode();
                return new Response(code);
            } else if (amountOK < query.getAck()) {
                code = Code.CODE_NOT_FOUND.getCode();
                return new Response(code);
            } else {
                code = Code.CODE_OK.getCode();
                return new Response(code, value);
            }
        } catch (IllegalArgumentException e) {
            code = Code.CODE_BAD_REQUEST.getCode();
            return new Response(code, e.getMessage());
        } catch (NoSuchElementException e) {
            code = Code.CODE_NOT_FOUND.getCode();
            return new Response(code, e.getMessage());
        }
    }

    @NotNull
    @Override
    public Response put(@NotNull Query query,
                        @NotNull byte[] value) {
        try {
            List<String> nodes = getNodesById(query.getId(), query.getFrom());
            doTasks(PUT_REQUEST, query, nodes, value);
            int success = 0;
            for (int i = 0; i < query.getFrom() && success < query.getAck(); i++) {
                try {
                    Response response = completionService.take().get();
                    if (response.getCode() == Code.CODE_CREATED.getCode()) {
                        success++;
                    }
                } catch (Exception e) {
                    code = Code.CODE_SERVER_ERROR.getCode();
                    return new Response(code);
                }
            }
            if (success < query.getAck()) {
                code = Code.CODE_NOT_ENOUGH_REPLICAS.getCode();
                return new Response(code);
            } else {
                code = Code.CODE_CREATED.getCode();
                return new Response(code);
            }
        } catch (IllegalArgumentException e) {
            code = Code.CODE_BAD_REQUEST.getCode();
            return new Response(code, e.getMessage());
        } catch (NoSuchElementException e) {
            code = Code.CODE_NOT_FOUND.getCode();
            return new Response(code, e.getMessage());
        }
    }

    @NotNull
    @Override
    public Response delete(@NotNull Query query) {
        try {
            List<String> nodes = getNodesById(query.getId(), query.getFrom());
            doTasks(DELETE_REQUEST, query, nodes, null);
            int success = 0;
            for (int i = 0; i < query.getFrom() && success < query.getAck(); i++) {
                try {
                    Response response = completionService.take().get();
                    if (response.getCode() == Code.CODE_ACCEPTED.getCode()) {
                        success++;
                    }
                } catch (Exception e) {
                    code = Code.CODE_SERVER_ERROR.getCode();
                    return new Response(code);
                }
            }
            if (success < query.getAck()) {
                code = Code.CODE_NOT_ENOUGH_REPLICAS.getCode();
                return new Response(code);
            } else {
                code = Code.CODE_ACCEPTED.getCode();
                return new Response(code);
            }
        } catch (IllegalArgumentException e) {
            code = Code.CODE_BAD_REQUEST.getCode();
            return new Response(code, e.getMessage());
        } catch (NoSuchElementException e) {
            code = Code.CODE_NOT_FOUND.getCode();
            return new Response(code, e.getMessage());
        }
    }

    private void doTasks(@NotNull String method,
                         @NotNull Query query,
                         @NotNull List<String> nodes,
                         @Nullable byte[] value) {
        String address = PATH + ":" + port;
        for (String node : nodes) {
            if (node.equals(address)) {
                switch (method) {
                    case GET_REQUEST:
                        completionService.submit(() -> instanceOfIH.get(query));
                        break;
                    case PUT_REQUEST:
                        completionService.submit(() -> instanceOfIH.put(query, value));
                        break;
                    case DELETE_REQUEST:
                        completionService.submit(() -> instanceOfIH.delete(query));
                        break;
                    default:
                        throw new IllegalArgumentException(Message.MES_NOT_ALLOWED.toString());
                }
            } else {
                String path = node + PATH_INNER;
                String id = ID + query.getId();
                completionService.submit(() -> makeRequest(method, path, id, value));
            }
        }
    }

    private Response makeRequest(@NotNull String method,
                                 @NotNull String path,
                                 @NotNull String id,
                                 @Nullable byte[] value) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(path + id);
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
