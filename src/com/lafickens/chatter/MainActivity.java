package com.lafickens.chatter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {
	SharedPreferences sharedPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		boolean isFirstTime = sharedPref.getBoolean("isFirstTime", true);
		boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);
		if (isFirstTime) {
			startActivity(new Intent(this, WelcomeActivity.class));
			finish();
		}
		if(!isLoggedIn) {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	// Change this
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			boolean succeed = ((ChatterApplication) getApplication()).getConnector().signout();
			if (succeed)
				sharedPref.edit().putBoolean("isLoggedIn", false).commit();
			else
				Toast.makeText(this, getString(R.string.con_err_try_later), Toast.LENGTH_SHORT).show();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
