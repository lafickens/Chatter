package com.lafickens.chatter.connection;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.lafickens.chatter.R;
import com.lafickens.chatter.connection.VolleyConnector.VolleyErrorListener;
import com.lafickens.chatter.connection.VolleyConnector.VolleyResponseListener;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ChatterClient {
	private Context context;
	private VolleyConnector volleyConnector;
	private Map<String, double[]> map = null;
	private boolean needReupdate = false;
	
	public ChatterClient(Context arg0) {
		context = arg0;
		volleyConnector = new VolleyConnector(context);
	}
	
	public void refreshNearbyPois(double latitude, double longitude) {
		map = null;
		
		try {
			String jsonString = new JSONStringer().object()
					.key("longitude")
					.value(longitude)
					.key("latitude")
					.value(latitude)
					.endObject().toString();
			JSONObject jsonObject = new JSONObject(jsonString);
			
			volleyConnector.sendRequest(R.string.subbranch_nearby, jsonObject, new VolleyResponseListener() {
				@Override
				public void onVolleyResponse(JSONObject arg0) {
					try {
						map = new HashMap<String, double[]>();
						JSONArray locationArray = arg0.getJSONArray("location");
						for (int i = 0; i < locationArray.length(); i++) {
							JSONObject tempObject = locationArray.getJSONObject(i);
							String userID = tempObject.getString("userid");
							JSONArray tempArray = tempObject.getJSONArray("coordinates");
							double[] coordinates = {tempArray.getDouble(0), tempArray.getDouble(1)};
							map.put(userID, coordinates);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}, 
			new VolleyErrorListener() {
				@Override
				public void onVolleyError() {
					Log.i("Chatter", "Fetch nearby poi failed");
				}	
			});
		} catch (JSONException e) {
			Log.i("Chatter", "JSON conversion error");
			e.printStackTrace();
		}
	}
	
	public void updateLocation(String userid, double latitude, double longitude) {	
		needReupdate = true;
		try {
			String jsonString = new JSONStringer().object()
					.key("userid")
					.value(userid)
					.key("longitude")
					.value(longitude)
					.key("latitude")
					.value(latitude)
					.endObject().toString();
			JSONObject jsonObject = new JSONObject(jsonString);
			
			volleyConnector.sendRequest(R.string.subbranch_update, jsonObject, new VolleyResponseListener() {
				@Override
				public void onVolleyResponse(JSONObject arg0) {
					needReupdate = false;
				}
			}, 
			new VolleyErrorListener() {
				@Override
				public void onVolleyError() {
					Log.i("Chatter", "Update location failed");
				}	
			});
		} catch (JSONException e) {
			Log.i("Chatter", "JSON conversion error");
			e.printStackTrace();
		}
	}
	
	
	
}
