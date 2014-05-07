package com.impulse.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.squareup.picasso.Picasso;

public class FriendView extends LinearLayout {

    private TextView userName;
    private ImageView profilePicture;

    public FriendView(Context context, Friend friend) {
        super(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.friend_view, this, true);
        userName = (TextView) findViewById(R.id.friend_name);
        profilePicture = (ImageView) findViewById(R.id.friend_profile_picture);
        userName.setText(friend.getUser_name());

      //  profilePicture.setProfileId(friend.getUser_id());

        Picasso.with(context)
                .load("https://graph.facebook.com/" + friend.getUser_id() + "/picture?type=large&redirect=true&width=400&height=400")
                .transform(new RoundedTransformation(80, 2))
                .into(profilePicture);

        requestLayout();
    }
}
