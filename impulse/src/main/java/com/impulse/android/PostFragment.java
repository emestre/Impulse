package com.impulse.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;

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
    private View mDialogView;
    private AlertDialog mReply;
    private boolean init = false;
    // the facebook ID of the current user, whoever is logged in to this instance
    private String mUserId;
    private Button mMessageReply;
    private String myUserKey;
    private String filePathToSend;

    private boolean allowReply = false;

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
        this.mPost = post;
    }

    public int getPageNumber() {
        return mPageNumber;
    }

    public Post getPost() {
        return mPost;
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        getActivity().getMenuInflater().inflate(R.menu.post_fragment, menu);
//
//        if (allowReply) {
//            menu.findItem(R.id.action_reply).setVisible(true);
//        }
//        else {
//            menu.findItem(R.id.action_reply).setVisible(false);
//        }
//
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_reply:
//                mReply.show();
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

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
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    initLayout();
                }
            });
        }

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

//    @Override
//    public void onActivityResult(int requestCode, int resultCode,
//                                    Intent imageReturnedIntent) {
//        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
//
//        switch(requestCode) {
//            case 100:
//                if(resultCode == Activity.RESULT_OK){
//                    Uri selectedImage = imageReturnedIntent.getData();
//                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//                    Cursor cursor = getActivity().getContentResolver().query(
//                            selectedImage, filePathColumn, null, null, null);
//                    cursor.moveToFirst();
//
//                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                    filePathToSend = cursor.getString(columnIndex);
//                    cursor.close();
//                    Log.i("DPOKADW", filePathToSend);
//                    showDialogSendPicture();
//                }
//        }
//    }

//    private void showDialogSendPicture() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Send Picture");
//        builder.setMessage("Do you want to send this picture?");
//        builder.setCancelable(false);
//        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                RestClient client = new RestClient();
//                client.createMessage(myUserKey, mPost.userKey, mPost.fileName, filePathToSend, "image", new PostCallback() {
//                    @Override
//                    public void onPostSuccess(String result) {
//                        if (Integer.parseInt(result) == HttpStatus.SC_OK) {
//                            Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//                            Toast.makeText(getActivity(), "Message Did Not Send", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//                dialog.dismiss();
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        builder.create().show();
//    }
}
