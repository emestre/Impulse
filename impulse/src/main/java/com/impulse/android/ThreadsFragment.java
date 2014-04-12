package com.impulse.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class ThreadsFragment extends Fragment implements AbsListView.OnItemClickListener {


    private AbsListView mListView;
    private ListAdapter mAdapter;

    private List<Thread> threads;
    private String response;

    // TODO: Rename and change types of parameters
    public static ThreadsFragment create(String response) {
        ThreadsFragment fragment = new ThreadsFragment(response);
        return fragment;
    }

    public ThreadsFragment(String response) {
        this.response = response;
    }

    public ThreadsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        threads = new ArrayList<Thread>();
        parsePosts(response);
        // TODO: Change Adapter to display your content
        mAdapter = new ThreadAdapter(getActivity(),
                android.R.layout.simple_list_item_1, threads);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_threads_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DrawerActivity activity = (DrawerActivity) getActivity();
        activity.setFragment(new MessageThreadFragment(), 2);
    }

    private void parsePosts(String response) {
        Log.i("Response", response);
        JsonParser parser = new JsonParser();
        JsonArray results = parser.parse(response).getAsJsonArray();
        for (JsonElement post : results) {
            JsonObject toAdd = post.getAsJsonObject();

            String userKey = toAdd.get("userKey").getAsString();

            threads.add(new Thread(userKey));
        }
    }
}
