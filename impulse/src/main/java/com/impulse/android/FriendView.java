package com.impulse.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

public class FriendView extends LinearLayout {

    private TextView userName;
    private ProfilePictureView profilePicture;

    public FriendView(Context context, Friend friend) {
        super(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.friend_view, this, true);
        userName = (TextView) findViewById(R.id.friend_name);
        profilePicture = (ProfilePictureView) findViewById(R.id.friend_profile_picture);
        userName.setText(friend.getUser_name());
        profilePicture.setProfileId(friend.getUser_id());
        requestLayout();
    }
}
