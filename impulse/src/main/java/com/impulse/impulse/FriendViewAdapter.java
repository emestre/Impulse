package com.impulse.impulse;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class FriendViewAdapter extends BaseAdapter {
    private Context context;
    private List<Friend> friends_list;

    public FriendViewAdapter(Context context, List<Friend> friends) {
        super();
        this.context = context;
        friends_list = friends;
    }

    @Override
    public int getCount() {
        return friends_list.size();
    }

    @Override
    public Object getItem(int position) {
        return friends_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FriendView view = new FriendView(context, friends_list.get(position));
        return view;
    }
}
