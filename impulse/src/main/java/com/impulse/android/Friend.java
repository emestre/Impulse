package com.impulse.android;

/**
 * Created by Eliot on 2/22/14.
 */
public class Friend {

    private String user_name;
    private String user_id;

    public Friend(String user_name, String user_id) {
        this.user_id = user_id;
        this.user_name = user_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_id() {
        return user_id;
    }
}
