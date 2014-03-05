package com.impulse.android;

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
import com.squareup.picasso.Picasso;

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
        image = (ImageView) view.findViewById(R.id.post_image);
        userName = (TextView) view.findViewById(R.id.post_user);
        mutualFriends = (TextView) view.findViewById(R.id.post_mutual_friends);
        caption = (TextView) view.findViewById(R.id.post_caption);
        timeout = (TextView) view.findViewById(R.id.post_timeout);


        int firstQuote = post.fileName.indexOf("\"");
        int lastQuote = post.fileName.lastIndexOf("\"");
        String userId = post.fileName.substring(firstQuote, lastQuote);
        Log.i("FILE", post.fileName);
        Log.i("USER_ID", userId + "     \"");
        Session session = Session.getActiveSession();

        Bundle requestBundle = new Bundle();
        requestBundle.putString("fields", "name");
        new Request(
                session,
                "/" + userId,
                requestBundle,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        Log.i("RESPONSE", response.toString());
                        response.getGraphObject();
                    }
                }
        ).executeAsync();

        if (post == null)
            return view;
        caption.setText(post.caption);
        Picasso.with(getActivity().getApplicationContext()).load(RestClient.getFile(post.fileName)).into(image);
        return view;
    }
}
