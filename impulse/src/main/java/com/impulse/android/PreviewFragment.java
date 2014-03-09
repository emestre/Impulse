package com.impulse.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class PreviewFragment extends Fragment {

    private static final String TAG = "PreviewFragment";

    private ImageView mImageView;
    private Button mAcceptButton;
    private Bitmap mImageBitmap;
    private int mRotation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        mImageView = (ImageView) view.findViewById(R.id.image_preview);
        mAcceptButton = (Button) view.findViewById(R.id.accept_post_button);

        // create the picture from the static data array
        mImageBitmap = BitmapFactory.decodeByteArray(CameraActivity.imageData, 0,
                CameraActivity.imageData.length);
        mRotation = getArguments().getInt(CameraActivity.IMAGE_ROTATION_KEY);

        // rotate the image
        if (mRotation != 0) {
            // create the rotation matrix
            Matrix matrix = new Matrix();
            matrix.postRotate(mRotation);
            mImageBitmap = Bitmap.createBitmap(mImageBitmap, 0, 0, mImageBitmap.getWidth(),
                    mImageBitmap.getHeight(), matrix, false);
        }

        mImageView.setImageBitmap(mImageBitmap);
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
    }

    private void acceptButtonClick() {
        Intent intent = new Intent(getActivity().getApplicationContext(), CreatePostActivity.class);
        // store the rotation
        intent.putExtra(CameraActivity.IMAGE_ROTATION_KEY, mRotation);
        startActivity(intent);

        getActivity().getSupportFragmentManager().popBackStack();
    }
}
