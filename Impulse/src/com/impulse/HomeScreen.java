package com.impulse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

public class HomeScreen extends Activity {

    private Button profileButton, logoutButton, viewPostsButton;
    private long startTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        profileButton = (Button) findViewById(R.id.profile_button);
        logoutButton = (Button) findViewById(R.id.logout_button);
        viewPostsButton = (Button) findViewById(R.id.view_posts_button);
        profileButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeScreen.this,
                        ProfileActivity.class);
                startActivity(intent);
            }

        });


        viewPostsButton.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(HomeScreen.this,
                        PostActivity.class);
                RestClient client = new RestClient();
                client.getPostList(new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        intent.putExtra("POST_LIST", response);
                        startActivity(intent);
                    }
                });

            }

        });

        logoutButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                logout();
                Intent intent = new Intent(HomeScreen.this,
                        MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        Session session = Session.getActiveSession();
        Request.newMeRequest(session, new Request.GraphUserCallback() {

            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    SharedPreferences prefs = getSharedPreferences("com.impulse", Context.MODE_PRIVATE);
                    if (!prefs.getString("UserId", "").equals(user.getId())) {
                        prefs.edit().putString("UserId", user.getId()).commit();
                        registerUser(prefs, user.getId());
                    }
                }
            }
        }).executeAsync();


    }

    public void cameraButtonClick(View view) {
        Intent intent = new Intent(HomeScreen.this, CameraActivity.class);
        startActivity(intent);
    }

    private void registerUser(final SharedPreferences prefs, final String userId) {
        RestClient db_client = new RestClient();
        db_client.postUser(userId, new PostCallback() {
            @Override
            public void onPostSuccess(String result) {
                Toast.makeText(HomeScreen.this, result, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onBackPressed() {
        finish();
    }

    public void logout() {
        Session session = Session.getActiveSession();
        session.closeAndClearTokenInformation();
        finish();
    }

}
