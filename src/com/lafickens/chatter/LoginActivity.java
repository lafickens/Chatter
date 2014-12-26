package com.lafickens.chatter;

import org.jivesoftware.smack.XMPPConnection;

import com.lafickens.chatter.connection.Connector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends Activity {
	
	private TextView usernameView, passwordView;
	private Button loginButton;
	
	private Connector connector;
	
	/*
	protected class LoginTask extends AsyncTask<String, String, Boolean> {
		ProgressDialog progressDialog;
		XMPPConnection smack;
		
		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(LoginActivity.this, "", getString(R.string.connecting));
			smack = connector.getSmack();
		}
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			String username = arg0[0];
			String password = arg0[1];
			
			boolean isConnected = smack.isConnected();
			if (!isConnected) {
				connector.connect();
			}
			publishProgress("Authenticating");
			boolean result = connector.login(username, password);
			
			return result;
		}
		
		@Override
		protected void onProgressUpdate(String... arg0) {
			progressDialog.setMessage(arg0[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			if (!success) {
				usernameView.setError(getString(R.string.error_invalid_credentials));
			}
		}
	}
	*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		getActionBar().hide();
		
		Connector connector = ((ChatterApplication) getApplication()).getConnector();
		if (connector == null)
			connector = new Connector(getApplicationContext());
		
		connector = ((ChatterApplication) getApplication()).getConnector();
		usernameView = (TextView) findViewById(R.id.activity_login_username);
		passwordView = (TextView) findViewById(R.id.activity_login_password);
		loginButton = (Button) findViewById(R.id.activity_login_button);
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String username = usernameView.getText().toString();
				String password = passwordView.getText().toString();
				if (username == "")
					usernameView.setError(getString(R.string.error_field_required));
				else if (password == "")
					passwordView.setError(getString(R.string.error_field_required));
				else {
					boolean success = login(username, password);
					if (!success)
						usernameView.setError(getString(R.string.error_invalid_credentials));
					else {
						SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putBoolean("isLoggedIn", true);
						editor.putString("username", username);
						editor.putString("password", password);
						editor.commit();
						startActivity(new Intent(LoginActivity.this, MainActivity.class));
					}
				}
			}
		});
	}
	private boolean login(String username, String password) {
		ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "", getString(R.string.connecting));
		XMPPConnection smack = connector.getSmack();
		
		boolean isConnected = smack.isConnected();
		if (!isConnected) {
			connector.connect();
		}
		progressDialog.setMessage(getString(R.string.authenticating));
		boolean result = connector.login(username, password);
		
		return result;
	}
}
