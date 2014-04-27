package com.impulse.android;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class GetTask extends AsyncTask<String, String, String> {
    private String url;
    private String userId;
    private String otherUserId;
    private String postId;
    private String version;
    private double lat, lon;
    private Date afterTime;
    private RestTaskCallback callback;

    public GetTask(String url, String userId, RestTaskCallback callback) {
        this.url = url;
        this.userId = userId;
        this.lat = 0.0;
        this.lon = 0.0;
        this.callback = callback;
    }

    public GetTask(String url, String userId, double latitude, double longitude, Date afterTime, RestTaskCallback callback) {
        this.url = url;
        this.userId = userId;
        this.lat = latitude;
        this.lon = longitude;
        this.afterTime = afterTime;
        this.callback = callback;
    }

    public GetTask(String url, String userId, String postId, RestTaskCallback callback) {
        this.url = url;
        this.userId = userId;
        this.postId = postId;
        this.callback = callback;
    }

    public GetTask(String url, String myUserId, String otherUserId, String postId, RestTaskCallback callback) {
        this.url = url;
        this.userId = myUserId;
        this.otherUserId = otherUserId;
        this.postId = postId;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet get;

        HttpResponse resp = null;
        String type = url.substring(url.lastIndexOf("/"));

        if (type.equals("/getPostList")) {
            url += "?userKey=" + userId;

            if (lat != 0.0 && lon != 0.0) {
                url += "&latitude=" + lat;
                url += "&longitude=" + lon;
            }

            if(afterTime != null) {
                DateFormat format = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                String date = format.format(afterTime);
                url += "&afterTime=" + date.replaceAll(" ", "+");
            }

            get = new HttpGet(url);

            try {
                resp = client.execute(get);
                result = (new BasicResponseHandler()).handleResponse(resp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result != null)
                return result;

            return "Error Occurred";
        }

        else if (type.equals("/removeFile")) {
            url += "?fileName=" + userId;
            get = new HttpGet(url);

            try {
                resp = client.execute(get);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (resp != null)
                return String.valueOf(resp.getStatusLine().getStatusCode());

            return "Error Occurred";
        }

        else if (type.equals("/aboutUser")) {
            url += "?userKey=" + userId;
            get = new HttpGet(url);

            try {
                resp = client.execute(get);
                result = (new BasicResponseHandler()).handleResponse(resp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result != null)
                return result;

            return "Error Occurred";
        }

        else if (type.equals("/getActiveThreads")) {
            url += "?userKey=" + userId;
            get = new HttpGet(url);

            try {
                resp = client.execute(get);
                result = (new BasicResponseHandler()).handleResponse(resp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result != null)
                return result;

            return "Error Occurred";
        }

        else if (type.equals("/getThread")) {
            url += "?myUserKey=" + userId;
            url += "&otherUserKey=" + otherUserId;
            url += "&postId=" + postId;
            get = new HttpGet(url);

            try {
                resp = client.execute(get);
                result = (new BasicResponseHandler()).handleResponse(resp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result != null)
                return result;

            return "Error Occurred";
        }

        else if (type.equals("/getActiveUserThreads")) {
            url += "?userKey=" + userId;
            url += "&postId=" + postId;

            get = new HttpGet(url);

            try {
                resp = client.execute(get);
                result = (new BasicResponseHandler()).handleResponse(resp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (result != null)
                return result;

            return "Error Occurred";
        }

        else if (type.equals("/initializeSession")) {
            url += "?version=" + userId;

            get = new HttpGet(url);

            try {
                resp = client.execute(get);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (resp != null)
                return String.valueOf(resp.getStatusLine().getStatusCode());

            return "Error Occurred";
        }

        return "Invalid URL";
    }

    @Override
    protected void onPostExecute(String result) {
        callback.onTaskComplete(result);
        super.onPostExecute(result);
    }
}
