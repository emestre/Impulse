package com.impulse;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class PostFragment extends Fragment {

    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static PostFragment create(int pageNumber) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PostFragment() {
    }

    public int getPageNumber() {
        return mPageNumber;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.post_fragment, container, false);
        ImageView image = (ImageView) view.findViewById(R.id.post_image);
        String url = null;
        if(getPageNumber() == 0)
            url = "http://ppcdn.500px.org/56690912/e1fa0e8ee33b7f123cf9ca851fe7d5cae4f09e23/5.jpg";
        else if(getPageNumber() == 1)
            url = "http://farm6.staticflickr.com/5542/9463864484_7f49df4725_b.jpg";
        else if(getPageNumber() == 2)
            url = "http://i.imgur.com/Jnjq2au.jpg";
        else if(getPageNumber() == 3)
            url = "http://i.imgur.com/6oIQo8M.jpg";
        else if(getPageNumber() == 4)
            url = "http://i.imgur.com/lqaii.jpg";

        Picasso.with(getActivity().getApplicationContext()).load(url).into(image);
        return view;
    }

}
