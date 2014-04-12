package com.impulse.android;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessageThreadFragment extends Fragment {

    private AbsListView mListView;
    private ListAdapter mAdapter;
    private List<Message> messages;
    private String response;

    private EditText replyEditText;
    private Button replyButton;

    private String otherUserKey;
    private String postId;
    private String userKey;
    private Timer timer;


    // TODO: Rename and change types and number of parameters
    public static MessageThreadFragment create(String response, String otherUserKey, String postId) {
        MessageThreadFragment fragment = new MessageThreadFragment(response, otherUserKey, postId);
        return fragment;
    }

    public MessageThreadFragment(String response, String otherUserKey, String postId) {
        this.response = response;
        this.otherUserKey = otherUserKey;
        this.postId = postId;
    }

    public MessageThreadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messages = new ArrayList<Message>();
        parsePosts(response);
        mAdapter = new MessageThreadAdapter(getActivity(), R.layout.post_reply_item, messages);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_thread, container, false);
        // Inflate the layout for this fragment
        // Set the adapter
        userKey = getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

        mListView = (AbsListView) view.findViewById(R.id.message_list);
        replyEditText = (EditText) view.findViewById(R.id.reply_from_thread_editText);
        replyButton = (Button) view.findViewById(R.id.reply_from_thread_button);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks

        replyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final RestClient client = new RestClient();
                String message = replyEditText.getText().toString();
                client.createMessage(userKey, otherUserKey, postId, message, new PostCallback() {
                    @Override
                    public void onPostSuccess(String result) {
                        client.getThread(userKey, otherUserKey, postId, new GetCallback() {
                            @Override
                            void onDataReceived(String response) {
                                DrawerActivity activity = (DrawerActivity) getActivity();
                                activity.setFragment(MessageThreadFragment.create(response,otherUserKey, postId), 2);
                            }
                        });
                    }
                });
            }
        });

       // timer = new Timer();
       // timer.schedule(new RefreshTask(userKey, otherUserKey, postId), 10000, 10000);
        return view;
    }

    private void parsePosts(String response) {
        Log.i("Response", response);
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonArray();
        for (JsonElement post : results) {
            JsonObject toAdd = post.getAsJsonObject();

            String userKey = toAdd.get("author").getAsString();
            String message = toAdd.get("message").getAsString();
            messages.add(new Message(userKey, message));
        }
    }

    private class RefreshTask extends TimerTask {

        private String userKey;
        private String otherUserKey;
        private String postId;

        public RefreshTask(String userKey, String otherUserKey, String postId) {
            this.userKey = userKey;
            this.otherUserKey = otherUserKey;
            this.postId = postId;
        }

        @Override
        public void run() {
            final RestClient client = new RestClient();
            client.getThread(userKey, otherUserKey, postId, new GetCallback() {
                @Override
                void onDataReceived(String response) {
                    DrawerActivity activity = (DrawerActivity) getActivity();
                    activity.setFragment(MessageThreadFragment.create(response,otherUserKey, postId), 2);
                }
            });
        }
    }
}
