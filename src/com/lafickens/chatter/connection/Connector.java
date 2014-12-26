package com.lafickens.chatter.connection;

import java.io.IOException;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import android.content.Context;

public class Connector {
	private Context context;
	private XMPPConnection smack;
	
	public Connector(Context arg0) {
		context = arg0;
		SmackAndroid.init(context);
		ConnectionConfiguration config = new ConnectionConfiguration("54.201.2.40");
		config.setDebuggerEnabled(true);
		smack = new XMPPTCPConnection(config);
	}
	
	public synchronized boolean connect() {
		boolean result = false;
		try {
			smack.connect();
			if (smack.isConnected())
				result = true;
		} catch (SmackException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean login(final String username, final String password) {
		Thread loginThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					smack.login(username, password);
				} catch (SaslException e) {
					e.printStackTrace();
				} catch (XMPPException e) {
					e.printStackTrace();
				} catch (SmackException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		loginThread.start();
		try {
			loginThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return smack.isAuthenticated();
	}
	
	public boolean signout() {
		boolean result = false;
		try {
			smack.disconnect();
			result = true;
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public XMPPConnection getSmack() {
		return smack;
	}
}
