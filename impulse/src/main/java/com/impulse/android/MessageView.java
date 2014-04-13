package com.impulse.android;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by eliot.mestre on 4/11/2014.
 */
public class MessageView  extends LinearLayout {

    private TextView messageTextView;
    private ImageView messageUserPicture;
    private TextView timeStampTextView;

    public MessageView(Context context, Message message) {
        super(context);

        String userKey = context.getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.message_view, this, true);
        messageUserPicture = (ImageView) view.findViewById(R.id.user_message_picture);
        messageTextView = (TextView) view.findViewById(R.id.message_textView);
        timeStampTextView = (TextView) view.findViewById(R.id.message_timeStamp);

        Picasso.with(context)
                .load("https://graph.facebook.com/" + message.userKey + "/picture?type=normal&redirect=true&width=500&height=500")
                .fit()
                .transform(new RoundedTransformation(45, 2))
                .into(messageUserPicture);



        RelativeLayout.LayoutParams textParams = (RelativeLayout.LayoutParams) messageTextView.getLayoutParams();
        RelativeLayout.LayoutParams timeStampParams = (RelativeLayout.LayoutParams) timeStampTextView.getLayoutParams();
        RelativeLayout.LayoutParams pictureParams = (RelativeLayout.LayoutParams) messageUserPicture.getLayoutParams();
        if(message.userKey.equals(userKey)) {
            textParams.addRule(RelativeLayout.LEFT_OF, R.id.user_message_picture);
            timeStampParams.addRule(RelativeLayout.LEFT_OF, R.id.user_message_picture);
            messageTextView.setGravity(Gravity.RIGHT);
            timeStampTextView.setGravity(Gravity.RIGHT);
            pictureParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        else {
            textParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_message_picture);
            timeStampParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_message_picture);
            pictureParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }

        messageTextView.setText(message.message);
        timeStampTextView.setText(message.timestamp);
    }
}
