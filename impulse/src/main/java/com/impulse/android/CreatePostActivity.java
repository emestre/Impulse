package com.impulse.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private RadioGroup mAudienceButtons;
    private TextView mExpirationText;

    private String mImagePath;
    private String mAudience;
    private int mExpirationTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        displayImage();
        initLayout();
        saveImageToCache();
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
        mCaptionEditText = (EditText) findViewById(R.id.caption_field);

        mAudienceButtons = (RadioGroup) findViewById(R.id.audience_radio);
        mAudienceButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                audienceSelectionChanged(checkedId);
            }
        });
        mAudienceButtons.check(R.id.everyone);

        mExpirationText = (TextView) findViewById(R.id.expiration_time);
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void saveImageToCache() {
        mImagePath = MediaFileHelper.getInternalCachePath(getApplicationContext(),
                                            MediaFileHelper.MEDIA_TYPE_IMAGE);

        new SaveImage().execute(mImagePath);
    }

    private class SaveImage extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... args) {

            Log.d(TAG, "path: " + args[0]);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result)
                Log.d(TAG, "async task done");
        }
    }
}
