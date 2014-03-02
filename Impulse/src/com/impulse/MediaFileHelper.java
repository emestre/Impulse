package com.impulse;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaFileHelper {
    public static final String TAG = "MediaFileHelper";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static boolean isExternalStorageMounted() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static String getExternalCachePath(Context context, int type) {
        // check if the SD card is mounted for r/w
        if (!isExternalStorageMounted()) {
            return null;
        }

        String prefix, extension;
        if (type == MEDIA_TYPE_IMAGE) {
            prefix = "IMG_";
            extension = ".jpg";
        }
        else if (type == MEDIA_TYPE_VIDEO) {
            prefix = "VID_";
            extension = ".mp4";
        }
        else {
            Log.d(TAG, "unsupported media type");
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        return context.getExternalCacheDir() + File.separator + prefix + timeStamp + extension;
    }

    public static String getInternalCachePath(Context context, int type) {

        String prefix, extension;
        if (type == MEDIA_TYPE_IMAGE) {
            prefix = "IMG_";
            extension = ".jpg";
        }
        else if (type == MEDIA_TYPE_VIDEO) {
            prefix = "VID_";
            extension = ".mp4";
        }
        else {
            Log.d(TAG, "unsupported media type");
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        return context.getCacheDir() + File.separator + prefix + timeStamp + extension;
    }

    public static boolean moveFileToSDCard(String path) {
        // check if the SD card is mounted for r/w
        if (!isExternalStorageMounted()) {
            return false;
        }

        // get the Pictures directory on the SD card
        File externalDir = new File(Environment.getExternalStorageDirectory(), "Impulse");

        // Create the storage directory if it does not exist
        if (!externalDir.exists()) {
            if (!externalDir.mkdirs()) {
                Log.d(TAG, "failed to create Impulse directory on SD card");
                return false;
            }
        }

        File oldPath = new File(path);
        File newPath = new File(externalDir.getAbsolutePath() + File.separator + oldPath.getName());

        return oldPath.renameTo(newPath);
    }

    public static void runMediaScanner(Context context) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                + Environment.getExternalStorageDirectory() + File.separator + "Impulse")));

        Log.d(TAG, "media scanner ran on Impulse");
    }
}
