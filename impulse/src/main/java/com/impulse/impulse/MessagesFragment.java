package com.impulse.impulse;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment implements AbsListView.OnItemClickListener {

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private List<Reply> replies;

    private String response;
    private boolean messagesAvailable = true;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static MessagesFragment create(String response) {
        MessagesFragment fragment = new MessagesFragment(response);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessagesFragment() {
    }

    public MessagesFragment(String response) {
        this.response = response;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replies = new ArrayList<Reply>();
        parsePosts(response);
        if(replies.size() == 0) {
            replies.add(new Reply("", "", "No messages"));
            messagesAvailable = false;
        }
        mAdapter = new ReplyPostAdapter(getActivity(), R.layout.post_reply_item, replies);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if(!messagesAvailable)
            return;
        mListView.setOnItemClickListener(null);
        String userKey = getActivity().getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");
        RestClient client = new RestClient();
        if(!userKey.equals(replies.get(position).userKey)) {
            client.getThread(userKey, replies.get(position).userKey, replies.get(position).postId, new GetCallback() {
                @Override
                void onDataReceived(String response) {
                    if (response.equals(RestClient.ERROR)) {
                        Dialog.noInternetDialog(getActivity());
                    }
                    else {
                        DrawerActivity activity = (DrawerActivity) getActivity();
                        activity.setFragment(MessageThreadFragment.create(response, replies.get(position).userKey, replies.get(position).postId), 2, true);
                    }
                }
            });
        }
        else {
            client.getActiveUserThreads(userKey, replies.get(position).postId, new GetCallback() {
                @Override
                void onDataReceived(String response) {
                    if (response.equals(RestClient.ERROR)) {
                        Dialog.noInternetDialog(getActivity());
                    }
                    else {
                        DrawerActivity activity = (DrawerActivity) getActivity();
                        activity.setFragment(ThreadsFragment.create(response, replies.get(position).postId), 2, true);
                    }
                }
            });
        }

    }

    private void parsePosts(String response) {
        Log.i("Response", response);
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonArray();
        for (JsonElement post : results) {
            JsonObject toAdd = post.getAsJsonObject();

            String postId = toAdd.get("postId").getAsString();
            String timeOut = toAdd.get("timeout").getAsString();
            String userKey = toAdd.get("userKey").getAsString();

            String calculatedTimeout = PostActivity.calculateTimeout(timeOut);
            if (calculatedTimeout.isEmpty())
                continue;

            replies.add(new Reply(postId, userKey, calculatedTimeout));
        }
    }
}
