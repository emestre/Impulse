package com.impulse.android;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.facebook.Response;
import com.facebook.model.GraphObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PostActivity extends FragmentActivity {

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

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        posts = new ArrayList<Post>();
        String response = getIntent().getExtras().get("POST_LIST").toString();
        parsePosts(response);
        NUM_PAGES = posts.size();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    invalidateOptionsMenu();
                }
            }
        });
    }

    private void parsePosts(String response) {
        Log.i("Response", response);
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonArray();
        for(JsonElement post : results) {
            JsonObject toAdd = post.getAsJsonObject();
            double lon = toAdd.get("longitude").getAsDouble();
            double lat = toAdd.get("latitude").getAsDouble();
            String caption = toAdd.get("caption").getAsString();
            String fileName = toAdd.get("fileName").getAsString();
            String timeOut = toAdd.get("timeout").getAsString();

            String calculatedTimeout = calculateTimeout(timeOut);

            if(calculatedTimeout.isEmpty())
                continue;

            Post newPost = new Post(lon, lat, caption, fileName, calculatedTimeout);
            posts.add(newPost);
        }
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    private String calculateTimeout(String timeOut) {
        String calculatedTimeout = null;
        try {
            Date result = null;
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.US);
            Date currentTime = new Date();
            result = df.parse(timeOut);
            Log.i("DATES", result.toString() + " " + currentTime.toString());
            long diffInDays = getDateDiff(currentTime, result, TimeUnit.DAYS);
            long diffInHours = getDateDiff(currentTime, result,TimeUnit.HOURS);
            long diffInMinutes = getDateDiff(currentTime, result, TimeUnit.MINUTES);
            if(diffInDays > 0) {
                diffInHours -= diffInDays * 24;
                diffInMinutes -= (diffInDays * 24 * 60 + diffInHours * 60);
                calculatedTimeout = diffInDays + " days " + diffInHours + " hrs " + diffInMinutes + " mins";
            }
            else if(diffInHours > 0) {
                diffInMinutes -= diffInHours * 60;
                calculatedTimeout = diffInHours + " hrs " + diffInMinutes + " mins";
            }
            else if(diffInMinutes > 0) {
                calculatedTimeout = diffInMinutes + " mins";
            }
            else
                return "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calculatedTimeout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.post_menu, menu);

        menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);
        menu.findItem(R.id.action_next).setEnabled(mPager.getCurrentItem() < mPagerAdapter.getCount() - 1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reply:
                final Dialog dialog = new Dialog(this);
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
