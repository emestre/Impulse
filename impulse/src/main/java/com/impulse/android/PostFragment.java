package com.impulse.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

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
    private ImageView mPostImage;
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

        initLayout();

        return view;
    }

    private void initLayout() {
        // load the post image
        Picasso.with(getActivity().getApplicationContext())
                .load(RestClient.getFile(mPost.fileName, "full"))
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
