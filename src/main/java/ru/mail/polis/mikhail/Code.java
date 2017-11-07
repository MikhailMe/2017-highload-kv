package ru.mail.polis.mikhail;

public enum Code {

    CODE_OK(200),
    CODE_CREATED(201),
    CODE_ACCEPTED(202),
    CODE_BAD_REQUEST(400),
    CODE_NOT_FOUND(404),
    CODE_NOT_ALLOWED(405),
    CODE_SERVICE_UNAVAILABLE(503),
    CODE_MES_NOT_ENOUGH_REPLICAS(504);


    private int responseCode;

    Code(int code) {
        this.responseCode = code;
    }

    public int getCode() {
        return responseCode;
    }
}
