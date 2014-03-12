package com.impulse.android;

import android.app.Dialog;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.util.ArrayList;

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

    private String postList;

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
        if(postList != null)
            parsePosts(postList);
        NUM_PAGES = posts.size();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) root.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getActivity().getSupportFragmentManager());
        mPager.setPageMargin(5);
        mPager.setClipToPadding(false);
        mPager.setAdapter(mPagerAdapter);
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
            String caption = toAdd.get("caption").getAsString();
            String fileName = toAdd.get("fileName").getAsString();
            String timeOut = toAdd.get("timeout").getAsString();
            int rotation = toAdd.get("rotation").getAsInt();
            String calculatedTimeout = calculateTimeout(timeOut);

            if (calculatedTimeout.isEmpty())
                continue;

            Post newPost = new Post(lon, lat, caption, fileName, calculatedTimeout, rotation);
            posts.add(newPost);
        }
    }

    public static long getDateTimeDiff(DateTime date1, DateTime date2) {
        return date2.getMillis() - date1.getMillis();
    }

    private String calculateTimeout(String timeOut) {
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
            return PostFragment.create(position, posts.get(position));
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
