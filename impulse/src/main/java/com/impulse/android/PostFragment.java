package com.impulse.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PostFragment extends Fragment {

    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;
    private Post post;
    private ImageView image;
    private TextView userName;
    private TextView mutualFriends;
    private TextView caption;
    private TextView timeout;

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static PostFragment create(int pageNumber, Post post) {
        PostFragment fragment = new PostFragment(post);
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PostFragment(Post post) {
        this.post = post;
    }

    public int getPageNumber() {
        return mPageNumber;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.post_fragment, container, false);

        if(view == null || post == null)
            return view;

        image = (ImageView) view.findViewById(R.id.post_image);
        userName = (TextView) view.findViewById(R.id.post_user);
        mutualFriends = (TextView) view.findViewById(R.id.post_mutual_friends);
        caption = (TextView) view.findViewById(R.id.post_caption);
        timeout = (TextView) view.findViewById(R.id.post_timeout);

        int firstQuote = post.fileName.indexOf("\"") + 1;
        int lastQuote = post.fileName.lastIndexOf("\"");
        String userId = post.fileName.substring(firstQuote, lastQuote);

        Session session = Session.getActiveSession();
        getUserName(session, userId);
        getMutualFriendsCount(session, userId);

        caption.setText(post.caption);
        timeout.setText(post.timeOut);
        Picasso.with(getActivity().getApplicationContext())
                .load(RestClient.getFile(post.fileName))
                .rotate(post.rotation).into(image);
        return view;
    }

    private void getUserName(Session session, String userId) {
        Bundle requestBundle = new Bundle();
        requestBundle.putString("fields", "name");
        new Request(session, "/" + userId, requestBundle, HttpMethod.GET, new Request.Callback() {
                    public void onCompleted(Response response) {
                        JSONObject json = response.getGraphObject().getInnerJSONObject();
                        if (json == null) {
                            userName.setText("Impulse");
                            return;
                        }
                        JsonElement elem = new JsonParser().parse(json.toString());
                        userName.setText(elem.getAsJsonObject().get("name").getAsString().split(" ")[0]);
                    }
                }
        ).executeAsync();
    }

    private void getMutualFriendsCount(Session session, String userId) {
        String myId = getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");
        if (!myId.equals(userId)) {
            new Request(session, "/" + userId + "/mutualFriends/" + myId, null, HttpMethod.GET, new Request.Callback() {
                        public void onCompleted(Response response) {
                            Log.i("Response", response.toString());
                            mutualFriends.setText(parseFriends(response) + " mutual friends");
                        }
                    }
            ).executeAsync();
        }
    }

    private int parseFriends(Response response) {
        GraphObject results = response.getGraphObject();
        JSONObject json = results.getInnerJSONObject();
        JsonElement elem = new JsonParser().parse(json.toString());
        JsonElement data = elem.getAsJsonObject().get("data");
        JsonArray mutualFriends = data.getAsJsonArray();
        return mutualFriends.size();
    }
}
