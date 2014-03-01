package com.impulse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileActivity extends Activity {

    private TextView userName;
    private ProfilePictureView profPic;
    private HorizontalListView friendsList;
    private ArrayList<Friend> friends;
    private FriendViewAdapter friendsAdapter;
    private String userId;
    private String userFirstName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        friends = new ArrayList<Friend>();
        friendsAdapter = new FriendViewAdapter(this, friends);
        final Session session = Session.getActiveSession();
        userName = (TextView) findViewById(R.id.user_name);
        profPic = (ProfilePictureView) findViewById(R.id.profile_picture);
        friendsList = (HorizontalListView) findViewById(R.id.friends_list);
        friendsList.setAdapter(friendsAdapter);


        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("USER_ID")) {
            userId = getIntent().getExtras().getString("USER_ID");
            userFirstName = getIntent().getExtras().getString("USER_NAME");
            userName.setText(userFirstName);
            profPic.setProfileId(userId);
            getFriends(session);
        } else {
            Request.newMeRequest(session, new Request.GraphUserCallback() {

                // callback after Graph API response with user object
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        userName.setText(user.getName().split(" ")[0]);
                        profPic.setProfileId(user.getId());
                        userId = user.getId();
                        getFriends(session);
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

    private void getFriends(Session session) {
        Bundle requestBundle = new Bundle();
        requestBundle.putString("fields", "id,name,installed");

        new Request(session, "/" + userId + "/friends", requestBundle, HttpMethod.GET, new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                parseFriends(response);
                friendsAdapter.notifyDataSetChanged();
            }
        }).executeAsync();
    }

    private void parseFriends(Response response) {
        GraphObject results = response.getGraphObject();
        JSONObject json = results.getInnerJSONObject();

        JsonElement elem = new JsonParser().parse( json.toString());
        JsonElement data = elem.getAsJsonObject().get("data");
        JsonArray newFriends = data.getAsJsonArray();
        for(JsonElement friend : newFriends) {
            JsonObject newFriend = friend.getAsJsonObject();
            if(newFriend.has("installed") && newFriend.get("installed").getAsString().equals("true")) {
                String friendName = newFriend.get("name").getAsString().split(" ")[0];
                String friendId = newFriend.get("id").getAsString();
                friends.add(new Friend(friendName, friendId));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

}
