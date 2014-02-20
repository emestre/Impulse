package com.impulse;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ViewConstructor")
public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
	
    private static final String TAG = "CameraPreview";
    
    private SurfaceView mSurfaceView;
	private SurfaceHolder mHolder;
    private Camera mCamera;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    
    @SuppressWarnings("deprecation")
	public CameraPreview(Context context) {
        super(context);
        
        mSurfaceView = new SurfaceView(context);
        this.addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public Surface getSurface() {
    	return mHolder.getSurface();
    }
    
    public void setCamera(Camera camera) {
    	mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            this.requestLayout();
        }        
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } 
        catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } 
        catch (Exception e){
          // ignore: tried to stop a non-existent preview
        	Log.d(TAG, "attempted to stop preview when no camera is opened");
        }

        // set preview size and make any resize, rotate or reformatting changes here

        // get the camera parameters
        Camera.Parameters params = mCamera.getParameters();
        
        // set camera preview to auto focus if available
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
        	// auto focus mode is supported
        	params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        
        // rotate camera 90 degrees to portrait
        mCamera.setDisplayOrientation(90);
        // rotate the image file so it's oriented correctly (portrait) when viewed in photo viewer
        params.setRotation(90);
        
        // set the preview size to the aspect ratio calculated by getOptimalPreviewSize()
        params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        this.requestLayout();
        // update the camera object parameters
        mCamera.setParameters(params);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } 
        catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        this.setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
           mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
           
           Log.d(TAG, "screen width = " + width + " screen height = " + height);
           Log.d(TAG, "width = " + mPreviewSize.width + " height = " + mPreviewSize.height);
        }
    }
    
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) 
        	return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) 
            	continue;
            
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        
        return optimalSize;
    }

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
//            if (mPreviewSize != null) {
//                previewWidth = mPreviewSize.width;
//                previewHeight = mPreviewSize.height;
//            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
	}
}
