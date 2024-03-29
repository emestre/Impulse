package com.impulse.impulse;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;

import java.io.IOException;
import java.util.Arrays;

public class CameraActivity extends FragmentActivity {

    private static final String TAG = "CameraActivity";
    public static final String IMAGE_ROTATION_KEY = "rotation";
    private static final int MAX_RECORDING_LENGTH = 8000;

    public static int BACK_CAMERA;
    public static int FRONT_CAMERA;
    private Camera mCamera;
    private int mNumCams = Camera.getNumberOfCameras();
    private int mCameraId;
    private boolean isFocusing = false;
    private int mRotation = 270;
    public static byte imageData[];

    private MediaRecorder mMediaRecorder;
    private boolean mIsRecording = false;
    private String mVideoPath;

    private FrameLayout mPreviewFrame;
    private SurfaceView mPreviewSurface;
    private CameraPreview mPreview;
    private ImageButton mCaptureButton;
    private ImageView mSwitchCamera;
    private Button mToggleButton;

    private OrientationEventListener mOrientationListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the status bar and action bar before setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // KEEP this code here, moving it does not allow the event listener to be
        // disabled in onPause (kills battery life because sensor is always running)
        mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                calculateRotation(orientation);
            }
        };

        // link this activity to the camera layout file
        this.setContentView(R.layout.activity_camera);

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0; camIdx < mNumCams; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                BACK_CAMERA = camIdx;
                Log.d(TAG, "back camera ID: " + BACK_CAMERA);
            }
            else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                FRONT_CAMERA = camIdx;
                Log.d(TAG, "front camera ID: " + FRONT_CAMERA);
            }
        }
        mCameraId = BACK_CAMERA;

        // create the preview surface and initialize button listeners
        initPreview();
        initLayout();

        showOnBoardingPopUp();

        // open the camera in onResume() so it can be properly released and re-opened
    }

    private void initPreview() {
        mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        // create the surface view that holds the camera preview
        mPreviewSurface = new SurfaceView(this);
        mPreviewSurface.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                   ViewGroup.LayoutParams.MATCH_PARENT));
        // create our Preview object
        mPreview = new CameraPreview(this, mPreviewSurface);
    }

    private void initLayout() {

        // set the capture button's on click listener
        mCaptureButton = (ImageButton) findViewById(R.id.camera_capture_button);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageData = null;
                // disable after click because multiple presses causes crash
                mCaptureButton.setEnabled(false);

                // get an image from the camera
                mCamera.takePicture(null, null, mPicture);
                Log.d(TAG, "picture taken");
            }
        });

        // image_check if this device has a front facing camera
        if (mNumCams >= 2) {
            mSwitchCamera  = (ImageView) findViewById(R.id.switch_camera_button);
            mSwitchCamera.setVisibility(View.VISIBLE);

            mSwitchCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchCameraButtonClick();
                }
            });
        }

        // set the toggle button's on click listener
        mToggleButton = (Button) findViewById(R.id.toggle_button);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // set the frame layout to receive touch events for auto focus
        mPreviewFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mCamera != null && !isFocusing) {

                        isFocusing = true;
                        // focus the camera, no callback
                        mCamera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                // no longer attempting to auto focus
                                isFocusing = false;
                            }
                        });
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = getCameraInstance(mCameraId);
        mPreview.setCamera(mCamera);
        mCamera.startPreview();

        mPreviewFrame.addView(mPreviewSurface);
        mPreviewFrame.addView(mPreview);

        // enable the capture button
        mCaptureButton.setEnabled(true);

        // enable this activity to receive orientation change events
        mOrientationListener.enable();
    }

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

        // disable the orientation change events, without this we will keep receiving
        // the events even after this activity loses context
        mOrientationListener.disable();

        // release the MediaRecorder
        releaseMediaRecorder();
        // release the camera so it can be used by other applications
        releaseCamera();

        mPreviewFrame.removeView(mPreview);
        mPreviewFrame.removeView(mPreviewSurface);
    }

    private void releaseCamera() {
        if (mCamera != null){
            mCamera.stopPreview();
            mPreview.setCamera(null);
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;

            if (mCameraId == BACK_CAMERA)
                Log.d(TAG, "back camera has been released");
            else
                Log.d(TAG, "front camera has been released");
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;

            Log.d(TAG, "media recorder has been released");
        }
    }

    private void switchCameraButtonClick() {
        // release whatever camera we have no and remove preview surfaces
        releaseCamera();

        mPreviewFrame.removeView(mPreviewSurface);
        mPreviewFrame.removeView(mPreview);

        // toggle what camera we want to preview
        if (mCameraId == BACK_CAMERA)
            mCameraId = FRONT_CAMERA;
        else
            mCameraId = BACK_CAMERA;
        // re-open the camera
        mCamera = getCameraInstance(mCameraId);
        // initialize and start the preview
        initPreview();
        mPreview.setCamera(mCamera);
        mCamera.startPreview();
        // add the preview surfaces back to the screen
        mPreviewFrame.addView(mPreviewSurface);
        mPreviewFrame.addView(mPreview);
    }

    /** Callback to run when a picture has been taken. */
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Log.d(TAG, "image data length: " + data.length);

            imageData = Arrays.copyOf(data, data.length);
            Bundle bundle = new Bundle();
            bundle.putInt(IMAGE_ROTATION_KEY, mRotation);
            if (mCameraId == FRONT_CAMERA) {
                bundle.putBoolean("front facing", true);
            }
            else {
                bundle.putBoolean("front facing", false);
            }

            findViewById(R.id.image_fragment_container).setVisibility(View.VISIBLE);
            // hide the camera preview to show image fragment
            mPreviewFrame.setVisibility(View.GONE);
            mCaptureButton.setVisibility(View.GONE);
            if (mNumCams >= 2)
                mSwitchCamera.setVisibility(View.GONE);
            mCamera.startPreview();

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    FragmentManager manager = getSupportFragmentManager();

                    if (manager.getBackStackEntryCount() == 0) {
                        mPreviewFrame.setVisibility(View.VISIBLE);
                        mCaptureButton.setVisibility(View.VISIBLE);
                        if (mNumCams >= 2)
                            mSwitchCamera.setVisibility(View.VISIBLE);
                        mCaptureButton.setEnabled(true);
                    }
                }
            });

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ImageFragment fragment = new ImageFragment();
            fragment.setArguments(bundle);
            fragmentTransaction.add(R.id.image_fragment_container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
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
            }
            else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // lock the camera on all APIs if prepare failed
                mCamera.lock();
                // inform user
                Toast.makeText(this, "Insert SD card to take video", Toast.LENGTH_SHORT).show();
            }
        }
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
            return false;
        }
        catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void onStopRecording() {

        // inform the user that recording has stopped
        mIsRecording = false;
        // release the MediaRecorder object
        releaseMediaRecorder();
        // lock the camera for API < 14
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mCamera.lock();
            Log.d(TAG, "camera locked after recording stopped");
        }

        Log.d(TAG, "recording stopped");
    }

    private void calculateRotation(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            mRotation = 270;
            return;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        orientation = (orientation + 45) / 90 * 90;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mRotation = (info.orientation - orientation + 360) % 360;
        }
        else {  // back-facing camera
            mRotation = (info.orientation + orientation) % 360;
        }
    }

    private void showOnBoardingPopUp() {

        final SharedPreferences preferences = this.getPreferences(Activity.MODE_PRIVATE);

        if (!preferences.contains("first run camera")) {
            new ShowcaseView.Builder(this, true)
                    .setContentTitle("Capture the Moment")
                    .setContentText("Show us what you’re doing…\nor what you wish you were doing!")
                    .setStyle(R.style.ImpulseShowcaseView)
                    .hideOnTouchOutside()
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("first run camera", false);
                            editor.commit();
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {}

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {
                            showcaseView.hideButton();
                            showcaseView.setShouldCentreText(true);
                        }
                    })
                    .build();
        }
    }
}
