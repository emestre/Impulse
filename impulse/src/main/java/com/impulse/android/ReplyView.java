package com.impulse.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;


/**
 * TODO: document your custom view class.
 */
public class ReplyView extends LinearLayout {

    private ImageView postThumbnail;
    private TextView postUser;
    private TextView postTimeout;
    private String userKey;

    public ReplyView(Context context, Reply reply) {
        super(context);

        initReply(context);
        Picasso.with(context).load(RestClient.getFile(reply.postId, "200", false)).fit().into(postThumbnail);
        if (reply.userKey.equals(userKey))
            postUser.setText("Your post");
        else
            getUserName(reply.userKey, true);
        postTimeout.setText(reply.timeout);
    }

    public ReplyView(Context context, Thread thread) {
        super(context);
        initThread(context);

        Picasso.with(context)
                .load("https://graph.facebook.com/" + thread.userKey + "/picture?type=normal&redirect=true&width=500&height=500")
                .fit()
                .into(postThumbnail);
        getUserName(thread.userKey, false);
    }

    private void initReply(Context context) {
        userKey = context.getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.post_reply_item, this, true);
        postThumbnail = (ImageView) view.findViewById(R.id.post_thumbnail);
        postUser = (TextView) view.findViewById(R.id.user_reply_name);
        postTimeout = (TextView) view.findViewById(R.id.post_reply_timeout);

    }

    private void initThread(Context context) {
        userKey = context.getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.post_thread_item, this, true);
        postThumbnail = (ImageView) view.findViewById(R.id.thread_user_thumbnail);
        postUser = (TextView) view.findViewById(R.id.thread_user_name);
    }


    private void getUserName(String userId, final boolean reply) {
        Bundle requestBundle = new Bundle();
        requestBundle.putString("fields", "name");
        Session session = Session.getActiveSession();
        new Request(session, "/" + userId, requestBundle, HttpMethod.GET, new Request.Callback() {
            public void onCompleted(Response response) {
                GraphObject obj = response.getGraphObject();
                if (obj == null) {
                    postUser.setText("Impulse's post");
                    return;
                }
                JSONObject json = response.getGraphObject().getInnerJSONObject();
                JsonElement elem = new JsonParser().parse(json.toString());
                String name = elem.getAsJsonObject().get("name").getAsString().split(" ")[0];
                if(reply)
                    postUser.setText(name + "'s post");
                else
                    postUser.setText(name);
            }
        }
        ).executeAsync();
    }
}
