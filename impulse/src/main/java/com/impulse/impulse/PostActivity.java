package com.impulse.impulse;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostActivity extends Fragment {

    private static final String TAG = "PostActivity";

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private int NUM_PAGES;
    private ArrayList<Post> posts;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;
    private String postList;
    private String myUserKey;
    private Post mCurrentPost;

    private SlidingDrawer mReplyDrawer;
    private Button mDrawerButton;
    private ImageView mCameraReply;
    private ImageView mVideoReply;
    private ImageView mMessageReply;
    private Button mButtonSend;
    private EditText mReplyEditText;

    private TextView mCaption;
    private ImageView mCaptionImage;
    private Button mButtonLike;

    private TextView mUserName;
    private ImageView mUserImage;
    private TextView mTimeout;
    private TextView mLocation;
    private ImageView mLocationPin;
    private TextView mLikes;
    private boolean myPosts;

    private ProgressDialog mDeleteProgress;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    public static PostActivity create(String posts, boolean myPosts) {
        return new PostActivity(posts, myPosts);
    }

    public PostActivity() {}

    public PostActivity(String postList, boolean myPosts) {
        this.postList = postList;
        this.myPosts = myPosts;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_post, container, false);

        posts = new ArrayList<Post>();
        if (postList != null)
            parsePosts(postList);
        NUM_PAGES = posts.size();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) root.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getActivity().getSupportFragmentManager());

    /*    mReplyDrawer = (SlidingDrawer) root.findViewById(R.id.bottom);
        mDrawerButton = (Button) root.findViewById(R.id.handle);
        mCameraReply = (ImageView) root.findViewById(R.id.camera_reply);
        mVideoReply = (ImageView) root.findViewById(R.id.video_reply);
        mMessageReply = (ImageView) root.findViewById(R.id.message_reply);
        mButtonSend = (Button) root.findViewById(R.id.reply_button);
        mReplyEditText = (EditText) root.findViewById(R.id.reply_editText); */

        mUserImage = (ImageView) root.findViewById(R.id.post_userpicture);
        mUserName = (TextView) root.findViewById(R.id.post_user);
        mTimeout = (TextView) root.findViewById(R.id.post_timeout);
        mLocation = (TextView) root.findViewById(R.id.post_location);
        mLocationPin = (ImageView) root.findViewById(R.id.location_pin);
        mLikes = (TextView) root.findViewById(R.id.post_likes);
        mCaption = (TextView) root.findViewById(R.id.post_caption);
        mCaptionImage = (ImageView) root.findViewById(R.id.caption_blurb);
        mButtonLike = (Button) root.findViewById(R.id.button_like);

        myUserKey = getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");
//        mPager.setPageMargin(5);
        mPager.setClipToPadding(false);
        mPager.setAdapter(mPagerAdapter);
        mPager.getCurrentItem();

        if (NUM_PAGES != 0) {
            mCurrentPost = posts.get(0);
            setPost();
            // tell the host activity that this fragment has an options menu
            setHasOptionsMenu(true);
        }

        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserProfile();
            }
        });
        mUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserProfile();
            }
        });

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPost = posts.get(position);

                if (mCurrentPost.userKey.equals(myUserKey)) {
                    Log.i("REPLYDRAWER", "MY POST");
                    getActivity().supportInvalidateOptionsMenu();
                }
                else {
                    Log.i("REPLYDRAWER", "NOT MY POST");
                    getActivity().supportInvalidateOptionsMenu();
                }

                setPost();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.post, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu called");

        if (!DrawerActivity.isDrawerOpen) {
            if (mCurrentPost.userKey.equals(myUserKey)) {
                menu.findItem(R.id.action_delete).setVisible(true);
                menu.findItem(R.id.action_reply).setVisible(false);
            }
            else {
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_reply).setVisible(true);
            }
            if (mCurrentPost.liked) {
                menu.findItem(R.id.action_like).setEnabled(false);
                menu.findItem(R.id.action_like).getIcon().setAlpha(130);
            }
            else {
                menu.findItem(R.id.action_like).setEnabled(true);
                menu.findItem(R.id.action_like).getIcon().setAlpha(255);
            }
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reply:
                RestClient client = new RestClient();
                client.getThread(myUserKey, mCurrentPost.userKey, mCurrentPost.fileName, new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        if (response.equals(RestClient.ERROR)) {
                            Dialog.noInternetDialog(getActivity());
                        }
                        else {
                            DrawerActivity activity = (DrawerActivity) getActivity();
                            activity.setAtHomeScreen(false);
                            activity.setFragment(MessageThreadFragment.create(response,
                                    mCurrentPost.userKey, mCurrentPost.fileName), 2, false);
                        }
                    }
                });
                return true;

            case R.id.action_delete:
                buildDeleteConfirmDialog().show();
                return true;

            case R.id.action_like:
                likeButtonClick();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AlertDialog buildDeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Post");
        builder.setMessage("Are you sure you want to delete this post?");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // display a loading spinner while the post is being deleted
                mDeleteProgress = new ProgressDialog(getActivity());
                mDeleteProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mDeleteProgress.setTitle("Deleting Post...");
                mDeleteProgress.setMessage("Your post is being deleted.");
                mDeleteProgress.setIndeterminate(true);
                mDeleteProgress.setCancelable(false);
                mDeleteProgress.show();

                final int curIndex = mPager.getCurrentItem();
                Log.d(TAG, "removing post: " + posts.get(curIndex).fileName);
                new RestClient().removeFile(posts.get(curIndex).fileName, new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        if (response.equals(RestClient.ERROR)) {
                            Dialog.noInternetDialog(getActivity());
                        }
                        else {
                            Log.d(TAG, "removeFile returned");

                            if (myPosts) {
                                Log.d(TAG, "getting all of my posts");

                                new RestClient().getPostList(new GetCallback() {
                                    @Override
                                    void onDataReceived(String response) {
                                        if (response.equals(RestClient.ERROR)) {
                                            Dialog.noInternetDialog(getActivity());
                                        }
                                        else {
                                            updatePosts(response, curIndex);
                                        }
                                    }
                                }, myUserKey);
                            } else {
                                Log.d(TAG, "getting all the posts");

                                new RestClient().getPostList(myUserKey, 0.0, 0.0, new Date(), new GetCallback() {
                                    @Override
                                    void onDataReceived(String response) {
                                        if (response.equals(RestClient.ERROR)) {
                                            Dialog.noInternetDialog(getActivity());
                                        }
                                        else {
                                            updatePosts(response, curIndex);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private void updatePosts(String response, int index) {
        postList = response;
        posts.clear();
        parsePosts(postList);
        NUM_PAGES = posts.size();
        mPagerAdapter.notifyDataSetChanged();
        if (NUM_PAGES > 1) {
            if (index == NUM_PAGES) {
                mPager.setCurrentItem(index-1, true);
                mCurrentPost = posts.get(index-1);
                setPost();
            }
            else {
                mPager.setCurrentItem(index, true);
                mCurrentPost = posts.get(index);
                setPost();
            }
        }
        getActivity().supportInvalidateOptionsMenu();
        mDeleteProgress.dismiss();
    }

    private void setPost() {
        Session session = Session.getActiveSession();
        mCaption.setText(mCurrentPost.caption);
        if (mCurrentPost.caption.equals(""))
            mCaptionImage.setVisibility(View.GONE);
        else
            mCaptionImage.setVisibility(View.VISIBLE);

        mUserName.setText("");
        getUserName(session, mCurrentPost.userKey);
        mLikes.setText(mCurrentPost.numLikes + "");
        Picasso.with(getActivity().getApplicationContext())
                .load("https://graph.facebook.com/" + mCurrentPost.userKey +
                        "/picture?type=normal&redirect=true&width=500&height=500")
                .transform(new RoundedTransformation(45, 2))
                .fit()
                .into(mUserImage);
        mTimeout.setText(mCurrentPost.timeOut);

        mLocation.setText(mCurrentPost.location);
        if (mCurrentPost.location.equals(""))
            mLocationPin.setVisibility(View.GONE);
        else
            mLocationPin.setVisibility(View.VISIBLE);

        if (mCurrentPost.liked) {
            getActivity().invalidateOptionsMenu();

            mButtonLike.setEnabled(false);
            mButtonLike.setText("Liked");
        }
        else {
            getActivity().supportInvalidateOptionsMenu();

            mButtonLike.setEnabled(true);
            mButtonLike.setText("Like");

            mButtonLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    likeButtonClick();
                }
            });
        }
    }

    private void likeButtonClick() {
        new RestClient().likePost(mCurrentPost.fileName, myUserKey, new GetCallback() {
            @Override
            void onDataReceived(String response) {
                if (response.equals(RestClient.ERROR)) {
                    Dialog.noInternetDialog(getActivity());
                }
                else if (Integer.parseInt(response) == HttpStatus.SC_OK) {
                    Log.i(TAG, "like post SUCCEEDED");
                    mButtonLike.setEnabled(false);
                    mButtonLike.setText("Liked");

                    mCurrentPost.liked = true;
                    ++mCurrentPost.numLikes;
                    mLikes.setText(mCurrentPost.numLikes + "");

                    getActivity().supportInvalidateOptionsMenu();
                }
            }
        });
    }

    private void getUserName(Session session, String userId) {
        Request.newGraphPathRequest(session, userId, new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                GraphUser user = response.getGraphObjectAs(GraphUser.class);

                // set user name
                mUserName.setText(user.getName().split(" ")[0]);

                // get the user's birthday to calculate age
                Log.d(TAG, "birthday: " + user.getBirthday());
                try {
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
                    DateTime dob = formatter.parseDateTime(user.getBirthday());
                    DateTime now = new DateTime();
                    // set this user's age
                    mUserName.setText(mUserName.getText() + ", " + Years.yearsBetween(dob, now).getYears());
                }
                catch (NullPointerException eNull) {
                    Log.d(TAG, "user's birthday returned NULL");
                }
                catch (IllegalArgumentException eBad) {
                    Log.d(TAG, "user doesn't share birthday year on facebook");
                }
            }
        }).executeAsync();
    }

    private void loadUserProfile() {
        Post post = posts.get(mPager.getCurrentItem());
        Bundle bundle = new Bundle();
        bundle.putString("id", post.userKey);

        ((DrawerActivity) getActivity()).setAtHomeScreen(false);
        Fragment fragment = new ProfileActivity();
        fragment.setArguments(bundle);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private void parsePosts(String response) {
        Log.i("Response", response);
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonArray();
        for (JsonElement post : results) {
            JsonObject toAdd = post.getAsJsonObject();

            double lon = toAdd.get("longitude").getAsDouble();
            double lat = toAdd.get("latitude").getAsDouble();

            String caption = toAdd.get("caption").getAsString();
            String fileName = toAdd.get("fileName").getAsString();
            String timeOut = toAdd.get("timeout").getAsString();
            String userKey = toAdd.get("userKey").getAsString();
            String location = toAdd.get("location").getAsString();

            String dateStr = toAdd.get("timestamp").getAsString();
            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            Date date = null;
            try {
                date = formatter.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String calculatedTimeout = calculateTimeout(timeOut);
            if (calculatedTimeout.isEmpty())
                continue;

            boolean liked = toAdd.get("liked").getAsBoolean();
            long likes = toAdd.get("likes").getAsLong();

            Post newPost = new Post(lon, lat, caption, fileName, calculatedTimeout,
                    userKey, date, liked, likes, location);
            posts.add(newPost);
        }
    }

    public static long getDateTimeDiff(DateTime date1, DateTime date2) {
        return date2.getMillis() - date1.getMillis();
    }

    public static String calculateTimeout(String timeOut) {
        String calculatedTimeout;
        DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
        DateTime dt = formatter.parseDateTime(timeOut);
        DateTime currentTime = new DateTime();
        //long diffInDays = getDateTimeDiff(currentTime, dt) / (1000 * 60 * 60 * 24);
        long diffInHours = getDateTimeDiff(currentTime, dt) / (1000 * 60 * 60);
        //long diffInMinutes = getDateTimeDiff(currentTime, dt) / (1000 * 60);

        if (diffInHours > 0) {
            calculatedTimeout = "expires in " + diffInHours + " hrs";
        }
        else {
            calculatedTimeout = "expires in less than 1 hr";
        }
        /*if (diffInDays > 0) {
            diffInHours -= diffInDays * 24;
            diffInMinutes -= (diffInDays * 24 * 60 + diffInHours * 60);
            calculatedTimeout = diffInDays + " days " + diffInHours + " hrs " + diffInMinutes + " mins";
        } else if (diffInHours > 0) {
            diffInMinutes -= diffInHours * 60;
            calculatedTimeout = diffInHours + " hrs " + diffInMinutes + " mins";
        } else if (diffInMinutes > 0) {
            calculatedTimeout = diffInMinutes + " mins";
        } else
            calculatedTimeout = "";*/

        return calculatedTimeout;
    }

    /**
     * A simple pager adapter that represents 5 {@link PostFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            String userKey = PostActivity.this.getActivity().getSharedPreferences(
                    "com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

            RestClient client = new RestClient();
            if (position == PostActivity.this.NUM_PAGES - 1 && !myPosts && NUM_PAGES >= 25) {
                client.getPostList(userKey, 0.0, 0.0, posts.get(position).date, new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        if (response.equals(RestClient.ERROR)) {
                            Dialog.noInternetDialog(getActivity());
                        }
                        else {
                            parsePosts(response);
                            NUM_PAGES = posts.size();
                            PostActivity.this.mPagerAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            return PostFragment.create(position, posts.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
