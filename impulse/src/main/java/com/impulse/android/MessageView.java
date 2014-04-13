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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
       //     textParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textParams.addRule(RelativeLayout.LEFT_OF, R.id.user_message_picture);
            timeStampParams.addRule(RelativeLayout.LEFT_OF, R.id.user_message_picture);
            messageTextView.setGravity(Gravity.RIGHT);
            timeStampTextView.setGravity(Gravity.RIGHT);
            messageTextView.setBackground(context.getResources().getDrawable(R.drawable.my_message_bg));
            pictureParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        }
        else {
         //   textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            textParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_message_picture);
            timeStampParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_message_picture);
            pictureParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            messageTextView.setBackground(context.getResources().getDrawable(R.drawable.other_message_bg));
        }

        messageTextView.setText(message.message);
        timeStampTextView.setText(parseTimestamp(message.timestamp));
    }

    private String parseTimestamp(String timestamp) {
        return calculateTimeout(timestamp);
    }

    public static long getDateTimeDiff(DateTime date1, DateTime date2) {
        return date2.getMillis() - date1.getMillis();
    }

    public static String calculateTimeout(String timeOut) {
        String calculatedTimeout = "";
        DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
        DateTime dt = formatter.parseDateTime(timeOut);
        DateTime currentTime = new DateTime();

        long diffInHours = -1 * getDateTimeDiff(currentTime, dt) / (1000 * 60 * 60);
        long diffInMinutes = -1 * getDateTimeDiff(currentTime, dt) / (1000 * 60);

        if(diffInHours > 1)
            calculatedTimeout = diffInHours + " hours ";
        else if(diffInHours == 1)
            calculatedTimeout = diffInHours + " hour ";
        else if (diffInHours == 0)
            if(diffInMinutes > 1)
                calculatedTimeout = diffInMinutes + " minutes ";
            else if(diffInMinutes == 1)
                calculatedTimeout = diffInMinutes + " minute ";
            else if(diffInMinutes == 0)
                calculatedTimeout = "Less than a minute ";

        calculatedTimeout += "ago";
        return calculatedTimeout;
    }

}
