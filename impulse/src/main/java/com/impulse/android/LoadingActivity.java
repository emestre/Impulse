package com.impulse.android;

import com.impulse.android.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);

        // display a loading spinner while the image is uploading
        final ProgressDialog mUploadingProgress = new ProgressDialog(this);
        mUploadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mUploadingProgress.setTitle("Uploading Post...");
        mUploadingProgress.setMessage("Your post is being created.");
        mUploadingProgress.setIndeterminate(true);
        mUploadingProgress.setCancelable(false);
        mUploadingProgress.show();
    }
}
