package com.impulse.android;

import java.util.Date;

public class RestClient {
    private static final String BASE_URL = "http://impulse-backend.appspot.com";
    private static final String CREATE_USER = "/createUser";
    private static final String UPLOAD_FILE = "/uploadFile";
    private static final String GET_POST_LIST = "/getPostList";
    private static final String GET_FILE = "/getFile";
    private static final String REMOVE_FILE = "/removeFile";
    private static final String LIKE_POST = "/likePost";
    private static final String ABOUT_USER = "/aboutUser";
    private static final String GET_ACTIVE_THREADS = "/getActiveThreads";
    private static final String GET_THREAD = "/getThread";
    private static final String CREATE_MESSAGE = "/createMessage";
    private static final String EDIT_ABOUT_USER = "/editAboutUser";
    private static final String GET_ACTIVE_USER_THREAD = "/getActiveUserThreads";

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

    public void aboutUser(String userKey, final GetCallback callback) {
        String url = BASE_URL + ABOUT_USER;
        new GetTask(url, userKey, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }).execute();
    }

    public void getActiveThreads(String userKey, final GetCallback callback) {
        String url = BASE_URL + GET_ACTIVE_THREADS;
        new GetTask(url, userKey, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }).execute();
    }

    public void getThread(String myUserKey, String otherUserKey, String postId, final GetCallback callback) {
        String url = BASE_URL + GET_THREAD;
        new GetTask(url, myUserKey, otherUserKey, postId, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }).execute();
    }

    public void createMessage(String fromUser, String toUser, String postId, String message, final PostCallback callback) {
        String url = BASE_URL + CREATE_MESSAGE;
        new PostTask(url, fromUser, toUser, postId, message, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onPostSuccess(result);
            }
        }).execute();
    }

    public void editAboutUser(String userKey, String aboutMe, final PostCallback callback) {
        String url = BASE_URL + EDIT_ABOUT_USER;
        new PostTask(url, aboutMe, userKey, new RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onPostSuccess(result);
            }
        }).execute();
    }

    public void getActiveUserThreads(String userKey, String postId, final GetCallback callback) {
        String url = BASE_URL + GET_ACTIVE_USER_THREAD;
        new GetTask(url, userKey, postId, new RestTaskCallback() {
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
