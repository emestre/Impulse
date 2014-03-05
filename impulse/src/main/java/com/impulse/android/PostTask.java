package com.impulse.android;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * An AsyncTask implementation for performing POSTs on the Hypothetical REST APIs.
 */
public class PostTask extends AsyncTask<String, String, String> {
    private String url;
    private RestTaskCallback callback;
    private String userKey;
    private String caption;
    private double latitude;
    private double longitude;
    private String filePath;
    private String extension;
    private int timeout;
    private int rotation;

    /**
     * Creates a new instance of PostTask with the specified URL, callback, and
     * request body.
     *
     * @param url The URL for the REST API.
     * @param callback The callback to be invoked when the HTTP request
     *            completes.
     * @param userKey The body of the POST request.
     *
     */
    public PostTask(String url, String userKey, RestTaskCallback callback){
        this.url = url;
        this.userKey = userKey;
        this.callback = callback;
    }

    public PostTask(String url, String userKey, String caption, double latitude, double longitude, String filePath, String extension, int timeout, int rotation, RestTaskCallback callback) {
        this.url = url;
        this.userKey = userKey;
        this.caption = caption;
        this.latitude = latitude;
        this.longitude = longitude;
        this.filePath = filePath;
        this.extension = extension;
        this.timeout = timeout;
        this.rotation = rotation;
        this.callback = callback;
    }


    @Override
    protected String doInBackground(String... arg0) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        HttpResponse resp = null;
        String type = url.substring(url.lastIndexOf("/"));

        if (type.equals("/createUser")) {
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("userKey", this.userKey));

            try {
                post.setEntity(new UrlEncodedFormEntity(list));
                resp = client.execute(post);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (resp != null)
               return Integer.toString(resp.getStatusLine().getStatusCode());

            return "Error Occurred";
        }

        else if (type.equals("/uploadFile")) {
            MultipartEntityBuilder entity = MultipartEntityBuilder.create();
            entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addTextBody("extension", this.extension);
            entity.addTextBody("userKey", this.userKey);
            entity.addTextBody("caption", this.caption);
            entity.addTextBody("latitude", String.valueOf(this.latitude));
            entity.addTextBody("longitude", String.valueOf(this.longitude));
            entity.addTextBody("timeout", String.valueOf(this.timeout));
            entity.addTextBody("rotation", String.valueOf(this.rotation));
            entity.addPart("image", new FileBody(new File(filePath)));

            try {
                post.setEntity(entity.build());
                resp = client.execute(post);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (resp != null)
                return Integer.toString(resp.getStatusLine().getStatusCode());

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
