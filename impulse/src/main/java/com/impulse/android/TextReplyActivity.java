package com.impulse.android;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.HttpStatus;


public class TextReplyActivity extends ActionBarActivity {

    private EditText mReplyText;
    private String mFilename;
    private String mPostUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        this.getActionBar().hide();

        setContentView(R.layout.activity_text_reply);

        mPostUserId = this.getIntent().getExtras().getString("userid");
        mFilename = this.getIntent().getExtras().getString("filename");
        Picasso.with(this)
                .load(RestClient.getFile(mFilename, "full", false))
                .into((ImageView) findViewById(R.id.reply_post_thumb));

        mReplyText = (EditText) findViewById(R.id.reply_text_field);

        ImageButton replyButton = (ImageButton) findViewById(R.id.reply_button);
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String message = mReplyText.getText().toString();
        if (message.equals("")) {
            Toast.makeText(this, "Enter message text", Toast.LENGTH_SHORT).show();
            return;
        }

        String myUserKey = getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

        new RestClient().createMessage(myUserKey, mPostUserId, mFilename, message, new PostCallback() {
            @Override
            public void onPostSuccess(String result) {
                if (Integer.parseInt(result) == HttpStatus.SC_OK) {
                    Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Message Did Not Send", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
