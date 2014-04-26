package com.impulse.android;

import android.os.AsyncTask;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveImageTask extends AsyncTask<String, String, Boolean> {

    private static final String TAG = "SaveImageTask";

    private String mPath;
    private SaveImageCallback mCallback;

    public SaveImageTask(String str, SaveImageCallback callback) {
        this.mPath = str;
        this.mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(String... args) {

        // try to write the image data to storage
        try {
            FileOutputStream fos = new FileOutputStream(mPath);
            fos.write(CameraActivity.imageData);
            fos.close();
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
            return false;
        }
        catch (IOException e) {
            Log.d(TAG, "Error writing to file: " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result)
            Log.d(TAG, "saving image...SUCCESS");
        else
            Log.d(TAG, "saving image...FAILED");

        this.mCallback.onTaskComplete(result, mPath);
    }
}