package ru.mail.polis.mikhail;

public class Response {

    private final int code;
    private final byte[] value;

    Response(int code) {
        this.code = code;
        this.value = null;
    }

    Response(int code, String value) {
        this.code = code;
        this.value = value.getBytes();
    }

    Response(int code, byte[] value) {
        this.code = code;
        this.value = value;
    }

    int getCode() {
        return code;
    }

    boolean hasValue() {
        return value != null;
    }

    byte[] getValue() {
        return value;
    }
}
