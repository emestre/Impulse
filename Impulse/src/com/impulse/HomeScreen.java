package com.impulse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;

public class HomeScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		try {
			URL url = new URL("http://impulse-backend.appspot.com/impulse/");
			new AsyncTask<Void, Void, String>() {

				@Override
				protected String doInBackground(Void... args) {
					String notify = null;
					//Toast.makeText(HomeScreen.this, "Starting server connection", Toast.LENGTH_LONG).show();
					
					try {
						HttpClient client = new DefaultHttpClient();
						HttpGet get = new HttpGet("http://impulse-backend.appspot.com/impulse");
						HttpResponse response;
						response = client.execute(get);
						
						if (response.getStatusLine().getStatusCode() == 200)
							notify = "Successfull connection to Server";
						else
							notify = "Unsuccessfull connection to Server";
						return notify;
						
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return notify;
				}
			
				protected void onPostExecute(String notify) {
					Toast.makeText(HomeScreen.this, notify, Toast.LENGTH_LONG).show();
				}
			}.execute();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
