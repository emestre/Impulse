package com.impulse.impulse;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Eliot on 3/11/14.
 */
public class FullScreenActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_fullscreen);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String filePath = extras.getString("FILE_PATH");
            ImageView image = (ImageView) findViewById(R.id.fullscreen_image);
            image.setBackgroundColor(Color.BLACK);
            Picasso.with(getApplicationContext())
                    .load(RestClient.getFile(filePath, "full", false))
                    .into(image);
        }
        else
            finish();
    }


}
