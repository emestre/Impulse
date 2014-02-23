package com.impulse;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private ArrayList<String> friends;

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

    public PostTask(String url, ArrayList<String> friends, RestTaskCallback callback) {
        this.url = url;
        this.friends = friends;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... arg0) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        HttpGet get = new HttpGet(url);
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

        else if (type.equals("/getFriendList")) {
            String result = null;
            HttpParams params = new BasicHttpParams();
            List<NameValuePair> list = new ArrayList<NameValuePair>();

            params.setParameter("size", friends.size());
            for (int i = 0; i < friends.size(); i++)
                params.setParameter(String.valueOf(i), friends.get(i));

            try {
                get.setParams(params);
                resp = client.execute(get);
                result = (new BasicResponseHandler()).handleResponse(resp);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
