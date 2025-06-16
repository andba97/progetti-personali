package dev.andba.trismultiplayergame.module;

public class ServerResponse<T> {
    private String operation;
    private T data;

    public ServerResponse() {
    }

    public ServerResponse(String operation) {
        this.operation = operation;
    }

    public ServerResponse(String operation, T data) {
        this.operation = operation;
        this.data = data;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}