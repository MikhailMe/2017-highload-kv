package ru.mail.polis.mikhail.Helpers;

public enum Code {

    CODE_OK(200),
    CODE_CREATED(201),
    CODE_ACCEPTED(202),
    CODE_BAD_REQUEST(400),
    CODE_NOT_FOUND(404),
    CODE_NOT_ALLOWED(405),
    CODE_SERVER_ERROR(500),
    CODE_NOT_ENOUGH_REPLICAS(504);

    private int responseCode;

    Code(int code) {
        this.responseCode = code;
    }

    public int getCode() {
        return responseCode;
    }
}
