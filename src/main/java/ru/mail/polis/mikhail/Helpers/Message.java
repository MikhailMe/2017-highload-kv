package ru.mail.polis.mikhail.Helpers;

public enum Message {

    MES_OK("ok"),
    MES_CREATED("created"),
    MES_ACCEPTED("accepted"),
    MES_NOT_FOUND("not found"),
    MES_BAD_REQUEST("bad request"),
    MES_NOT_ALLOWED("not allowed"),
    MES_SERVER_ERROR("server error"),
    MES_NOT_ENOUGH_REPLICAS("not enough replicas");

    private String message;

    Message(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
