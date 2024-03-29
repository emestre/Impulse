package com.impulse.impulse;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private SurfaceHolder mHolder;

    private Camera mCamera;
    private Camera.Parameters mCamParams;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private List<Camera.Size> mSupportedPictureSizes;
    private Camera.Size mPictureSize;

    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, SurfaceView surface) {
        super(context);

        mHolder = surface.getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        setFocusableInTouchMode(true);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (camera != null) {
            mCamParams = mCamera.getParameters();
            mSupportedPreviewSizes = mCamParams.getSupportedPreviewSizes();
            mSupportedPictureSizes = mCamParams.getSupportedPictureSizes();
            this.requestLayout();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // do nothing, wait until surface changed
        Log.d(TAG, "preview surface has been created");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty, camera is released in CameraActivity
        Log.d(TAG, "preview surface has been destroyed");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d(TAG, "preview surface changed");
        initSurface();
    }

    private void initSurface() {
        if (mCamera != null && mHolder.getSurface() != null) {

            mCamera.stopPreview();

            if (mPreviewSize.width > mPreviewSize.height) {
                // rotate camera 90 degrees to portrait
                mCamera.setDisplayOrientation(90);
                Log.d(TAG, "display orientation set to: 90");
            }

            Log.d(TAG, "supported preview sizes:");
            for (Camera.Size size : mSupportedPreviewSizes)
                Log.d(TAG, size.width + " x " + size.height);

            // set the preview size to the aspect ratio calculated by getOptimalPreviewSize()
            mCamParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            Log.d(TAG, "preview size set to: " + mPreviewSize.width + " x " + mPreviewSize.height);

            Log.d(TAG, "supported picture sizes:");
            for (Camera.Size size : mSupportedPictureSizes)
                Log.d(TAG, size.width + " x " + size.height);

            mCamParams.setPictureSize(mPictureSize.width, mPictureSize.height);
            Log.d(TAG, "picture size set to: " + mPictureSize.width + " x " + mPictureSize.height);

            // decrease JPG quality to reduce file size
            mCamParams.setJpegQuality(25);

            boolean autoFocus = false;
            boolean continuousPicture = false;
            Log.d(TAG, "supported focus modes:");
            for (String s : mCamParams.getSupportedFocusModes()) {
                Log.d(TAG, s);
                if (s.equals(Camera.Parameters.FOCUS_MODE_AUTO))
                    autoFocus = true;
                else if (s.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                    continuousPicture = true;
            }

            if (continuousPicture) {
                mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                Log.d(TAG, "continuous picture focus set");
            }
            else if (autoFocus) {
                mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                Log.d(TAG, "auto focus set");
            }

            // update the camera object parameters
            mCamera.setParameters(mCamParams);
            Log.d(TAG, "camera parameters set");

            try {
                mCamera.setPreviewDisplay(mHolder);
            }
            catch (IOException e) {
                Log.d(TAG, "set preview display threw exception: " + e.getMessage());
            }
            mCamera.startPreview();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        this.setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
        mPictureSize = getOptimalPictureSize(mSupportedPictureSizes, width, height);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }

        return optimalSize;
    }

    private Camera.Size getOptimalPictureSize(List<Camera.Size> list, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / (double) w;
        int targetHeight = 720;
        int small = Integer.MAX_VALUE;
        int diff;
        Camera.Size best = list.get(list.size() - 1);

        for (Camera.Size size : list) {
            if (Math.abs((double)size.width / (double)size.height - targetRatio) <= ASPECT_TOLERANCE) {
                diff = Math.abs(size.height - targetHeight);
                if (diff < small) {
                    small = diff;
                    best = size;
                }
            }
        }

        return best;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
