package com.impulse.android;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

public class GetTask extends AsyncTask<String, String, String> {
    private String url;
    private String userId;
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

            if(afterTime != null)
                url += "&afterTime=" + afterTime.toString().replaceAll(" ", "+");

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

        return "Invalid URL";
    }

    @Override
    protected void onPostExecute(String result) {
        callback.onTaskComplete(result);
        super.onPostExecute(result);
    }
}
