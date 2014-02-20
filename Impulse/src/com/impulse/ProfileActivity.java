package com.impulse;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

public class ProfileActivity extends Activity {

	private TextView userName;
	private ProfilePictureView profPic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		Session session = Session.getActiveSession();
        userName = (TextView) findViewById(R.id.user_name);
        profPic = (ProfilePictureView) findViewById(R.id.profile_picture);
        
		Request.newMeRequest(session, new Request.GraphUserCallback() {

        // callback after Graph API response with user object
        @Override
        public void onCompleted(GraphUser user, Response response) {
          if (user != null) {
            userName.setVisibility(View.VISIBLE);
            profPic.setVisibility(View.VISIBLE);
            userName.setText(user.getName());
            profPic.setProfileId(user.getId());
          }
        }
      }).executeAsync(); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

}
