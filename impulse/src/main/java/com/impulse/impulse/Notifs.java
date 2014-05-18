package com.impulse.impulse;

/**
 * Created by akshay159 on 17/05/14.
 */
public class Notifs {
    private String senderKey;
    private String senderName;
    private String postId;
    private String message;

    public Notifs(String senderKey, String senderName, String postId, String message) {
        this.senderKey = senderKey;
        this.senderName = senderName;
        this.postId = postId;
        this.message = message;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getPostId() {
        return postId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object object) {
        if (object.getClass().equals(this.getClass()) == false) {
            return false;
        }

        Notifs comp = (Notifs)object;

        if (comp.getSenderKey().equals(senderKey) && comp.getPostId().equals(postId))
            return true;

        return false;
    }
}
