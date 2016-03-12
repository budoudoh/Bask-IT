package com.budoudoh.bask_it.domain;

/**
 * Created by basilu on 3/12/16.
 */
public class DynamoRequest {
    private String operation;
    private Payload payload;

    public DynamoRequest(String operation, Payload payload) {
        this.operation = operation;
        this.payload = payload;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
