package com.impulse.android;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SlidingDrawer;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostActivity extends Fragment {

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
    private Boolean start = true;
    private String postList;
    private String myUserKey;

    private SlidingDrawer mReplyDrawer;
    private Button mDrawerButton;
    private Button mSendButton;
    private EditText mReplyEditText;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    public PostActivity() {
    }

    public static PostActivity create(String posts) {
        return new PostActivity(posts);
    }

    public PostActivity(String postList) {
        this.postList = postList;
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
        mReplyDrawer = (SlidingDrawer) root.findViewById(R.id.bottom);
        mDrawerButton = (Button) root.findViewById(R.id.handle);
        mSendButton = (Button) root.findViewById(R.id.reply_button);
        mReplyEditText = (EditText) root.findViewById(R.id.reply_editText);

        myUserKey = getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");
        mPager.setPageMargin(5);
        mPager.setClipToPadding(false);
        mPager.setAdapter(mPagerAdapter);
        mPager.getCurrentItem();

        mReplyDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                mDrawerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_caret_down, 0, R.drawable.ic_caret_down, 0);
                Log.i("CURRENT_POST", mPager.getCurrentItem() + "");
            }
        });

        mReplyDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                mDrawerButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_caret_up, 0, R.drawable.ic_caret_up, 0);
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curPos = mPager.getCurrentItem();
                Post currentPost = posts.get(curPos);

                RestClient client = new RestClient();
                String postId = currentPost.fileName;
                String userKey = currentPost.userKey;
                String message = mReplyEditText.getText().toString();
                client.createMessage(myUserKey, userKey, postId, message, new PostCallback() {
                    @Override
                    public void onPostSuccess(String result) {
                        if(result.equals("200"))
                            Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), "Message Did Not Send", Toast.LENGTH_SHORT).show();
                    }
                });

                mReplyEditText.setText("");
                mReplyDrawer.animateClose();

            }
        });


   /*     mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActivity().invalidateOptionsMenu();
                }
            }
        });
        */

        return root;
    }

    private void parsePosts(String response) {
        Log.i("Response", response);
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonArray();
        for (JsonElement post : results) {
            JsonObject toAdd = post.getAsJsonObject();

            double lon = toAdd.get("longitude").getAsDouble();
            double lat = toAdd.get("latitude").getAsDouble();
            int rotation = toAdd.get("rotation").getAsInt();

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
                    rotation, userKey, date, liked, likes, location);
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
        long diffInDays = getDateTimeDiff(currentTime, dt) / (1000 * 60 * 60 * 24);
        long diffInHours = getDateTimeDiff(currentTime, dt) / (1000 * 60 * 60);
        long diffInMinutes = getDateTimeDiff(currentTime, dt) / (1000 * 60);

        if (diffInDays > 0) {
            diffInHours -= diffInDays * 24;
            diffInMinutes -= (diffInDays * 24 * 60 + diffInHours * 60);
            calculatedTimeout = diffInDays + " days " + diffInHours + " hrs " + diffInMinutes + " mins";
        } else if (diffInHours > 0) {
            diffInMinutes -= diffInHours * 60;
            calculatedTimeout = diffInHours + " hrs " + diffInMinutes + " mins";
        } else if (diffInMinutes > 0) {
            calculatedTimeout = diffInMinutes + " mins";
        } else
            calculatedTimeout = "";

        return calculatedTimeout;
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getActivity().getMenuInflater().inflate(R.menu.post_menu, menu);

        menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);
        menu.findItem(R.id.action_next).setEnabled(mPager.getCurrentItem() < mPagerAdapter.getCount() - 1);
        return true;
    } */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reply:
                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_reply);

                dialog.show();
                return true;
            case R.id.action_previous:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                return true;

            case R.id.action_next:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                return true;
        }


        return super.onOptionsItemSelected(item);
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
            String userKey = PostActivity.this.getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");
            RestClient client = new RestClient();
            if (position == 0 && !start) {
                client.getPostList(userKey, 0.0, 0.0, new Date(), new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        posts.clear();
                        start = true;
                        parsePosts(response);
                        NUM_PAGES = posts.size();
                        PostActivity.this.mPagerAdapter.notifyDataSetChanged();
                    }
                });
                return PostFragment.create(position, posts.get(position));

            }
            if (start)
                start = false;
            if (position == PostActivity.this.NUM_PAGES - 1) {
                client.getPostList(userKey, 0.0, 0.0, posts.get(position).date, new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        parsePosts(response);
                        NUM_PAGES = posts.size();
                        PostActivity.this.mPagerAdapter.notifyDataSetChanged();
                    }
                });
            }

            return PostFragment.create(position, posts.get(position));
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
