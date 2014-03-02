package com.impulse;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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
        // empty. Take care of releasing the Camera preview in your activity.
        Log.d(TAG, "preview surface has been destroyed");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        Log.d(TAG, "preview surface changed");
        Log.d(TAG, "camera = " + mCamera + " surface = " + mHolder.getSurface());

        if (mCamera != null && mHolder.getSurface() != null) {

            mCamera.stopPreview();

            if (mPreviewSize.width > mPreviewSize.height) {
                // rotate camera 90 degrees to portrait
                mCamera.setDisplayOrientation(90);
                // rotate the image file so it's oriented correctly (portrait) when viewed in photo viewer
                mCamParams.setRotation(90);
            }

            // set the preview size to the aspect ratio calculated by getOptimalPreviewSize()
            mCamParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            this.requestLayout();
            mCamParams.setPictureSize(mPictureSize.width, mPictureSize.height);

            mCamParams.setJpegQuality(50);
            mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            // update the camera object parameters
            mCamera.setParameters(mCamParams);
            Log.d(TAG, "preview size set: width = " + mPreviewSize.width + " height = " + mPreviewSize.height);
            Log.d(TAG, "picture size: width = " + mPictureSize.width + " height = " + mPictureSize.height);

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
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
            Log.d(TAG, "on measure, optimal preview size calculated");
        }
        mPictureSize = getOptimalPreviewSize(mSupportedPictureSizes, width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            // Center the child SurfaceView within the parent.
            if (width * height > height * width) {
                final int scaledChildWidth = width * height / height;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            }
            else {
                final int scaledChildHeight = height * width / width;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }

            Log.d(TAG, "on layout changed");
        }

        Log.d(TAG, "in on layout, no change");
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
}
