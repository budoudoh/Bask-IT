package com.budoudoh.bask_it.domain;

/**
 * Created by basilu on 3/12/16.
 */
public class SMSRequest {
    String message;

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {

        return message;
    }

    public SMSRequest(String message) {

        this.message = message;
    }
}
