package com.lafickens.chatter.connection;

import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lafickens.chatter.R;

import android.content.Context;
import android.util.Log;

public class VolleyConnector {
	private Context context;
	private RequestQueue requestQueue;
	
	private String serverAddress;
	
	public static interface VolleyResponseListener {
		public void onVolleyResponse(JSONObject arg0);
	}
	
	public static interface VolleyErrorListener {
		public void onVolleyError();
	}
	
	public VolleyConnector(Context arg0) {
		context = arg0;
		requestQueue = Volley.newRequestQueue(context);
		serverAddress = context.getString(R.string.server_address);
	}
	
	public void sendRequest(int subbrachResId, JSONObject jsonObject, 
			final VolleyResponseListener responseListener, final VolleyErrorListener errorListener) {
		String fullAddress = serverAddress + context.getString(subbrachResId);
		JsonObjectRequest request = new JsonObjectRequest(Method.POST, fullAddress, jsonObject, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject arg0) {
				responseListener.onVolleyResponse(arg0);
			}
		}, 
		new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.i("Chatter", arg0.getMessage());
				errorListener.onVolleyError();
			}
		});
		requestQueue.add(request);
	}
	
}
