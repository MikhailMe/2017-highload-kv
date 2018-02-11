package ru.mail.polis;


import org.junit.Test;
import ru.mail.polis.mikhail.Helpers.Parser;

import java.security.SecureRandom;
import java.util.*;

/**
 * Unit tests for parser {@link ru.mail.polis.mikhail.Helpers.Parser}
 *
 * @author Mikhail Medvedev <neon-pf@yandex.ru>
 */
public class ParserTest {

    private static final String AND = "&";
    private static final String ID = "id=";
    private static final String REPLICAS = "replicas=";

    private List<String> ids = new ArrayList<>();
    private List<String> replicas = new ArrayList<>();

    @Test
    public void checkParams() {
        for (int i = 0; i < 1000; i++) {
            String request = generateRequestWithReplicas();
            Map<String, String> params = Parser.getParams(request);
            assert params.get("id").equals(ids.get(i));
            assert params.get("replicas").equals(replicas.get(i));
        }
        System.out.println("all right");
    }

    private String generateRequestWithReplicas() {
        String id = generateId();
        String replica = generateReplicas();
        ids.add(id);
        replicas.add(replica);
        return ID + id + AND + REPLICAS + replica;
    }

    private String generateId() {
        int length = 16;
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rand = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(alphabet.charAt(rand.nextInt(alphabet.length())));
        return sb.toString();
    }

    private String generateReplicas() {
        int a = new Random().nextInt(3) + 1;
        int b = new Random().nextInt(3) + 1;
        return a > b ? b + "/" + a : a + "/" + b;
    }

}
