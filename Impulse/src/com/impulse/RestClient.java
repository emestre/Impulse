package com.impulse;

import java.util.ArrayList;

public class RestClient {
    private static final String BASE_URL = "http://impulse-backend.appspot.com";
    private static final String CREATE_USER = "/createUser";
    private static final String FRIEND_LIST = "/getFriendList";

    public void postUser(String userKey, final PostCallback callback) {
        String url = BASE_URL + CREATE_USER;
        new PostTask(url, userKey, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                if(callback != null)
                    callback.onPostSuccess(result);
            }
        }).execute();
    }

    public void getFriendList(ArrayList<String> friends, final PostCallback callback) {
        String url = BASE_URL + FRIEND_LIST;
        new PostTask(url, friends, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                if (callback != null)
                    callback.onPostSuccess(result);
            }
        }).execute();
    }
}
