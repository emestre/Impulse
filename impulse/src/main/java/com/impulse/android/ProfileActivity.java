package com.impulse.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileActivity extends Fragment {

    private static final String TAG = "ProfileActivity";

    private TextView userName;
//    private ProfilePictureView profPic;
    private HorizontalListView friendsList;
    private ArrayList<Friend> friends;
    private FriendViewAdapter friendsAdapter;
    private Button viewPosts;
    private String userId;
    private TextView mFriendsText;
    private ImageView profPic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_profile, container, false);

        friends = new ArrayList<Friend>();
        friendsAdapter = new FriendViewAdapter(getActivity(), friends);
        final Session session = Session.getActiveSession();
        userName = (TextView) root.findViewById(R.id.user_name);
        friendsList = (HorizontalListView) root.findViewById(R.id.friends_list);
        viewPosts = (Button) root.findViewById(R.id.button_user_posts);
        friendsList.setAdapter(friendsAdapter);
        profPic = (ImageView) root.findViewById(R.id.profile_picture);
        mFriendsText = (TextView) root.findViewById(R.id.friends_text_field);

        Request.newMeRequest(session, new Request.GraphUserCallback() {

            // callback after Graph API response with user object
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    // get the facebook user ID
                    userId = user.getId();
                    // set this user's name
                    userName.setText(user.getName().split(" ")[0]);
                    // get this user's list of friends on impulse
                    getFriends(session);
                    // load profile picture
                    Picasso.with(getActivity())
                            .load("https://graph.facebook.com/" + userId + "/picture?type=large&redirect=true&width=400&height=400")
                            .into(profPic);
                }
            }
        }).executeAsync();

        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get the new person's ID
                userId = ((Friend) friendsList.getItemAtPosition(position)).getUser_id();
                // set the new user's name
                userName.setText(((Friend) friendsList.getItemAtPosition(position)).getUser_name());
                // load new person's profile picture
                Picasso.with(getActivity())
                        .load("https://graph.facebook.com/" + userId + "/picture?type=large&redirect=true&width=400&height=400")
                        .into(profPic);
                // update to this user's friends list
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
                mFriendsText.setText("Friends on Impulse (" + friends.size() + ")");
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
