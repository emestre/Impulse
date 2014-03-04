package com.impulse.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

public class CreatePost extends ActionBarActivity {

    public static final String TAG = "CreatePost";
    public static final String UPLOAD_SUCCESS = "200";

    private String mPathToMedia;
    private ProgressDialog mUploadingProgress;

    private EditText mCaption;
    private FrameLayout mFrameLayout;

    // post expiration time in hours
    private int mPostExpireTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        initLayout();
        displayMedia();
    }

    private void initLayout() {

        mFrameLayout = (FrameLayout) findViewById(R.id.media_view);
        // set the frame layout to receive touch events to hide keyboard
        mFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // hide the keyboard if touch is received outside of keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                return true;
            }
        });

        // set the caption text field text color to white if not running
        // API > 10, newer text fields have transparent background (black for this screen)
        mCaption = (EditText) findViewById(R.id.caption_field);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            mCaption.setTextColor(Color.WHITE);
        }
    }

    private void displayMedia() {
        Bundle extras = getIntent().getExtras();
        int type = (Integer) extras.get(CameraActivity.MEDIA_TYPE_KEY);
        mPathToMedia = (String) extras.get(CameraActivity.PATH_KEY);
        int cameraId = (Integer) extras.get(CameraActivity.CAMERA_ID_KEY);

        if (type == MediaFileHelper.MEDIA_TYPE_IMAGE) {
            ImageView image = new ImageView(this);

            // try to read the EXIF tags of the JPG image
            int degrees = getExifRotation();
            Log.d(TAG, "rotation = " + degrees);

            Bitmap picture = BitmapFactory.decodeFile(mPathToMedia);
            Matrix rotateMatrix = new Matrix();

            // rotate the image by the amount indicated in the EXIF tags
            // or mirror the image if taken with the front camera
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
            mFrameLayout.addView(image);

            Log.d(TAG, "image received");
        }
        else if (type == MediaFileHelper.MEDIA_TYPE_VIDEO) {
            VideoView video = new VideoView(this);
            video.setVideoPath(mPathToMedia);
            mFrameLayout.addView(video);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem expirationItem = menu.findItem(R.id.menu_expiration);

        if (expirationItem != null) {
            switch (mPostExpireTime) {
                case 1:
                    expirationItem.setTitle("1 Hour");
                    break;

                case 6:
                    expirationItem.setTitle("6 Hours");
                    break;

                case 12:
                    expirationItem.setTitle("12 Hours");
                    break;

                case 24:
                    expirationItem.setTitle("24 Hours");
                    break;

                case 48:
                    expirationItem.setTitle("48 Hours");
                    break;

                case 72:
                    expirationItem.setTitle("72 Hours");
                    break;

                default:
                    expirationItem.setTitle(getString(R.string.menu_expire_text));
                    break;
            }
        }
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
        else if (itemId == R.id.submenu_1) {
            mPostExpireTime = 1;
            supportInvalidateOptionsMenu();
            return true;
        }
        else if (itemId == R.id.submenu_6) {
            mPostExpireTime = 6;
            supportInvalidateOptionsMenu();
            return true;
        }
        else if (itemId == R.id.submenu_12) {
            mPostExpireTime = 12;
            supportInvalidateOptionsMenu();
            return true;
        }
        else if (itemId == R.id.submenu_24) {
            mPostExpireTime = 24;
            supportInvalidateOptionsMenu();
            return true;
        }
        else if (itemId == R.id.submenu_48) {
            mPostExpireTime = 48;
            supportInvalidateOptionsMenu();
            return true;
        }
        else if (itemId == R.id.submenu_72) {
            mPostExpireTime = 72;
            supportInvalidateOptionsMenu();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void uploadPost() {

        final String captionText = mCaption.getText().toString();
        // post expiration time in minutes
        final int timeout;

        if (mPostExpireTime == 0) {
            timeout = 48 * 60;
        }
        else {
            timeout = mPostExpireTime * 60;
        }

        Session session = Session.getActiveSession();
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                String userId;
                if (user != null) {
                    userId = user.getId();

                    Log.d(TAG, "caption text = " + captionText);
                    Log.d(TAG, "timeout in minutes = " + timeout);
                    RestClient client = new RestClient();
                    client.postFile(userId, captionText, 0, 0, mPathToMedia, "jpg", timeout, new PostCallback() {
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
