package com.impulse.impulse;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by eliot.mestre on 4/11/2014.
 */
public class ReplyPostAdapter  extends ArrayAdapter<Reply> {

        private Context mContext;

        public ReplyPostAdapter(Context context, int resource, List<Reply> items) {
            super(context, resource, items);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Reply reply = getItem(position);
            return new ReplyView(mContext, reply);
        }
}
