package com.lafickens.chatter;

import com.lafickens.chatter.connection.Connector;

import android.app.Application;

public class ChatterApplication extends Application {
	private Connector connector = null;
	
	public Connector getConnector() {
		return connector;
	}
	
}
