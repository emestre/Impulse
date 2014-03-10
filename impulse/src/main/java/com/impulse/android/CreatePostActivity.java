package com.impulse.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreatePostActivity extends ActionBarActivity {

    private static final String TAG = "CreatePostActivity";
    private static final String FRIENDS = "friends";
    private static final String EVERYONE = "everyone";

    private static final float TIME_STEP = 25;
    private static final int ONE_HOUR = 0;
    private static final int THREE_HOURS = 1;
    private static final int TWELVE_HOURS = 2;
    private static final int ONE_DAY = 3;
    private static final int TWO_DAYS = 4;

    private int mRotation;
    private EditText mCaptionEditText;
    private TextView mExpirationText;
    private Button mShareButton;
    private ProgressDialog mUploadingProgress;
    private AlertDialog mUploadStatus;

    private String mImagePath;
    private String mAudience;
    private int mExpirationTime = 48 * 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        displayImage();
        initLayout();

        // save the image data in a background thread
        mImagePath = MediaFileHelper.getInternalCachePath(getApplicationContext());
        new SaveImageTask().execute(mImagePath);
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

        // handle the audience radio button listeners
        RadioGroup audienceButtons = (RadioGroup) findViewById(R.id.audience_radio);
        audienceButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                audienceSelectionChanged(checkedId);
            }
        });
        audienceButtons.check(R.id.everyone);

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
        mShareButton = (Button) findViewById(R.id.share_button);
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

    private void audienceSelectionChanged(int id) {
        if (id == R.id.friends) {
            mAudience = FRIENDS;
        }
        else if (id == R.id.everyone) {
            mAudience = EVERYONE;
        }
    }

    private void setExpirationTime(float progress) {
        int time = Math.round(progress / TIME_STEP);

        switch (time) {
            case ONE_HOUR:
                mExpirationText.setText("1 Hour");
                mExpirationTime = 60;
                break;

            case THREE_HOURS:
                mExpirationText.setText("3 Hours");
                mExpirationTime = 3 * 60;
                break;

            case TWELVE_HOURS:
                mExpirationText.setText("12 Hours");
                mExpirationTime = 12 * 60;
                break;

            case ONE_DAY:
                mExpirationText.setText("24 Hours");
                mExpirationTime = 24 * 60;
                break;

            case TWO_DAYS:
                mExpirationText.setText("48 Hours");
                mExpirationTime = 48 * 60;
                break;
        }
    }

    private void shareButtonClick() {
        // get the user's unique facebook ID from shared preferences
        String userId = getSharedPreferences("com.impulse", Context.MODE_PRIVATE).getString("UserId", "");
        String caption = mCaptionEditText.getText().toString();
        if (caption.equals(""))
            caption = userId;

        Log.d(TAG, "uploading new post with...");
        Log.d(TAG, "user ID: " + userId);
        Log.d(TAG, "caption text: " + caption);
        Log.d(TAG, "audience: " + mAudience);
        Log.d(TAG, "timeout in minutes: " + mExpirationTime);
        Log.d(TAG, "rotate image: " + mRotation);

        RestClient client = new RestClient();
        client.postFile(userId, caption, 0, 0, mImagePath, "jpg", mExpirationTime, mRotation, new PostCallback() {
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

                RestClient client = new RestClient();
                client.getPostList(new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("POST_LIST", response);
                        startActivity(intent);
                    }
                });
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


                RestClient client = new RestClient();
                client.getPostList(new GetCallback() {
                    @Override
                    void onDataReceived(String response) {
                        Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("POST_LIST", response);
                        startActivity(intent);
                    }
                });
            }
        });

        return builder.create();
    }

    private class SaveImageTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... args) {

            // try to write the image data to storage
            try {
                FileOutputStream fos = new FileOutputStream(args[0]);
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
            if (result) {
                mShareButton.setEnabled(true);
                Log.d(TAG, "saving image to internal cache...SUCCESS");
                CameraActivity.imageData = null;
            }
            else {
                Log.d(TAG, "saving image to internal cache...FAILED");
            }
        }
    }
}
