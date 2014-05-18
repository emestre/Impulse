package com.impulse.impulse;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.lang.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public static ArrayList<Notifs> notifs = new ArrayList<Notifs>();
    private NotificationManager mNotificationManager;
    public static String viewingUserKey = null;
    public static String viewingPostId = null;

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

        String message = received.getString("message");
        String senderKey = received.getString("senderKey");
        String postId = received.getString("postId");
        String senderName;
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
            if(user == null)
                return;

            senderName = user.getName().split(" ")[0];


            if (postId.equals(viewingPostId) && senderKey.equals(viewingUserKey)) {
                Intent refreshScreen = new Intent();
                refreshScreen.setAction("com.impulse.MessageThreadFragment");
                refreshScreen.putExtra("otherUserKey", senderKey);
                refreshScreen.putExtra("postId", postId);

                sendBroadcast(refreshScreen);
                return;
            }

            notifs.add(new Notifs(senderKey, senderName, postId, message));

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(GcmIntentService.this)
                            .setSmallIcon(R.drawable.ic_impulse_icon)
                            .setContentTitle(senderName)
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setNumber(notifs.size());

            if (notifs.size() == 1) {
                mBuilder.setContentTitle(senderName)
                        .setContentText(message);
            }
            else {
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                inboxStyle.setBigContentTitle(notifs.size() + " new messages");

                for(Notifs cur: notifs)
                    inboxStyle.addLine(cur.getSenderName() + ": " + cur.getMessage());

                mBuilder.setStyle(inboxStyle);
            }
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}