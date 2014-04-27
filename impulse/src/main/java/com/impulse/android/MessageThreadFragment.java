package com.impulse.android;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessageThreadFragment extends Fragment {

    private AbsListView mListView;
    private ArrayAdapter mAdapter;
    private List<Message> messages;
    private String response;

    private EditText replyEditText;
    private Button replyButton;

    private TextView otherUserName;

    private String otherUserKey;
    private String postId;
    private String userKey;
    private Timer timer;

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
        otherUserName = (TextView) view.findViewById(R.id.thread_other_user_name);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        getUserName(otherUserKey);
        // Set OnItemClickListener so we can be notified on item clicks

        replyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final RestClient client = new RestClient();
                String message = replyEditText.getText().toString();
                replyEditText.setText("");
                client.createMessage(userKey, otherUserKey, postId, message,"text", new PostCallback() {
                    @Override
                    public void onPostSuccess(String result) {
                        client.getThread(userKey, otherUserKey, postId, new GetCallback() {
                            @Override
                            void onDataReceived(String response) {
                                try {
                                    messages.clear();
                                    parsePosts(response);
                                    mAdapter.notifyDataSetChanged();
                                    scrollToEnd();
                                } catch (Exception e) {
                                    Toast.makeText(getActivity(), "An error has ocurred.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });
        scrollToEnd();

         timer = new Timer();
         timer.schedule(new RefreshTask(userKey, otherUserKey, postId), 10000, 10000);

        return view;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    private void scrollToEnd() {
        mListView.post(new Runnable() {
            public void run() {
                mListView.setSelection(mListView.getCount() - 1);
            }
        });
    }

    private void parsePosts(String response) {
        Log.i("Response", response);
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonArray();
        for (JsonElement post : results) {
            JsonObject toAdd = post.getAsJsonObject();

            String userKey = toAdd.get("author").getAsString();
            String message = toAdd.get("message").getAsString();
            String timestamp = toAdd.get("timestamp").getAsString();
            String type = toAdd.get("type").getAsString();
            messages.add(new Message(userKey, message, timestamp, type));
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
                    try {
                        messages.clear();
                        parsePosts(response);
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "An error has ocurred.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void getUserName(String userId) {
        Bundle requestBundle = new Bundle();
        requestBundle.putString("fields", "name");
        Session session = Session.getActiveSession();
        new Request(session, "/" + userId, requestBundle, HttpMethod.GET, new Request.Callback() {
            public void onCompleted(Response response) {
                GraphObject obj = response.getGraphObject();
                if (obj == null) {
                    otherUserName.setText("Impulse");
                    return;
                }
                JSONObject json = response.getGraphObject().getInnerJSONObject();
                JsonElement elem = new JsonParser().parse(json.toString());
                String name = elem.getAsJsonObject().get("name").getAsString().split(" ")[0];
                otherUserName.setText(name);
            }
        }
        ).executeAsync();
    }
}
