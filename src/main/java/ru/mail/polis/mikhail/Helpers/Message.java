package ru.mail.polis.mikhail.Helpers;

public enum Message {

    MES_OK("ok"),
    MES_CREATED("created"),
    MES_ACCEPTED("accpted"),
    MES_EMPTY_ID("empty id"),
    MES_NOT_FOUND("not found"),
    MES_NOT_ALLOWED("not allowed"),
    MES_EMPTY_REPLICAS("empty replicas"),
    MES_INVALID_PARAMETERS("invalid parameters"),
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
