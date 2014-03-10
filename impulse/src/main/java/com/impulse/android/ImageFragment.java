package com.impulse.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class ImageFragment extends Fragment {

    private static final String TAG = "ImageFragment";

    private Button mAcceptButton;
    private int mRotation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ImageView image = (ImageView) view.findViewById(R.id.image_preview);
        mAcceptButton = (Button) view.findViewById(R.id.accept_post_button);

        // create the picture from the static data array
        Bitmap bitmap = BitmapFactory.decodeByteArray(CameraActivity.imageData, 0,
                CameraActivity.imageData.length);
        mRotation = getArguments().getInt(CameraActivity.IMAGE_ROTATION_KEY);

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
    }

    private void acceptButtonClick() {
        Intent intent = new Intent(getActivity().getApplicationContext(), CreatePostActivity.class);
        // store the rotation
        intent.putExtra(CameraActivity.IMAGE_ROTATION_KEY, mRotation);
        startActivity(intent);

        getActivity().getSupportFragmentManager().popBackStack();
    }
}
