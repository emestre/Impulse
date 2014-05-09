package com.impulse.impulse;

/**
 * Created by eliot.mestre on 4/11/2014.
 */
public class Message {

    public String userKey;
    public String message;
    public String timestamp;
    public String type;

    public Message(String userKey, String message, String timestamp, String type) {
        this.userKey = userKey;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }
}
