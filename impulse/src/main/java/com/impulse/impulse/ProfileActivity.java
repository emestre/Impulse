package com.impulse.impulse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileActivity extends Fragment {

    private static final String TAG = "ProfileActivity";

    private String mUserId;
    private String myUserKey;

    private TextView mUserName;
    private EditText mAboutField;
    private HorizontalListView mFriendScrollList;
    private ArrayList<Friend> mFriendList;
    private FriendViewAdapter mFriendAdapter;
    private Button mViewPostsButton;
    private TextView mFriendsText;
    private ImageView mProfilePic;

    private boolean isEditing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_profile, container, false);

        // get the user ID from the bundled arguments to populate profile information
        mUserId = this.getArguments().getString("id");
        myUserKey = this.getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");
        Log.d(TAG, "user ID: " + mUserId);
        // tell the host activity that this fragment has an options menu
        setHasOptionsMenu(true);

        // get the user's facebook session
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
                Log.d(TAG, "birthday: " + user.getBirthday());
                try {
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
                    DateTime dob = formatter.parseDateTime(user.getBirthday());
                    DateTime now = new DateTime();
                    // set this user's age
                    mUserName.setText(mUserName.getText() + ", " + Years.yearsBetween(dob, now).getYears());
                } catch (NullPointerException eNull) {
                    Log.d(TAG, "user's birthday returned NULL");
                } catch (IllegalArgumentException eBad) {
                    Log.d(TAG, "user doesn't share age on facebook");
                }

                // get this user's list of friends on impulse
                getFriends(session);
            }
        }).executeAsync();

        new RestClient().aboutUser(mUserId, new GetCallback() {
            @Override
            void onDataReceived(String response) {
                if (response.equals(RestClient.ERROR)) {
                    Dialog.noInternetDialog(getActivity());
                }
                else {
                    mAboutField.setText(response);
                }
            }
        });

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
                RestClient client = new RestClient();
                client.getPostList(new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        if (response.trim().equals("[]"))
                            showNoPostsDialog();
                        else {
                            getActivity().getIntent().putExtra("USER_ID", response);
                            ((DrawerActivity) getActivity()).selectItem(0);
                        }
                    }
                }, mUserId);
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mUserId.equals(myUserKey)) {
            final SharedPreferences preferences = this.getActivity().getPreferences(Activity.MODE_PRIVATE);

            if (!preferences.contains("first run profile")) {
                new ShowcaseView.Builder(this.getActivity(), true)
                        .setContentTitle("Pulse Profile")
                        .setContentText("Let people know what you’re all about.\n" +
                                "Your profile is the easiest way for other\n" +
                                "users to get a sense of who you are.")
                        .setStyle(R.style.ImpulseShowcaseView)
                        .hideOnTouchOutside()
                        .setShowcaseEventListener(new OnShowcaseEventListener() {
                            @Override
                            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putBoolean("first run profile", false);
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // if we're viewing the logged in user's profile, then allow editing of about field
        if (mUserId.equals(myUserKey)) {
            inflater.inflate(R.menu.profile, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_edit:
                if (isEditing) {
                    isEditing = false;

                    // upload the user's text to the server
                    String text = mAboutField.getText().toString();
                    if (text.equals(""))
                        text = mUserId;

                    Log.d(TAG, "uploading about field: " + text);
                    new RestClient().editAboutUser(mUserId, text, new PostCallback() {
                        @Override
                        public void onPostSuccess(String result) {
                            if (result.equals(RestClient.ERROR)) {
                                Dialog.noInternetDialog(getActivity());
                            }
                            else if (Integer.parseInt(result) == HttpStatus.SC_OK) {
                                Log.d(TAG, "about user text upload SUCCEEDED");
                            }
                        }
                    });

                    mAboutField.setEnabled(false);
                    mAboutField.setBackgroundColor(0x00000000);
                    item.setTitle("Edit");
                } else {
                    isEditing = true;
                    mAboutField.setEnabled(true);
                    mAboutField.requestFocus();
                    mAboutField.setBackgroundColor(0xFFFFFFFF);
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mAboutField, InputMethodManager.SHOW_IMPLICIT);
                    item.setTitle("Done");
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getFriends(Session session) {
        Bundle requestBundle = new Bundle();
        requestBundle.putString("fields", "id, name, installed");

        new Request(session, "/" + mUserId + "/friends", requestBundle, HttpMethod.GET, new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                try {
                    parseFriends(response);
                    mFriendsText.setText("Friends on Impulse (" + mFriendList.size() + ")");
                    mFriendAdapter.notifyDataSetChanged();
                } catch (NullPointerException e) {
                    Log.d(TAG, "friends list response returned NULL");
                    mFriendsText.setText("Doesn't share friends list...");
                    mFriendAdapter.notifyDataSetChanged();
                }
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

    private void showNoPostsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("No Live Posts");
        builder.setMessage("This person does not currently have any live posts.");
        builder.setCancelable(true);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

}
