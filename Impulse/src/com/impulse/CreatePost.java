package com.impulse;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class CreatePost extends Activity {

    public static final String TAG = "CreatePost";

    private String mPathToMedia;
    private boolean mSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        displayMedia();
    }

    private void displayMedia() {
        Bundle extras = getIntent().getExtras();
        int type = (Integer) extras.get(CameraActivity.MEDIA_TYPE_KEY);

        mPathToMedia = (String) getIntent().getExtras().get(CameraActivity.PATH_KEY);

        String toastText;
        FrameLayout mediaView = (FrameLayout) findViewById(R.id.media_view);
        if (type == MediaFileHelper.MEDIA_TYPE_IMAGE) {
            ImageView image = new ImageView(this);

            // try to read the EXIF tags of the JPG image
            int degrees = getRotation();
            Log.d(TAG, "rotation = " + degrees);

            // rotate the image by the amount indicated in the EXIF tags
            Bitmap picture = BitmapFactory.decodeFile(mPathToMedia);
            if (degrees != 0) {
                Matrix rotateMatrix = new Matrix();
                rotateMatrix.postRotate(degrees);
                picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(),
                        picture.getHeight(), rotateMatrix, false);
            }
            image.setImageBitmap(picture);
            mediaView.addView(image);

            toastText = "image received";
        }
        else if (type == MediaFileHelper.MEDIA_TYPE_VIDEO) {
            VideoView video = new VideoView(this);
            video.setVideoPath(mPathToMedia);
            mediaView.addView(video);
            video.start();

            toastText = "video received";
        }
        else {
            toastText = "unknown media file received";
        }

        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
    }

    private int getRotation() {
        ExifInterface exif = null;
        int degrees = 0;

        try {
            exif = new ExifInterface(mPathToMedia);
        }
        catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

        if (exif != null) {
            // get the orientation attribute of the image
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degrees = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                degrees = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                degrees = 270;
                break;
            }
        }

        return degrees;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!mSaved) {
            File image = new File(mPathToMedia);
            boolean success = image.delete();

            if (success)
                Log.d(TAG, "file deletion SUCCEEDED");
            else
                Log.d(TAG, "file deletion FAILED");
        }
        else {
            Log.d(TAG, "file was moved to SD card");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int itemId = item.getItemId();

        if (itemId == R.id.submenu_save) {
//            String toastText;
//
//            if (MediaFileHelper.moveFileToSDCard(mPathToMedia)) {
//                toastText = "Saved to SD Card";
//                mSaved = true;
//                MediaFileHelper.runMediaScanner(this);
//            }
//            else {
//                toastText = "Save Failed";
//                mSaved = false;
//            }
//            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

            Toast.makeText(this, "does nothing right now", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (itemId == R.id.submenu_post) {
            // handle uploading post here
            Toast.makeText(this, "upload selected", Toast.LENGTH_SHORT).show();

            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

}
