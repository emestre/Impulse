package com.impulse.android;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.widget.RelativeLayout;

/**
 * Created by Eliot on 3/11/14.
 */
public class ImpulseViewPager extends ViewPager {

    public ImpulseViewPager(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth/2, parentHeight);
        this.setLayoutParams(new RelativeLayout.LayoutParams(parentWidth / 2, parentHeight));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
