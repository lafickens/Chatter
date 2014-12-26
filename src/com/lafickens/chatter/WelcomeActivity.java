package com.lafickens.chatter;

import com.lafickens.chatter.fragments.WelcomeFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class WelcomeActivity extends FragmentActivity {
	
	ViewPager viewPager;
	int pageNum;
	
	protected class WelcomePagerAdapter extends FragmentPagerAdapter {
		public WelcomePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			Bundle args = new Bundle();
			args.putInt("page", arg0 + 1);
			Fragment fragment = new WelcomeFragment();
			fragment.setArguments(args);
			
			return fragment;
		}

		@Override
		public int getCount() {
			return pageNum;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		getActionBar().hide();
		pageNum = 1;
		
		WelcomePagerAdapter adapter = new WelcomePagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(adapter);
	}
	
	@Override
	public void onBackPressed() {
		if (pageNum != 1)
			super.onBackPressed();
		else
			finish();
	}
}
