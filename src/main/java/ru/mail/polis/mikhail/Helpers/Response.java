package ru.mail.polis.mikhail.Helpers;

public class Response {

    private final int code;
    private final byte[] value;

    public Response(int code) {
        this.code = code;
        this.value = null;
    }

    public Response(int code, String value) {
        this.code = code;
        this.value = value.getBytes();
    }

    public Response(int code, byte[] value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public byte[] getValue() {
        return value;
    }

    public boolean hasValue() {
        return value != null;
    }
}
