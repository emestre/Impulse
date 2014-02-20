package com.impulse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.facebook.Session;

public class HomeScreen extends Activity {

	private Button profileButton, serverButton;
	private long startTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		profileButton = (Button) findViewById(R.id.profile_button);
		profileButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeScreen.this,
						ProfileActivity.class);
				startActivity(intent);
			}

		});
		
		serverButton = (Button) findViewById(R.id.server_button);
		serverButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(HomeScreen.this, "Attempting to connect to server", Toast.LENGTH_LONG);
				
				new AsyncTask<Void, Void, String>() {

					@Override
					protected String doInBackground(Void... args) {
						String notify = null;
						startTime = System.currentTimeMillis();
						try {
							
							HttpClient client = new DefaultHttpClient();
							HttpGet get = new HttpGet(
									"http://impulse-backend.appspot.com/impulse");
							HttpResponse response;
							response = client.execute(get);

							if (response.getStatusLine().getStatusCode() == 200)
								notify = "Successful connection to Server";
							else
								notify = "Unsuccessful connection to Server";
							return notify;

						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return notify;
					}

					protected void onPostExecute(String notify) {
						Toast.makeText(HomeScreen.this, notify + " in " + (System.currentTimeMillis() - startTime) + "ms", Toast.LENGTH_LONG)
								.show();
					}
				}.execute();
			}
		});
	}
	
	public void cameraButtonClick(View view) {
		Intent intent = new Intent(HomeScreen.this, CameraActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onBackPressed() {
		Session session = Session.getActiveSession();
		session.closeAndClearTokenInformation();
		finish();
	}

}
