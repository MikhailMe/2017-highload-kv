package ru.mail.polis.mikhail.Helpers;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    private static final String ID = "id";
    private static final String AND = "&";
    private static final String EQUALS = "=";
    private final static String DELIMITER = "/";
    private static final String ENCODING = "UTF-8";
    private static final String REPLICAS = "replicas";
    private static final String INVALID_QUERY = "Invalid query";

    private static final Map<String, Query> cache;

    static {
        cache = new HashMap<>();
    }

    public static Query getQuery(@NotNull String key, @NotNull List<String> topology) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Map<String, String> params = getParams(key);
        String id = params.get(ID);
        int ack, from;
        if (params.containsKey(REPLICAS)) {
            String rp[] = params.get(REPLICAS).split(DELIMITER);
            ack = Integer.valueOf(rp[0]);
            from = Integer.valueOf(rp[1]);
        } else {
            ack = topology.size() / 2 + 1;
            from = topology.size();
        }
        if (id == null || "".equals(id) || ack < 1 || from < 1 || ack > from) {
            throw new IllegalArgumentException(INVALID_QUERY);
        }
        Query query  = new Query(id, ack, from);
        cache.put(id, query);
        return query;
    }

    private static Map<String, String> getParams(@NotNull String query) {
        try {
            Map<String, String> params = new HashMap<>();
            for (String param : query.split(AND)) {
                int index = param.indexOf(EQUALS);
                String param1 = URLDecoder.decode(param.substring(0, index), ENCODING);
                String param2 = URLDecoder.decode(param.substring(index + 1), ENCODING);
                params.put(param1, param2);
            }
            return params;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(INVALID_QUERY);
        }
    }
}
