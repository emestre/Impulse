package com.impulse.impulse;

/**
 * Created by eliot.mestre on 4/11/2014.
 */
public class Reply {

    public String postId;
    public String userKey;
    public String timeout;

    public Reply(String postId, String userKey, String timeout) {
        this.postId = postId;
        this.userKey = userKey;
        this.timeout = timeout;
    }
}
