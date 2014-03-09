package com.impulse.android;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URLEncoder;

public class GetTask extends AsyncTask<String, String, String> {
    private String url;
    private String userId;
    private RestTaskCallback callback;

    public GetTask(String url, RestTaskCallback callback) {
        this.url = url;
        this.callback = callback;
    }

    public GetTask(String url, String userId, RestTaskCallback callback) {
        this.url = url;
        this.userId = userId;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);

        HttpResponse resp = null;
        String type = url.substring(url.lastIndexOf("/"));

        if (type.equals("/getPostList")) {
            if (userId != null) {
                url += "?userKey=" + userId;
                get = new HttpGet(url);
                try {
                    resp = client.execute(get);
                    result = (new BasicResponseHandler()).handleResponse(resp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    resp = client.execute(get);
                    result = (new BasicResponseHandler()).handleResponse(resp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (result != null)
                return result;

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
