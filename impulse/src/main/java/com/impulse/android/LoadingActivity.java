package com.impulse.android;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.impulse.android.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoadingActivity extends Activity {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String SENDER_ID = "354120366381";
    private ProgressDialog mUploadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);

        setContentView(R.layout.activity_loading);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            ActionBar actionBar = getActionBar();
            actionBar.hide();
        }

        // display a loading spinner while the image is uploading
        mUploadingProgress = new ProgressDialog(this);
        mUploadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mUploadingProgress.setTitle("Loading...");
        mUploadingProgress.setIndeterminate(true);
        mUploadingProgress.setCancelable(false);
        mUploadingProgress.show();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String regid = getRegistrationId(getApplicationContext());

        if (regid.isEmpty()) {
            registerInBackground(getApplicationContext());
        } else {
            sendRegistrationIdToBackend();
        }
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

    private void registerInBackground(final Context context) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                String msg = "";
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    String regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.


                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.i("LOADING", result);
                sendRegistrationIdToBackend();
            }
        }.execute(null, null, null);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("LOADING", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("LOADING", "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences("com.impulse", Context.MODE_PRIVATE);
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("LOADING", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToBackend() {

        final String regid = getRegistrationId(getApplicationContext());
        Session session = Session.getActiveSession();
        Request.newMeRequest(session, new Request.GraphUserCallback() {

            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    SharedPreferences prefs = getSharedPreferences("com.impulse", Context.MODE_PRIVATE);
                    //if (!prefs.getString("UserId", "").equals(user.getId())) {
                        prefs.edit().putString("UserId", user.getId()).commit();
                        registerUser(prefs, user.getId(), regid);
                        Log.i("USER", user.getId());

                  //  }
                }
            }
        }).executeAsync();

        SharedPreferences prefs = getSharedPreferences("com.impulse", Context.MODE_PRIVATE);
        Log.i("USER", prefs.getString("UserId", ""));

        Log.i("REGID", regid);
        String version = null;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        RestClient client = new RestClient();
        client.initSession(version, new GetCallback() {
            @Override
            void onDataReceived(String response) {
                if (response.equals(HttpStatus.SC_OK + "")) {
                    // mUploadingProgress.dismiss();
                    Intent intent = new Intent(LoadingActivity.this, DrawerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else
                    showIncorrectVersionDialog();
            }
        });
    }

    private void registerUser(final SharedPreferences prefs, final String userId, final String regId) {
        RestClient db_client = new RestClient();
        db_client.postUser(userId, regId, new PostCallback() {
            @Override
            public void onPostSuccess(String result) {
            }
        });
    }
}
