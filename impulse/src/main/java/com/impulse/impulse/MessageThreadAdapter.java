package com.impulse.impulse;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by eliot.mestre on 4/11/2014.
 */
public class MessageThreadAdapter extends ArrayAdapter<Message> {

    private Context mContext;

    public MessageThreadAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);
        return new MessageView(mContext, message);
    }
}
