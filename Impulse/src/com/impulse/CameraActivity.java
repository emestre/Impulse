package com.impulse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/** The activity that will display the camera preview. */
public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";
    
	private Camera mCamera;
    private CameraPreview mPreview;
    private ImageButton mRecordButton;
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecording = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // hide the status bar and action bar before setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        						WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // link this activity to the camera layout file
        this.setContentView(R.layout.camera_layout);
        
        // create our Preview object
        mPreview = new CameraPreview(this);
        // set the preview object as the view of the FrameLayout
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        // get the record button
        mRecordButton = (ImageButton) findViewById(R.id.camera_record_button);
        
        // open the camera in onResume() so it can be properly released and re-opened
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mCamera = getCameraInstance();
        mPreview.setCamera(mCamera);
        
        Log.d(TAG, "new camera instance has been created");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // release the MediaRecorder
        releaseMediaRecorder();
        
        // release the camera immediately on pause event so it can be used by other apps
        if (mCamera != null){
        	mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        
        Log.d(TAG, "camera preview paused, camera has been released");
    }
    
    private void releaseMediaRecorder() {
    	
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }
    
    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance() {
        Camera c = null;
        
        try {
        	// attempt to get a Camera instance
            c = Camera.open();
        }
        catch (Exception e) {
            // camera is not available (in use or does not exist)
        	Log.d(TAG, "camera object could no be obtained: in use or does not exist");
        }
        
        // returns null if camera is unavailable
        return c;
    }
    
    /** Listener method for the capture button. Takes a snapshot of the camera preview. */
    public void captureButtonClick(View view) {
    	// get an image from the camera
        mCamera.takePicture(null, null, mPicture);
        Log.d(TAG, "picture taken");
    }
    
    /** Callback to run when a picture has been taken. */
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	
        	// create a file for the image to be saved as
            File image = MediaFileHelper.getOutputMediaFile(MediaFileHelper.MEDIA_TYPE_IMAGE);
            
            // code that saves the image to the SD card
            if (image == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            
            try {
                FileOutputStream fos = new FileOutputStream(image);
                fos.write(data);
                fos.close();
            }
            catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            }
            catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            // end of code that saves the image to the SD card
        	
            // restart the camera preview so we can take another picture
        	mCamera.startPreview();
        }
    };
    
    public void recordButtonClick (View view) {
    	
    	if (mIsRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            mIsRecording = false;
            mRecordButton.setImageResource(R.drawable.ic_action_video);
        }
    	else {
            // initialize video camera
    		boolean prepare = prepareVideoRecorder();
            if (prepare) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                // inform the user that recording has started
                mIsRecording = true;
                mRecordButton.setImageResource(R.drawable.ic_action_stop);
            }
            else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }
    }
    
    private boolean prepareVideoRecorder(){

        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(MediaFileHelper.getOutputMediaFile(MediaFileHelper.MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(CameraPreview.mHolder.getSurface());

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
}
