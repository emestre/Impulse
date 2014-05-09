package com.impulse.impulse;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.http.HttpStatus;

import java.io.File;

public class CreatePostActivity extends ActionBarActivity {

    private static final String TAG = "CreatePostActivity";

    private static final float TIME_STEP = 10;

    private int mRotation;
    private EditText mCaptionEditText;
    private EditText mCheckInEditText;
    private TextView mExpirationText;
    private ImageButton mShareButton;
    private Switch mAudienceSwitch;
    private ProgressDialog mUploadingProgress;
    private AlertDialog mUploadStatus;

    private String mImagePath;
    private String mUserId;
    private int mExpirationTime = 48 * 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        this.getActionBar().hide();

        setContentView(R.layout.activity_create_post);

        displayImage();
        initLayout();
        mUserId = getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");

        // save the image data in a background thread
        mImagePath = MediaFileHelper.getInternalCachePath(getApplicationContext());
        new SaveImageTask(mImagePath, new SaveImageCallback() {

            @Override
            public void onTaskComplete(boolean result, String path) {
                if (result) {
                    mShareButton.setEnabled(true);
                    CameraActivity.imageData = null;
                }
                else {
                    Log.d(TAG, "image couldn't be saved, can't upload");
                }
            }
        }).execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        File image = new File(mImagePath);
        boolean success = image.delete();

        if (success)
            Log.d(TAG, "deleting cached image...SUCCESS");
        else
            Log.d(TAG, "deleting cached image...FAILED");
    }

    private void displayImage() {
        ImageView image = (ImageView) findViewById(R.id.image_thumb);
        Bitmap pic = BitmapFactory.decodeByteArray(CameraActivity.imageData, 0,
                CameraActivity.imageData.length);
        mRotation = (Integer) getIntent().getExtras().get(CameraActivity.IMAGE_ROTATION_KEY);

        Log.d(TAG, "image view dimensions: " + image.getWidth() + " x " + image.getHeight());
        // rotate the image
        if (mRotation != 0) {
            // create the rotation matrix
            Matrix matrix = new Matrix();
            matrix.postRotate(mRotation);
            pic = Bitmap.createBitmap(pic, 0, 0, pic.getWidth(),
                    pic.getHeight(), matrix, false);
        }
        image.setImageBitmap(pic);
    }

    private void initLayout() {
        // get the caption field
        mCaptionEditText = (EditText) findViewById(R.id.caption_field);
        // get the image_check in text field
        mCheckInEditText = (EditText) findViewById(R.id.checkin_field);

        mAudienceSwitch = (Switch) findViewById(R.id.audience_switch);

        // handle the sliding bar changes
        mExpirationText = (TextView) findViewById(R.id.expiration_time);
        mExpirationText.setText("48 Hours");
        ((SeekBar) findViewById(R.id.time_slider)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setExpirationTime((float) progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // not handled for now
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int step = Math.round((float) seekBar.getProgress() / TIME_STEP);
                seekBar.setProgress(step * (int) TIME_STEP);
            }
        });

        // set the share button's on click listener
        mShareButton = (ImageButton) findViewById(R.id.share_button);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareButtonClick();
            }
        });

        LinearLayout container = (LinearLayout) findViewById(R.id.post_container);
        // set the root layout to receive touch events to hide keyboard
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // hide the keyboard if touch is received outside of keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                return true;
            }
        });
    }

    private void setExpirationTime(float progress) {
        int time = Math.round(progress / TIME_STEP);

        switch (time) {
            case 0:
                mExpirationText.setText("1 Hour");
                mExpirationTime = 60;
                break;

            case 1:
                mExpirationText.setText("2 Hours");
                mExpirationTime = 2 * 60;
                break;

            case 2:
                mExpirationText.setText("3 Hours");
                mExpirationTime = 3 * 60;
                break;

            case 3:
                mExpirationText.setText("4 Hours");
                mExpirationTime = 4 * 60;
                break;

            case 4:
                mExpirationText.setText("5 Hours");
                mExpirationTime = 5 * 60;
                break;

            case 5:
                mExpirationText.setText("6 Hours");
                mExpirationTime = 6 * 60;
                break;

            case 6:
                mExpirationText.setText("8 Hours");
                mExpirationTime = 8 * 60;
                break;

            case 7:
                mExpirationText.setText("10 Hours");
                mExpirationTime = 10 * 60;
                break;

            case 8:
                mExpirationText.setText("12 Hours");
                mExpirationTime = 12 * 60;
                break;

            case 9:
                mExpirationText.setText("24 Hours");
                mExpirationTime = 24 * 60;
                break;

            case 10:
                mExpirationText.setText("48 Hours");
                mExpirationTime = 48 * 60;
                break;
        }
    }

    private void shareButtonClick() {
        String caption = mCaptionEditText.getText().toString();
        if (caption.equals(""))
            caption = mUserId;

        String checkIn = mCheckInEditText.getText().toString();
        if (checkIn.equals(""))
            checkIn = mUserId;

        String audience = "everyone";
        if (mAudienceSwitch.isChecked())
            audience = "friends";

        Log.d(TAG, "uploading new post with...");
        Log.d(TAG, "user ID: " + mUserId);
        Log.d(TAG, "caption text: " + caption);
        Log.d(TAG, "location check in text: " + checkIn);
        Log.d(TAG, "audience: " + audience);
        Log.d(TAG, "timeout in minutes: " + mExpirationTime);
        Log.d(TAG, "rotate image: " + mRotation);

        RestClient client = new RestClient();
        client.postFile(mUserId, caption, 0, 0, mImagePath, "jpg", mExpirationTime,
                        mRotation, audience, checkIn, new PostCallback() {
            @Override
            public void onPostSuccess(String result) {

                mUploadingProgress.dismiss();

                if (Integer.parseInt(result) == HttpStatus.SC_OK) {
                    Log.d(TAG, "Uploading...SUCCESS");
                    mUploadStatus = buildSuccessDialog();
                }
                else {
                    Log.d(TAG, "Uploading...FAILED");
                    mUploadStatus = buildFailureDialog();
                }

                mUploadStatus.show();
            }
        });

        // display a loading spinner while the image is uploading
        mUploadingProgress = new ProgressDialog(this);
        mUploadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mUploadingProgress.setTitle("Uploading Post...");
        mUploadingProgress.setMessage("Your post is being created.");
        mUploadingProgress.setIndeterminate(true);
        mUploadingProgress.setCancelable(false);
        mUploadingProgress.show();
    }

    private AlertDialog buildSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Succeeded");
        builder.setMessage("Your post was successfully created.");
        builder.setCancelable(false);
        builder.setPositiveButton("View Posts", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), DrawerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        return builder.create();
    }

    private AlertDialog buildFailureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Failed");
        builder.setMessage("Sorry, there was a problem uploading your post.");
        builder.setCancelable(false);
        builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                shareButtonClick();
            }
        });
        builder.setNegativeButton("Abandon", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Intent intent = new Intent(getApplicationContext(),
                        DrawerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        return builder.create();
    }
}
