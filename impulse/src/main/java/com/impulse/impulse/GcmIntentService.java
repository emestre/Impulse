package com.impulse.impulse;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.lang.*;
import java.util.concurrent.ExecutionException;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public static final String GROUP_KEY = "group_notifications";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private String userName;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle

            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification(extras);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification(extras);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i("BLARGH", "Completed work @ " + SystemClock.elapsedRealtime());
                sendNotification(extras);
                Log.i("BLARGH", "Received: " + extras.toString());
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Bundle received) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        final String message = received.getString("message");
        final String senderKey = received.getString("senderKey");
        String postId = received.getString("postId");
        Session session = Session.getActiveSession();

        Intent intent = new Intent(this, DrawerActivity.class);
        intent.putExtra("thread_user", senderKey);
        intent.putExtra("thread_post", postId);

        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            Response resp = Request.newGraphPathRequest(session, senderKey, new Request.Callback() {
                @Override
                public void onCompleted(Response response) {
                }
            }).executeAsync().get().get(0);
            GraphUser user = resp.getGraphObjectAs(GraphUser.class);
            if(user != null) {
                userName = user.getName().split(" ")[0];

                Intent refreshScreen = new Intent();
                refreshScreen.setAction("com.impulse.MessageThreadFragment");
                refreshScreen.putExtra("otherUserKey", senderKey);
                refreshScreen.putExtra("postId", postId);

                sendBroadcast(refreshScreen);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(GcmIntentService.this)
                                .setSmallIcon(R.drawable.ic_impulse_icon)
                                .setContentTitle(userName)
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(message))
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL);

                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}