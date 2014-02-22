package com.impulse;

public class RestClient {
    private static final String BASE_URL = "http://impulse-backend.appspot.com";
    private static final String CREATE_USER = "/createUser";

    public void postUser(String userKey, final PostCallback callback) {
        String url = BASE_URL + CREATE_USER;
        new PostTask(url, userKey, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onPostSuccess(result);
            }
        }).execute();
    }
}
