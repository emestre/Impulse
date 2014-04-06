package com.impulse.android;

import java.util.ArrayList;
import java.util.Date;

public class RestClient {
    private static final String BASE_URL = "http://impulse-backend.appspot.com";
    private static final String CREATE_USER = "/createUser";
    private static final String UPLOAD_FILE = "/uploadFile";
    private static final String GET_POST_LIST = "/getPostList";
    private static final String GET_FILE = "/getFile";
    private static final String REMOVE_FILE = "/removeFile";
    private static final String LIKE_POST = "/likePost";

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

    public void postFile(String userKey, String caption, double latitude, double longitude, String filePath, String extension, int timeout, int rotation, String audience, String location, final PostCallback callback) {
        String url = BASE_URL + UPLOAD_FILE;
        new PostTask(url, userKey, caption, latitude, longitude, filePath, extension, timeout, rotation, audience, location, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                if (callback != null) {
                    callback.onPostSuccess(result);
                }

            }
        }).execute();
    }

    public void getPostList(String userKey, double latitude, double longitude, Date afterTime, final GetCallback callback) {
        String url = BASE_URL + GET_POST_LIST;
        new GetTask(url, userKey, latitude, longitude, afterTime, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }).execute();
    }

    public void getPostList(final GetCallback callback, String userId) {
        String url = BASE_URL + GET_POST_LIST;
        new GetTask(url, userId, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }).execute();
    }

    public void removeFile(String filename, final GetCallback callback) {
        String url = BASE_URL + REMOVE_FILE;
        new GetTask(url, filename, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }).execute();
    }

    public void likePost(String filename, String userKey, final GetCallback callback) {
        String url = BASE_URL + LIKE_POST;
        new PostTask(url, filename, userKey, new RestTaskCallback() {
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
