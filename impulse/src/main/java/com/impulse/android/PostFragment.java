package com.impulse.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
    private View view;
    private boolean init = false;
    // the facebook ID of the current user, whoever is logged in to this instance
    private String mUserId;
    private Button mMessageReply;
    private EditText mReplyEditText;
    private Button mButtonSend;
    private String myUserKey;

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
        myUserKey = getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View view = inflater.inflate(R.layout.post_fragment, container, false);

        if (view == null || mPost == null)
            return view;

        // get the user's unique facebook ID from shared preferences
        mUserId = getActivity().getSharedPreferences("com.impulse",
                Context.MODE_PRIVATE).getString("UserId", "");

        mPostImage = (ImageView) view.findViewById(R.id.post_image);
        mMessageReply = (Button) view.findViewById(R.id.message_reply_button);
        mReplyEditText = (EditText) view.findViewById(R.id.reply_editText);
        mButtonSend = (Button) view.findViewById(R.id.reply_button);

        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    initLayout();
                }
            });
        }
        if (mPost.userKey.equals(myUserKey))
            mMessageReply.setVisibility(View.GONE);
        else
            mMessageReply.setVisibility(View.VISIBLE);

        return view;
    }

    private void initLayout() {
        // load the post image
        int width = mPostImage.getWidth();
        int height = mPostImage.getHeight();


        Picasso.with(getActivity())
                .load(RestClient.getFile(mPost.fileName, "full", false))
                .resize(width, height)
                .centerCrop()
                        // .fit()
                .into(mPostImage);
        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FullScreenActivity.class);
                intent.putExtra("FILE_PATH", mPost.fileName);
                getActivity().startActivity(intent);
            }
        });


        mMessageReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReplyEditText.setVisibility(View.VISIBLE);
                mButtonSend.setVisibility(View.VISIBLE);
                mMessageReply.setVisibility(View.INVISIBLE);
                mReplyEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mReplyEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        mButtonSend.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if(visibility == View.VISIBLE)
                    mMessageReply.setVisibility(View.INVISIBLE);
                else
                    mMessageReply.setVisibility(View.VISIBLE);
            }
        });

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestClient client = new RestClient();
                String postId = mPost.fileName;
                String userKey = mPost.userKey;
                String message = mReplyEditText.getText().toString();
                client.createMessage(myUserKey, userKey, postId, message, new PostCallback() {
                    @Override
                    public void onPostSuccess(String result) {
                        if (result.equals("200"))
                            Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), "Message Did Not Send", Toast.LENGTH_SHORT).show();
                        InputMethodManager inputManager = (InputMethodManager)
                                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                });
                mReplyEditText.setVisibility(View.GONE);
                mButtonSend.setVisibility(View.GONE);
                mMessageReply.setVisibility(View.VISIBLE);
                mReplyEditText.setText("");
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
