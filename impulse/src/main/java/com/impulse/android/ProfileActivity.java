package com.impulse.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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

public class ProfileActivity extends Fragment {

    private TextView userName;
    private ProfilePictureView profPic;
    private HorizontalListView friendsList;
    private ArrayList<Friend> friends;
    private FriendViewAdapter friendsAdapter;
    private Button viewPosts;
    private String userId;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_profile, container, false);


        friends = new ArrayList<Friend>();
        friendsAdapter = new FriendViewAdapter(getActivity(), friends);
        final Session session = Session.getActiveSession();
        userName = (TextView) root.findViewById(R.id.user_name);
        profPic = (ProfilePictureView) root.findViewById(R.id.profile_picture);
        friendsList = (HorizontalListView) root.findViewById(R.id.friends_list);
        viewPosts = (Button) root.findViewById(R.id.user_posts);
        friendsList.setAdapter(friendsAdapter);


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


        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userName.setText(((Friend) friendsList.getItemAtPosition(position)).getUser_name());
                userId = ((Friend) friendsList.getItemAtPosition(position)).getUser_id();
                profPic.setProfileId(userId);
                friends.clear();
                friendsAdapter.notifyDataSetChanged();
                getFriends(session);
            }
        });

        viewPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DrawerActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });



        return root;
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

        JsonElement elem = new JsonParser().parse(json.toString());
        JsonElement data = elem.getAsJsonObject().get("data");
        JsonArray newFriends = data.getAsJsonArray();
        for (JsonElement friend : newFriends) {
            JsonObject newFriend = friend.getAsJsonObject();
            if (newFriend.has("installed") && newFriend.get("installed").getAsString().equals("true")) {
                String friendName = newFriend.get("name").getAsString().split(" ")[0];
                String friendId = newFriend.get("id").getAsString();
                friends.add(new Friend(friendName, friendId));
            }
        }
    }

}
