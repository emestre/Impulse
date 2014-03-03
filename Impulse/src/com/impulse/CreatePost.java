package com.impulse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import java.io.File;
import java.io.IOException;

public class CreatePost extends Activity {

    public static final String TAG = "CreatePost";
    public static final String UPLOAD_SUCCESS = "200";

    private String mPathToMedia;
    private ProgressDialog mUploadingProgress;

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
        int cameraId = (Integer) getIntent().getExtras().get(CameraActivity.CAMERA_ID_KEY);

        FrameLayout mediaView = (FrameLayout) findViewById(R.id.media_view);
        if (type == MediaFileHelper.MEDIA_TYPE_IMAGE) {
            ImageView image = new ImageView(this);

            // try to read the EXIF tags of the JPG image
            int degrees = getExifRotation();
            Log.d(TAG, "rotation = " + degrees);

            // rotate the image by the amount indicated in the EXIF tags
            Bitmap picture = BitmapFactory.decodeFile(mPathToMedia);
            Matrix rotateMatrix = new Matrix();
            if (cameraId == CameraActivity.FRONT_CAMERA) {
                rotateMatrix.preScale(-1, 1);
                rotateMatrix.postRotate(90);
            }
            else {
                rotateMatrix.postRotate(degrees);
            }
            picture = Bitmap.createBitmap(picture, 0, 0, picture.getWidth(),
                    picture.getHeight(), rotateMatrix, false);

            image.setImageBitmap(picture);
            mediaView.addView(image);

            Log.d(TAG, "image received");
        }
        else if (type == MediaFileHelper.MEDIA_TYPE_VIDEO) {
            VideoView video = new VideoView(this);
            video.setVideoPath(mPathToMedia);
            mediaView.addView(video);
            video.start();

            Log.d(TAG, "video received");
        }
        else {
            Log.d(TAG, "unknown media received");
        }

    }

    private int getExifRotation() {
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

        File image = new File(mPathToMedia);
        boolean success = image.delete();

        if (success)
            Log.d(TAG, "file deletion SUCCEEDED");
        else
            Log.d(TAG, "file deletion FAILED");
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

        if (itemId == R.id.menu_home) {
            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            return true;
        }
        else if (itemId == R.id.submenu_save) {
            String toastText;

            if (MediaFileHelper.moveFileToSDCard(mPathToMedia)) {
                toastText = "Saved to SD Card";
                MediaFileHelper.runMediaScanner(this);
            }
            else {
                toastText = "Save Failed";
            }
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

            return true;
        }
        else if (itemId == R.id.submenu_post) {
            uploadPost();

            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void uploadPost() {

        Session session = Session.getActiveSession();
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                String userId;
                if (user != null) {
                    userId = user.getId();

                    RestClient client = new RestClient();
                    // timeout in minutes
                    client.postFile(userId, "test caption", 0, 0, mPathToMedia, "jpg", 0, new PostCallback() {
                        @Override
                        public void onPostSuccess(String result) {
                            mUploadingProgress.dismiss();

                            if (result.equals(UPLOAD_SUCCESS)) {
                                Log.d(TAG, "upload succeeded");
                                Toast.makeText(getApplicationContext(), "upload succeeded", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Log.d(TAG, "upload failed");
                                Toast.makeText(getApplicationContext(), "upload failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).executeAsync();

        // display a loading spinner while the bill is uploading
        mUploadingProgress = new ProgressDialog(this);
        mUploadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mUploadingProgress.setTitle("Uploading Post...");
        mUploadingProgress.setMessage("Your post is being created.");
        mUploadingProgress.setIndeterminate(true);
        mUploadingProgress.setCancelable(false);
        mUploadingProgress.show();
    }

}
