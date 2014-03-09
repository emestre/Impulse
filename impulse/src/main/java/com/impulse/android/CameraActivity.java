package com.impulse.android;

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
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

public class CameraActivity extends FragmentActivity {

    private static final String TAG = "CameraActivity";
    public static final String PATH_KEY = "path";
    public static final String IMAGE_ROTATION_KEY = "rotation";
    public static final int BACK_CAMERA = 0;
    public static final int FRONT_CAMERA = 1;
    private static final int MAX_RECORDING_LENGTH = 8000;

    private Camera mCamera;
    private int mNumCams = Camera.getNumberOfCameras();
    private int mCameraId = BACK_CAMERA;
    private boolean isFocusing = false;
    private int mRotation = 90;
    public static byte imageData[];

    private MediaRecorder mMediaRecorder;
    private boolean mIsRecording = false;
    private String mVideoPath;

    private FrameLayout mPreviewFrame;
    private SurfaceView mPreviewSurface;
    private CameraPreview mPreview;
    private Button mCaptureButton;
    private Button mSwitchCamera;
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

        // create the preview surface and initialize button listeners
        initPreview();
        initLayout();

        // open the camera in onResume() so it can be properly released and re-opened
    }

    private void initPreview() {
        mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        // create the surface view that holds the camera preview
        mPreviewSurface = new SurfaceView(this);
        mPreviewSurface.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                   ViewGroup.LayoutParams.MATCH_PARENT));
        mPreviewFrame.addView(mPreviewSurface);
        // create our Preview object
        mPreview = new CameraPreview(this, mPreviewSurface);
        // set the preview object as the view of the FrameLayout
        mPreviewFrame.addView(mPreview);
    }

    private void initLayout() {

        // set the capture button's on click listener
        mCaptureButton = (Button) findViewById(R.id.camera_capture_button);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // disable after click because multiple presses causes crash
                mCaptureButton.setEnabled(false);

                // get an image from the camera
                mCamera.takePicture(null, null, mPicture);
                Log.d(TAG, "picture taken");
            }
        });

        // check if this device has a front facing camera
        if (mNumCams >= 2) {
            mSwitchCamera  = (Button) findViewById(R.id.switch_camera_button);
            mSwitchCamera.setEnabled(true);
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

        // enable the capture button
        mCaptureButton.setEnabled(true);

        // enable this activity to receive orientation change events
        mOrientationListener.enable();
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

        // disable the orientation change events, without this we will keep receiving
        // the events even after this activity loses context
        mOrientationListener.disable();

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
        // toggle what camera we want to preview
        if (mCameraId == BACK_CAMERA)
            mCameraId = FRONT_CAMERA;
        else
            mCameraId = BACK_CAMERA;

        releaseCamera();
        mPreviewFrame.removeView(mPreviewSurface);
        mPreviewFrame.removeView(mPreview);

        // re-open the camera
        mCamera = getCameraInstance(mCameraId);
        initPreview();
        // initialize and start the preview
        mPreview.setCamera(mCamera);
    }

    /** Callback to run when a picture has been taken. */
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            imageData = Arrays.copyOf(data, data.length);
            Bundle bundle = new Bundle();
            bundle.putInt(IMAGE_ROTATION_KEY, mRotation);

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
            PreviewFragment fragment = new PreviewFragment();
            fragment.setArguments(bundle);
            fragmentTransaction.add(R.id.image_fragment_container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            mPreviewFrame.setVisibility(View.GONE);
            mCaptureButton.setVisibility(View.GONE);
            if (mNumCams >= 2)
                mSwitchCamera.setVisibility(View.GONE);
            mCamera.startPreview();



//            // get the path to the new image file in internal storage
//            String path = MediaFileHelper.getInternalCachePath(getApplicationContext(),
//                                            MediaFileHelper.MEDIA_TYPE_IMAGE);
//
//            // try to write the image data to storage
//            try {
//                FileOutputStream fos = new FileOutputStream(path);
//                fos.write(data);
//                fos.close();
//            }
//            catch (FileNotFoundException e) {
//                Log.d(TAG, "File not found: " + e.getMessage());
//            }
//            catch (IOException e) {
//                Log.d(TAG, "Error writing to file: " + e.getMessage());
//            }
//
//            Intent intent = new Intent(getApplicationContext(), CreatePost.class);
//            // store the media type in the intent
//            intent.putExtra(MEDIA_TYPE_KEY, MediaFileHelper.MEDIA_TYPE_IMAGE);
//            // store which camera we took the picture with
//            intent.putExtra(CAMERA_ID_KEY, mCameraId);
//            // store the rotation
//            intent.putExtra(IMAGE_ROTATION_KEY, mRotation);
//            // store the path to the picture
//            intent.putExtra(PATH_KEY, path);
//
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

//        Intent intent = new Intent(getApplicationContext(), CreatePost.class);
//        // store the media type in the intent
//        intent.putExtra(MEDIA_TYPE_KEY, MediaFileHelper.MEDIA_TYPE_VIDEO);
//        // store the path to the media
//        intent.putExtra(PATH_KEY, mVideoPath);
//        // start the preview post activity
//        startActivity(intent);

        Log.d(TAG, "recording stopped");
    }

    private void calculateRotation(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN)
            return;
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
}
