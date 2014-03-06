package com.impulse.android;

import java.util.ArrayList;

public class RestClient {
    private static final String BASE_URL = "http://impulse-backend.appspot.com";
    private static final String CREATE_USER = "/createUser";
    private static final String UPLOAD_FILE = "/uploadFile";
    private static final String GET_POST_LIST = "/getPostList";
    private static final String GET_FILE = "/getFile";

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

    public void postFile(String userKey, String caption, double latitude, double longitude, String filePath, String extension, int timeout, int rotation, final PostCallback callback) {
        String url = BASE_URL + UPLOAD_FILE;
        new PostTask(url, userKey, caption, latitude, longitude, filePath, extension, timeout, rotation, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                if (callback != null) {
                    callback.onPostSuccess(result);
                }

            }
        }).execute();
    }

    public void getPostList(final GetCallback callback) {
        String url = BASE_URL + GET_POST_LIST;
        new GetTask(url, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }).execute();
    }

    public static String getFile(String fileName) {
        return BASE_URL + GET_FILE + "?fileName=" + fileName;
    }
}
