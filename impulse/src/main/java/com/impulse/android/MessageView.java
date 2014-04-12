package com.impulse.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by eliot.mestre on 4/11/2014.
 */
public class MessageView  extends LinearLayout {

    private TextView messageTextView;

    public MessageView(Context context, Message message) {
        super(context);

        String userKey = context.getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.message_view, this, true);
        messageTextView = (TextView) view.findViewById(R.id.message_textView);
        messageTextView.setText(message.message);
    }
}
