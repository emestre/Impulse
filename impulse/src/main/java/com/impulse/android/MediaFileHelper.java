package com.impulse.android;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
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
        // image_check if the SD card is mounted for r/w
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

    public static String getInternalCachePath(Context context) {

        return context.getCacheDir() + File.separator +  "IMG_temp.jpg";
    }

    public static String getSdCardPath() {
        // image_check if the SD card is mounted for r/w
        if (!isExternalStorageMounted()) {
            return null;
        }

        // get the Pictures directory on the SD card
        File externalDir = new File(Environment.getExternalStorageDirectory(), "Impulse");

        // Create the storage directory if it does not exist
        if (!externalDir.exists()) {
            if (!externalDir.mkdirs()) {
                Log.d(TAG, "failed to create Impulse directory on SD card");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        return externalDir.getAbsolutePath() + File.separator + "IMG_" + timeStamp + ".jpg";
    }
}
