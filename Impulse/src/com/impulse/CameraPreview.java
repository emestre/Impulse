package com.impulse;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private SurfaceHolder mHolder;

    private Camera mCamera;
    private Camera.Parameters mCamParams;
    private int mCameraId;

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

    public void setCamera(Camera camera, int id) {
        mCamera = camera;
        mCameraId = id;
        if (camera != null) {
            mCamParams = mCamera.getParameters();
            mSupportedPreviewSizes = mCamParams.getSupportedPreviewSizes();
            mSupportedPictureSizes = mCamParams.getSupportedPictureSizes();
            this.requestLayout();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "received touch event");

            // focus the camera, no callback
            mCamera.autoFocus(null);
        }

        return true;
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
        initSurface();
    }

    public void initSurface() {
        if (mCamera != null && mHolder.getSurface() != null) {

            mCamera.stopPreview();

            if (mPreviewSize.width > mPreviewSize.height) {
                // rotate camera 90 degrees to portrait
                mCamera.setDisplayOrientation(90);
                Log.d(TAG, "display orientation set to: 90");
            }
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            // rotate the image file so it's oriented correctly when viewed in photo viewer
            mCamParams.setRotation(info.orientation);
            Log.d(TAG, "rotation set to: " + info.orientation);

            // set the preview size to the aspect ratio calculated by getOptimalPreviewSize()
            mCamParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//            Log.d(TAG, "preview size set to: " + mPreviewSize.width + " x " + mPreviewSize.height);
            this.requestLayout();

            mPictureSize = getOptimalPictureSize(mSupportedPictureSizes);
            mCamParams.setPictureSize(mPictureSize.width, mPictureSize.height);
            Log.d(TAG, "picture size set to: " + mPictureSize.width + " x " + mPictureSize.height);

            mCamParams.setJpegQuality(50);
            mCamParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            // update the camera object parameters
            mCamera.setParameters(mCamParams);
            Log.d(TAG, "camera parameters set");

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
        }
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

    private Camera.Size getOptimalPictureSize(List<Camera.Size> list) {
        final double TARGET_RATIO = 4.0 / 3.0;
        final int TARGET_WIDTH = 900;
        int diff = Integer.MAX_VALUE;
        Camera.Size best = list.get(list.size() - 1);

        // get all the sizes that have a 4:3 ratio
        // then get the size that has height closest to 900px
        for (Camera.Size size : list) {
            if ((double)size.width / (double)size.height == TARGET_RATIO) {

                int temp = Math.abs(size.height - TARGET_WIDTH);
                if (temp < diff) {
                    diff = temp;
                    best = size;
                }
            }
        }

        return best;
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
        else {
            Log.d(TAG, "in on layout no change");
        }
    }
}
