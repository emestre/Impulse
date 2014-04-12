package com.impulse.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

public class PostFragment extends Fragment {

    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    private static final String TAG = "PostFragment";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;
    private Post mPost;
    private ImageView mUserImage;
    private ImageView mPostImage;
    private TextView mUserName;
    private TextView mCaption;
    private ImageView mCaptionImage;
    private TextView mTimeout;
    private TextView mLocation;
    private ImageView mLocationPin;
    private Button mButtonLike;
    private TextView mLikes;
    // the facebook ID of the current user, whoever is logged in to this instance
    private String mUserId;

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

    public PostFragment() {
    }


    public PostFragment(Post post) {
        this.mPost = post;
    }

    public int getPageNumber() {
        return mPageNumber;
    }

    public Post getPost() {
        return mPost;
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

        if(view == null || mPost == null)
            return view;

        // get the user's unique facebook ID from shared preferences
        mUserId = getActivity().getSharedPreferences("com.impulse",
                Context.MODE_PRIVATE).getString("UserId", "");

        mPostImage = (ImageView) view.findViewById(R.id.post_image);
        mUserImage = (ImageView) view.findViewById(R.id.post_userpicture);
        mUserName = (TextView) view.findViewById(R.id.post_user);
        mCaption = (TextView) view.findViewById(R.id.post_caption);
        mCaptionImage = (ImageView) view.findViewById(R.id.caption_blurb);
        mTimeout = (TextView) view.findViewById(R.id.post_timeout);
        mLocation = (TextView) view.findViewById(R.id.post_location);
        mLocationPin = (ImageView) view.findViewById(R.id.location_pin);
        mButtonLike = (Button) view.findViewById(R.id.button_like);
        mLikes = (TextView) view.findViewById(R.id.post_likes);

        initLayout();

        return view;
    }

    private void initLayout() {
        Session session = Session.getActiveSession();
        getUserName(session, mPost.userKey);
        mUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserProfile();
            }
        });

        // populate caption and check in text fields
        // hide image icons if fields are empty
        mCaption.setText(mPost.caption);
        if (mPost.caption.equals(""))
            mCaptionImage.setVisibility(View.GONE);
        mTimeout.setText(mPost.timeOut + " left");
        mLocation.setText(mPost.location);
        if (mPost.location.equals(""))
            mLocationPin.setVisibility(View.GONE);

        // load the post image
        Picasso.with(getActivity().getApplicationContext())
                .load(RestClient.getFile(mPost.fileName))
                .fit()
                .into(mPostImage);
        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FullScreenActivity.class);
                intent.putExtra("FILE_PATH", mPost.fileName);
                getActivity().startActivity(intent);
            }
        });

        // load the user profile picture
        Picasso.with(getActivity().getApplicationContext())
                .load("https://graph.facebook.com/" + mPost.userKey + "/picture?type=normal&redirect=true&width=45&height=45")
                .into(mUserImage);
        // set profile picture link to user's profile
        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserProfile();
            }
        });

        // handle the liking functionality
        mLikes.setText(mPost.numLikes + " likes");
        if (mPost.liked) {
            mButtonLike.setEnabled(false);
            mButtonLike.setText("Liked");
        }
        else {
            mButtonLike.setEnabled(true);
            mButtonLike.setText("Like");

            mButtonLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RestClient client = new RestClient();
                    client.likePost(mPost.fileName, mUserId, new GetCallback() {
                        @Override
                        void onDataReceived(String response) {
                            mButtonLike.setEnabled(false);
                            mButtonLike.setText("Liked");
                            mLikes.setText(mPost.numLikes+1 + " likes");
                        }
                    });
                }
            });
        }
    }

    private void getUserName(Session session, String userId) {
        Bundle requestBundle = new Bundle();
        requestBundle.putString("fields", "name");
        new Request(session, "/" + userId, requestBundle, HttpMethod.GET, new Request.Callback() {
                    public void onCompleted(Response response) {
                        GraphObject obj = response.getGraphObject();
                        if (obj == null) {
                            mUserName.setText("Impulse");
                            return;
                        }
                        JSONObject json = response.getGraphObject().getInnerJSONObject();
                        JsonElement elem = new JsonParser().parse(json.toString());
                        mUserName.setText(elem.getAsJsonObject().get("name").getAsString().split(" ")[0]);
                    }
                }
        ).executeAsync();
    }

    // loads the post user's profile
    private void loadUserProfile() {
        Bundle bundle = new Bundle();
        bundle.putString("id", mPost.userKey);

        Fragment fragment = new ProfileActivity();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.content_frame, fragment).commit();
    }

//    private void getMutualFriendsCount(Session session, String userId) {
//        String myId = getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");
//        if (!myId.equals(userId)) {
//            new Request(session, "/" + userId + "/mutualFriends/" + myId, null, HttpMethod.GET, new Request.Callback() {
//                        public void onCompleted(Response response) {
//                            Log.i("Response", response.toString());
//                            mutualFriends.setText(parseFriends(response) + " mutual friends");
//                        }
//                    }
//            ).executeAsync();
//        }
//    }

//    private int parseFriends(Response response) {
//        GraphObject results = response.getGraphObject();
//        JSONObject json = results.getInnerJSONObject();
//        JsonElement elem = new JsonParser().parse(json.toString());
//        JsonElement data = elem.getAsJsonObject().get("data");
//        JsonArray mutualFriends = data.getAsJsonArray();
//        return mutualFriends.size();
//    }
}
