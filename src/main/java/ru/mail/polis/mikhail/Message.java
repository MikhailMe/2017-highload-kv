package ru.mail.polis.mikhail;

public enum Message {

    MES_OK("ok"),
    MES_CREATED("created"),
    MES_ACCEPTED("accpted"),
    MES_EMPTY_ID("empty id"),
    MES_EMPTY_REPLICAS("empty replicas"),
    MES_NOT_FOUND("not found"),
    MES_NOT_ENOUGH_REPLICAS("not enough replicas"),
    MES_INVALID_PARAMETERS("invalid parameters");

    private String message;

    Message(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
