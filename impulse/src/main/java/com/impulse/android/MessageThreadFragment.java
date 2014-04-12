package com.impulse.android;

import android.app.Activity;
import android.net.Uri;
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

public class MessageThreadFragment extends Fragment {

    private AbsListView mListView;
    private ListAdapter mAdapter;
    private List<Message> messages;
    private String response;

    // TODO: Rename and change types and number of parameters
    public static MessageThreadFragment create(String response) {
        MessageThreadFragment fragment = new MessageThreadFragment(response);
        return fragment;
    }

    public MessageThreadFragment(String response) {
        this.response = response;
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
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks

        return view;
    }

    private void parsePosts(String response) {
        Log.i("Response", response);
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonArray();
        for (JsonElement post : results) {
            JsonObject toAdd = post.getAsJsonObject();

            String userKey = toAdd.get("userKey").getAsString();
            String message = toAdd.get("message").getAsString();
            messages.add(new Message(userKey, message));
        }
    }

}
