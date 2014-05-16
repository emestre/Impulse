/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.impulse.impulse;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.Session;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;

import java.util.Date;

public class DrawerActivity extends ActionBarActivity {

    private static final String TAG = "DrawerActivity";
    private static final String SHARE_MESSAGE =
            "I want you to try this app called impulse: https://www.dropbox.com/s/i8jpcelxvvaaqw7/impulse.apk";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String userKey;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPageTitles;
    private Context context;

    public static boolean isDrawerOpen = false;
    private boolean atHomeScreen = true;
    private boolean allowDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        context = this;

        setContentView(R.layout.activity_drawer);

        mTitle = mDrawerTitle = getTitle();
        mPageTitles = getResources().getStringArray(R.array.pages_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPageTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        userKey = getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_navigation_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("thread_user")) {
            Log.i("CHANGE FRAG", "Sanity Check");
            final String userThread = getIntent().getExtras().getString("thread_user");
            final String postThread = getIntent().getExtras().getString("thread_post");
            setAtHomeScreen(false);
            RestClient client = new RestClient();
            client.getThread(userKey, userThread, postThread, new GetCallback() {
                @Override
                void onDataReceived(String response) {
                    if (response.equals(RestClient.ERROR)) {
                        Dialog.noInternetDialog(context);
                    }
                    else {
                        setFragment(MessageThreadFragment.create(response, userThread, postThread), 2, false);
                        Log.i("CHANGE FRAG", "WHY U NO WORK!?");
                    }
                }
            });
            getIntent().getExtras().remove("thread_user");
            getIntent().getExtras().remove("thread_post");

            Intent local = new Intent();
            local.setAction("com.impulse.DrawerActivity");
            sendBroadcast(local);
        }
        else {
            selectItem(0);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.impulse.DrawerActivity");
        registerReceiver(receiver, filter);

        showOnBoardingPopUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // hide all the action bar items when drawer is open
        isDrawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            if (isDrawerOpen) {
                if (item.getItemId() == R.id.action_delete) {
                    allowDelete = item.isVisible();
                }
                item.setVisible(false);
            }
            else {
                if (allowDelete) {
                    if (item.getItemId() != R.id.action_reply) {
                        item.setVisible(true);
                    }
                }
                else {
                    if (item.getItemId() != R.id.action_delete) {
                        item.setVisible(true);
                    }
                }
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.new_post) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public void setAtHomeScreen(boolean atHomeScreen) {
        this.atHomeScreen = atHomeScreen;
    }

    public void selectItem(final int position) {
        // update the main content by replacing fragments
        Fragment fragment;
        RestClient client = new RestClient();
        atHomeScreen = false;

        switch (position) {

            // home drawer click
            case 0:
                final Bundle extras = getIntent().getExtras();
                if (extras != null && extras.containsKey("USER_ID")) {

                    Log.d(TAG, "getting a certain user's posts...");
                            String response = extras.getString("USER_ID");
                            if (response == null || response.equals(RestClient.ERROR)) {
                                Dialog.noInternetDialog(context);
                            }
                            else {
                                Fragment frag = PostActivity.create(response, true);
                                setFragment(frag, position, true);
                            }

                    getIntent().removeExtra("USER_ID");
                } else {
                    atHomeScreen = true;
                    for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                        getSupportFragmentManager().popBackStack();
                    }
                    Log.d(TAG, "getting all posts...");

                    client.getPostList(userKey, 0.0, 0.0, new Date(), new GetCallback() {
                        @Override
                        void onDataReceived(String response) {
                            if (response.equals(RestClient.ERROR)) {
                                Dialog.noInternetDialog(context);
                            }
                            else {
                                Fragment frag = PostActivity.create(response, false);
                                setFragment(frag, position, false);
                            }
                        }
                    });
                }
                break;

            // profile drawer click
            case 1:
                for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                    getSupportFragmentManager().popBackStack();
                }
                String userId = getSharedPreferences("com.impulse",
                        Context.MODE_PRIVATE).getString("UserId", "");
                Bundle bundle = new Bundle();
                bundle.putString("id", userId);

                fragment = new ProfileActivity();
                fragment.setArguments(bundle);
                setFragment(fragment, position, false);

                break;

            // messages drawer click
            case 2:
                for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                    getSupportFragmentManager().popBackStack();
                }
                client = new RestClient();
                client.getActiveThreads(userKey, new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        if (response.equals(RestClient.ERROR)) {
                            Dialog.noInternetDialog(context);
                        }
                        else {
                            Fragment frag = MessagesFragment.create(response);
                            setFragment(frag, position, false);
                        }
                    }
                });
                break;

            // share button
            case 3:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, this.SHARE_MESSAGE);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Try this new app called impulse");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share this app using..."));
                break;

            // logout drawer click
            case 4:
                Session session = Session.getActiveSession();
                session.closeAndClearTokenInformation();

                String regId = getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("registration_id", "");
                final Context context = this;

                new RestClient().logout(userKey, regId, new PostCallback() {
                    @Override
                    public void onPostSuccess(String result) {
                        if (result.equals(RestClient.ERROR)) {
                            Dialog.noInternetDialog(context);
                        }
                        else {
                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                break;
        }
    }

    public void setFragment(Fragment fragment, int position, boolean addToBackStack) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction()
                .replace(R.id.content_frame, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        //setTitle(mPageTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void showOnBoardingPopUp() {

        final SharedPreferences preferences = this.getPreferences(Activity.MODE_PRIVATE);

        if (!preferences.contains("first run")) {
            new ShowcaseView.Builder(this, true)
                    .setContentTitle("Live Posts")
                    .setContentText("See what people around you are doing!\n" +
                            "Our live feed shows posts from users\nin your area. " +
                            "Posts are time sensitive,\nso you better act now!")
                    .setStyle(R.style.ImpulseShowcaseView)
                    .hideOnTouchOutside()
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("first run", false);
                            editor.commit();
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {}

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {
                            showcaseView.setShouldCentreText(true);
                        }
                    })
                    .build();
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (!atHomeScreen && getSupportFragmentManager().getBackStackEntryCount() == 0) {
            getIntent().removeExtra("USER_ID");
            selectItem(0);
        } else {
            super.onBackPressed();
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}