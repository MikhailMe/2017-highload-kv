package ru.mail.polis.mikhail;

public enum Code {

    OK(200),
    CREATED(201),
    ACCEPTED(202),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    NOT_ALLOWED(405),
    SERVICE_UNAVAILABLE(503);

    private int responseCode;

    Code(int code) {
        this.responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
