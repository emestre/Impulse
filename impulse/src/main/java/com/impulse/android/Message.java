package com.impulse.android;

/**
 * Created by eliot.mestre on 4/11/2014.
 */
public class Message {

    public String userKey;
    public String message;
    public String timestamp;

    public Message(String userKey, String message, String timestamp) {
        this.userKey = userKey;
        this.message = message;
        this.timestamp = timestamp;
    }
}
