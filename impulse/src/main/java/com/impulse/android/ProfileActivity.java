package com.impulse.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileActivity extends Fragment {

    private static final String TAG = "ProfileActivity";

    private String mUserId;

    private TextView mUserName;
    private EditText mAboutField;
    private HorizontalListView mFriendScrollList;
    private ArrayList<Friend> mFriendList;
    private FriendViewAdapter mFriendAdapter;
    private Button mViewPostsButton;
    private TextView mFriendsText;
    private ImageView mProfilePic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_profile, container, false);

        final Session session = Session.getActiveSession();

        mUserName = (TextView) root.findViewById(R.id.user_name);
        mAboutField = (EditText) root.findViewById(R.id.bio_text);
        mProfilePic = (ImageView) root.findViewById(R.id.profile_picture);
        mFriendsText = (TextView) root.findViewById(R.id.friends_text_field);
        mViewPostsButton = (Button) root.findViewById(R.id.button_user_posts);

        mFriendList = new ArrayList<Friend>();
        mFriendAdapter = new FriendViewAdapter(getActivity(), mFriendList);
        mFriendScrollList = (HorizontalListView) root.findViewById(R.id.friends_list);
        mFriendScrollList.setAdapter(mFriendAdapter);

        // get the user ID from the bundled arguments to populate profile information
        mUserId = this.getArguments().getString("id");
        Log.d(TAG, "user ID: " + mUserId);

        // if we're viewing the logged in user's profile, then allow editing of about field
        if (mUserId.equals(getActivity().getSharedPreferences("com.impulse",
                Context.MODE_PRIVATE).getString("UserId", ""))) {

            mAboutField.setEnabled(true);
        }

        Request.newGraphPathRequest(session, mUserId, new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                GraphUser user = response.getGraphObjectAs(GraphUser.class);

                // load profile picture
                    Picasso.with(getActivity())
                            .load("https://graph.facebook.com/" + mUserId + "/picture?type=large&redirect=true&width=400&height=400")
                            .into(mProfilePic);

                // set user name
                mUserName.setText(user.getName().split(" ")[0]);

                // get the user's birthday to calculate age
                if (user.getBirthday() != null) {
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
                    DateTime dob = formatter.parseDateTime(user.getBirthday());
                    DateTime now = new DateTime();
                    // set this user's age
                    mUserName.setText(mUserName.getText() + ", " + Years.yearsBetween(dob, now).getYears());
                }
                else {
                    Log.d(TAG, "user's birthday returned NULL");
                }

                // get this user's list of friends on impulse
                getFriends(session);
            }
        }).executeAsync();

        mFriendScrollList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get the new user's ID
                String newUserId = ((Friend) mFriendScrollList.getItemAtPosition(position)).getUser_id();

                Bundle bundle = new Bundle();
                bundle.putString("id", newUserId);

                Fragment fragment = new ProfileActivity();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.content_frame, fragment).commit();
            }
        });

        mViewPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DrawerActivity.class);
                intent.putExtra("USER_ID", mUserId);
                startActivity(intent);
            }
        });

        LinearLayout screen = (LinearLayout) root.findViewById(R.id.profile_container);
        // set the root layout to receive touch events to hide keyboard
        screen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // hide the keyboard if touch is received outside of keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                return true;
            }
        });

        return root;
    }

    private void getFriends(Session session) {
        Bundle requestBundle = new Bundle();
        requestBundle.putString("fields", "id, name, installed");

        new Request(session, "/" + mUserId + "/friends", requestBundle, HttpMethod.GET, new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                parseFriends(response);
                mFriendsText.setText("Friends on Impulse (" + mFriendList.size() + ")");
                mFriendAdapter.notifyDataSetChanged();
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
                mFriendList.add(new Friend(friendName, friendId));
            }
        }
    }

}
