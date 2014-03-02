package com.impulse;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/** This activity that will display the camera preview. */
public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";
    public static final String PATH_KEY = "path";
    public static final String MEDIA_TYPE_KEY = "type";
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;
    private static final int MAX_RECORDING_LENGTH = 8000;

    private Camera mCamera;
    private CameraPreview mPreview;
    private SurfaceView mPreviewSurface;
    private Button mRecordButton;
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecording = false;
    private String mVideoPath;
    private int mCameraState = BACK_CAMERA;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the status bar and action bar before setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // link this activity to the camera layout file
        this.setContentView(R.layout.activity_camera);

        // create the preview surface and initialize button listeners
        initLayout();

        // open the camera in onResume() so it can be properly released and re-opened
    }

    private void initLayout() {
        // create our Preview object
        mPreviewSurface = (SurfaceView) findViewById(R.id.preview_surface);
        mPreview = new CameraPreview(this, mPreviewSurface);
        // set the preview object as the view of the FrameLayout
        ((FrameLayout) findViewById(R.id.camera_preview)).addView(mPreview);

        // set the record button's on click listener
        mRecordButton = (Button) findViewById(R.id.camera_record_button);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                recordButtonClick();
                Toast.makeText(getApplicationContext(), "disabled right now", Toast.LENGTH_SHORT).show();
            }
        });

        // set the capture button's on click listener
        findViewById(R.id.camera_capture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get an image from the camera
                mCamera.takePicture(null, null, mPicture);
                Log.d(TAG, "picture taken");
            }
        });

        // check if this device has a front facing camera
        if (Camera.getNumberOfCameras() >= 2) {
            Button switchCamera  = (Button) findViewById(R.id.switch_camera_button);
            switchCamera.setEnabled(true);
            switchCamera.setVisibility(View.VISIBLE);

            switchCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchCameraButtonClick();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = getCameraInstance(mCameraState);
        mPreview.setCamera(mCamera);
        mCamera.startPreview();
    }

    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance(int id) {
        Camera c = null;

        try {
            // attempt to get a Camera instance
            c = Camera.open(id);

            if (id == BACK_CAMERA)
                Log.d(TAG, "back facing camera has been opened");
            else
                Log.d(TAG, "front facing camera has been opened");
        }
        catch (Exception e) {
            // camera is not available (in use or does not exist)
            Log.d(TAG, "camera object could no be obtained: in use or does not exist");
        }

        // returns null if camera is unavailable
        return c;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // release the MediaRecorder
        releaseMediaRecorder();
        // release the camera so it can be used by other applications
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null){
            mCamera.stopPreview();
            mPreview.setCamera(null);
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;

            Log.d(TAG, "camera has been released");
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();

            Log.d(TAG, "media recorder has been released");
        }
    }

    /** Callback to run when a picture has been taken. */
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            // get the path to the new image file in internal storage
            String path = MediaFileHelper.getInternalCachePath(getApplicationContext(), MediaFileHelper.MEDIA_TYPE_IMAGE);

            // try to write the image data to storage
            try {
                FileOutputStream fos = new FileOutputStream(path);
                fos.write(data);
                fos.close();
            }
            catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            }
            catch (IOException e) {
                Log.d(TAG, "Error writing to file: " + e.getMessage());
            }

//            Intent intent = new Intent(getApplicationContext(), ReviewPostActivity.class);
//            // store the media type in the intent
//            intent.putExtra(MEDIA_TYPE_KEY, MediaFileHelper.MEDIA_TYPE_IMAGE);
//            // store the path to the media
//            intent.putExtra(PATH_KEY, path);
//            // start the preview post activity
//            startActivity(intent);
        }
    };

    public void recordButtonClick () {

        if (mIsRecording) {
            // stop recording
            mMediaRecorder.stop();
            onStopRecording();
        }
        else {
            // initialize video camera
            boolean prepare = prepareVideoRecorder();
            if (prepare) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();
                Log.d(TAG, "recording started");

                // inform the user that recording has started
                mIsRecording = true;
                mRecordButton.setText("Stop Recording");
            }
            else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
                Toast.makeText(this, "Insert SD card to take video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onStopRecording() {

        // release the MediaRecorder object
        releaseMediaRecorder();
        // inform the user that recording has stopped
        mIsRecording = false;
        mRecordButton.setText("Start Recording");

//        Intent intent = new Intent(getApplicationContext(), ReviewPostActivity.class);
//        // store the media type in the intent
//        intent.putExtra(MEDIA_TYPE_KEY, MediaFileHelper.MEDIA_TYPE_VIDEO);
//        // store the path to the media
//        intent.putExtra(PATH_KEY, mVideoPath);
//        // start the preview post activity
//        startActivity(intent);

        Log.d(TAG, "recording stopped");
    }

    private boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {

                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    // recording stopped due to 8 second limit
                    onStopRecording();
                    Log.d(TAG, "8 second video limit reached");
                }
            }
        });

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // set video format
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
//        mMediaRecorder.setVideoSize(480, 360);
//        mMediaRecorder.setVideoFrameRate(30);
//        mMediaRecorder.setVideoEncodingBitRate(500000);

        // set max duration to 8 seconds
        mMediaRecorder.setMaxDuration(MAX_RECORDING_LENGTH);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mVideoPath = MediaFileHelper.getExternalCachePath(getApplicationContext(), MediaFileHelper.MEDIA_TYPE_VIDEO);
        if (mVideoPath == null) {
            Log.d(TAG, "SD card is not mounted");
            return false;
        }
        mMediaRecorder.setOutputFile(mVideoPath);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreviewSurface.getHolder().getSurface());

        // set the orientation hint for video view - need SUPPORT for more orientations ----------
        mMediaRecorder.setOrientationHint(90);

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        }
        catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void switchCameraButtonClick() {
        // toggle what camera we want to preview
        if (mCameraState == BACK_CAMERA)
            mCameraState = FRONT_CAMERA;
        else
            mCameraState = BACK_CAMERA;

        releaseCamera();
        // re-open the camera
        mCamera = getCameraInstance(mCameraState);
        // have to set reset our surface to preview the new camera
        try {
            if (mCamera != null)
                mCamera.setPreviewDisplay(mPreviewSurface.getHolder());
        }
        catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }

        // initialize and start the preview
        mPreview.setCamera(mCamera);
        mCamera.startPreview();
    }
}
