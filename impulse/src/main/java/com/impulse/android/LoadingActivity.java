package com.impulse.android;

import com.impulse.android.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);

        setContentView(R.layout.activity_loading);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            ActionBar actionBar = getActionBar();
            actionBar.hide();
        }

        // display a loading spinner while the image is uploading
        final ProgressDialog mUploadingProgress = new ProgressDialog(this);
        mUploadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mUploadingProgress.setTitle("Loading...");
        mUploadingProgress.setIndeterminate(true);
        mUploadingProgress.setCancelable(false);
        mUploadingProgress.show();

        PackageInfo pInfo = null;
        String version = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        RestClient client = new RestClient();
        client.initSession(version, new GetCallback() {
            @Override
            void onDataReceived(String response) {
                if(response.equals(HttpStatus.SC_OK + "")) {
                    mUploadingProgress.dismiss();
                    Intent intent = new Intent(LoadingActivity.this, DrawerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                    showIncorrectVersionDialog();
            }
        });
    }


    private void showIncorrectVersionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Outdated Version");
        builder.setMessage("The version of Impulse you are using is no longer supported. Please update the application to continue.");
        builder.setCancelable(false);
        builder.setPositiveButton("Close Impulse", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.create().show();
    }
}
