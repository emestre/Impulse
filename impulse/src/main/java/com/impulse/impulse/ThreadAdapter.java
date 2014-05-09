package com.impulse.impulse;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by eliot.mestre on 4/11/2014.
 */
public class ThreadAdapter extends ArrayAdapter<Thread> {

    private Context mContext;

    public ThreadAdapter(Context context, int resource, List<Thread> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Thread thread = getItem(position);
        return new ReplyView(mContext, thread);
    }
}
