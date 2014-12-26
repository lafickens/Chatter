package com.lafickens.chatter.fragments;

import com.lafickens.chatter.MainActivity;
import com.lafickens.chatter.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class WelcomeFragment extends Fragment {
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle args = getArguments();
		int pageNum = args.getInt("page");
		View rootView = preparePage(pageNum, inflater, container);
		
		return rootView;
	}
	
	private View preparePage(int page, LayoutInflater inflater, ViewGroup container) {
		View rootView;
		switch (page) {
		case 1:
			rootView = inflater.inflate(R.layout.fragment_welcome_page1, container, false);
		case 2:
			rootView = inflater.inflate(R.layout.fragment_welcome_page2, container, false);
			Button button = (Button) getActivity().findViewById(R.id.fragment_welcome_page2_login_button);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
					sharedPref.edit().putBoolean("isFirstTime", false).commit();
					startActivity(new Intent(getActivity(), MainActivity.class));
					getActivity().finish();
				}
			});
		default:
			rootView = inflater.inflate(R.layout.fragment_welcome_page1, container, false);
		}
		return rootView;
	}
}
