package com.impulse.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

public class HomeScreen extends Fragment {

    private Button profileButton, logoutButton, viewPostsButton;
    private long startTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_main, container, false);
        profileButton = (Button) root.findViewById(R.id.profile_button);
        logoutButton = (Button) root.findViewById(R.id.logout_button);
        viewPostsButton = (Button) root.findViewById(R.id.view_posts_button);
        profileButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),
                        ProfileActivity.class);
                startActivity(intent);
            }

        });


        viewPostsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(),
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
                Intent intent = new Intent(getActivity(),
                        MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Session session = Session.getActiveSession();
        Request.newMeRequest(session, new Request.GraphUserCallback() {

            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    SharedPreferences prefs = getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE);
                    if (!prefs.getString("UserId", "").equals(user.getId())) {
                        prefs.edit().putString("UserId", user.getId()).commit();
                        registerUser(prefs, user.getId());
                    }
                }
            }
        }).executeAsync();

    }

    public void cameraButtonClick(View view) {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivity(intent);
    }

    private void registerUser(final SharedPreferences prefs, final String userId) {
        RestClient db_client = new RestClient();
        db_client.postUser(userId, new PostCallback() {
            @Override
            public void onPostSuccess(String result) {
                Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
            }
        });
    }



    public void onBackPressed() {
        getActivity().finish();
    }

    public void logout() {
        Session session = Session.getActiveSession();
        session.closeAndClearTokenInformation();
        getActivity().finish();
    }

}
