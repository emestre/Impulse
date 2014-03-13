package com.impulse.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageFragment extends Fragment {

    private static final String TAG = "ImageFragment";

    private ImageButton mAcceptButton;
    private ImageButton mSaveButton;
    private ImageButton mCloseButton;
    private int mRotation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ImageView image = (ImageView) view.findViewById(R.id.image_preview);
        mAcceptButton = (ImageButton) view.findViewById(R.id.accept_post_button);
        mSaveButton = (ImageButton) view.findViewById(R.id.save_button);
        mCloseButton = (ImageButton) view.findViewById(R.id.close_button);

        // create the picture from the static data array
        Bitmap bitmap = BitmapFactory.decodeByteArray(CameraActivity.imageData, 0,
                CameraActivity.imageData.length);
        Log.d(TAG, "bitmap dimensions: " + bitmap.getWidth() + " x " + bitmap.getHeight());
        mRotation = getArguments().getInt(CameraActivity.IMAGE_ROTATION_KEY);
        Log.d(TAG, "image rotation: " + mRotation);

        // rotate the image
        if (mRotation != 0) {
            // create the rotation matrix
            Matrix matrix = new Matrix();
            matrix.postRotate(mRotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);
        }

        image.setImageBitmap(bitmap);
        container.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptButtonClick();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClick();
            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void acceptButtonClick() {
        Intent intent = new Intent(getActivity().getApplicationContext(), CreatePostActivity.class);
        // store the rotation
        intent.putExtra(CameraActivity.IMAGE_ROTATION_KEY, mRotation);
        startActivity(intent);

        getActivity().getSupportFragmentManager().popBackStack();
    }

    private void saveButtonClick() {
        String path = MediaFileHelper.getSdCardPath();

        if (path != null) {
            new SaveImageTask(path, new SaveImageCallback() {
                @Override
                public void onTaskComplete(boolean result, String path) {
                    if (result) {
                        String paths[] = {path};
                        String mime[] = {"image/jpeg"};
                        MediaScannerConnection.scanFile(getActivity(), paths, mime, new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                if (uri != null)
                                    Log.d(TAG, "media scanner running on image...SUCCESS");
                                else
                                    Log.d(TAG, "media scanner running on image...FAILED");
                            }
                        });

                        Toast.makeText(getActivity(), "Saved to SD card.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getActivity(), "Error saving to SD card.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).execute();
        }
        else {
            Toast.makeText(getActivity(), "Mount SD card to save.", Toast.LENGTH_SHORT).show();
        }
    }
}
