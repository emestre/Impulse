package com.impulse;

import java.util.ArrayList;

public class RestClient {
    private static final String BASE_URL = "http://impulse-backend.appspot.com";
    private static final String CREATE_USER = "/createUser";
    private static final String FRIEND_LIST = "/getFriendList";
    private static final String UPLOAD_FILE = "/uploadFile";

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

    public void postFile(String userKey, String caption, double latitude, double longitude, String filePath, String extension, int timeout, final PostCallback callback) {
        String url = BASE_URL + UPLOAD_FILE;
        new PostTask(userKey, caption, latitude, longitude, filePath, extension, timeout, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                if (callback != null) {
                    callback.onPostSuccess(result);
                }

            }
        }).execute();
    }
}
