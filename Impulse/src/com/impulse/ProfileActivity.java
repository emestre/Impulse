package com.impulse;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends Activity {

    private TextView userName;
    private ProfilePictureView profPic;
    private HorizontalListView friendsList;
    private ArrayList<String> allFriends;
    private ArrayList<Friend> friends;
    private FriendViewAdapter friendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        allFriends = new ArrayList<String>();
        friends = new ArrayList<Friend>();
        friendsAdapter = new FriendViewAdapter(this, friends);
        Session session = Session.getActiveSession();
        userName = (TextView) findViewById(R.id.user_name);
        profPic = (ProfilePictureView) findViewById(R.id.profile_picture);
        friendsList = (HorizontalListView) findViewById(R.id.friends_list);
        friendsList.setAdapter(friendsAdapter);


        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("USER_ID")) {
            String user_id = getIntent().getExtras().getString("USER_ID");
            String user_name = getIntent().getExtras().getString("USER_NAME");
            userName.setText(user_name);
            profPic.setProfileId(user_id);
        } else {
            Request.newMeRequest(session, new Request.GraphUserCallback() {

                // callback after Graph API response with user object
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        userName.setText(user.getName().split(" ")[0]);
                        profPic.setProfileId(user.getId());
                    }
                }
            }).executeAsync();


            Request.newMyFriendsRequest(session, new Request.GraphUserListCallback() {
                @Override
                public void onCompleted(List<GraphUser> users, Response response) {
                    {
                        if (users != null) {
                            for (GraphUser user : users) {
                                allFriends.add(user.getId());
                                friends.add(new Friend(user.getName().split(" ")[0], user.getId()));
                            }
                            RestClient db_client = new RestClient();
                            db_client.getFriendList(allFriends, new PostCallback() {
                                @Override
                                public void onPostSuccess(String result) {
                                    Log.i("DB FRIENDS", result);
                                    parseProceduresFromResponse(result);
                                    friendsAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }).executeAsync();
        }

        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                intent.putExtra("USER_NAME", ((Friend) friendsList.getItemAtPosition(position)).getUser_name());
                intent.putExtra("USER_ID", ((Friend) friendsList.getItemAtPosition(position)).getUser_id());
                startActivity(intent);
            }
        });
    }

    private void parseProceduresFromResponse(String response) {
        JsonElement elem = new JsonParser().parse(response);
        ArrayList<Friend> newFriendList = new ArrayList<Friend>();
        JsonArray array = elem.getAsJsonArray();
        for (int index = 0; index < array.size(); ++index) {
            elem = array.get(index);
            JsonObject obj = elem.getAsJsonObject();
            JsonElement innerElem = obj.get("key");
            String key = innerElem.getAsString();
            for(Friend friend : friends) {
                if(friend.getUser_id().equals(key))
                    newFriendList.add(friend);
            }
        }
        friends = newFriendList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

}
