package com.lafickens.chatter.fragments;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lafickens.chatter.ChatActivity;
import com.lafickens.chatter.R;
import com.lafickens.chatter.connection.VolleyConnector;
import com.lafickens.chatter.connection.VolleyConnector.VolleyErrorListener;
import com.lafickens.chatter.connection.VolleyConnector.VolleyResponseListener;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment implements OnMapReadyCallback, ConnectionCallbacks, 
												OnConnectionFailedListener, LocationListener {
	private MapFragment mapFragment;
	private GoogleMap googleMap;
	private GoogleApiClient googleApiClient;
	private Location location;
	private VolleyConnector volleyConnector;
	private ScheduledExecutorService executor;
	
	private boolean requestingLocationUpdates = false;
	private boolean needReupdate = false;
	private boolean needReconnect = false;
	private String userid;
	
	protected class ChatterInfoWindowAdapter implements InfoWindowAdapter {
		@Override
		public View getInfoContents(Marker arg0) {
			return null;
		}

		@Override
		public View getInfoWindow(Marker arg0) {
			View infoWindow = getActivity().getLayoutInflater().inflate(R.layout.info_window, null, false);
			TextView titleView = (TextView) infoWindow.findViewById(R.id.info_window_textView);
			titleView.setText(arg0.getTitle());
			titleView.setAlpha(0.7f);
			return infoWindow;
		}
	}
	
	protected class ChatterOnInfoWindowClickListener implements OnInfoWindowClickListener {
		@Override
		public void onInfoWindowClick(Marker arg0) {
			Intent intent = new Intent(getActivity(), ChatActivity.class);
			intent.putExtra("userid", arg0.getTitle());
			startActivity(intent);
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		
		buildGoogleApiClient();
		mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		googleMap = mapFragment.getMap();
		volleyConnector = new VolleyConnector(getActivity());
		userid = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).getString("userid", "");
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (location != null) {
					updateLocation(userid, location.getLatitude(), location.getLongitude());
					refreshNearbyPois(location.getLatitude(), location.getLongitude());
				}
			}
		}, 0, 20, TimeUnit.SECONDS);
		
		googleMap.setInfoWindowAdapter(new ChatterInfoWindowAdapter());
		googleMap.setOnInfoWindowClickListener(new ChatterOnInfoWindowClickListener());
		
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (googleApiClient.isConnected() && !requestingLocationUpdates)
			startLocationUpdates();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		stopLocationUpdates();
	}

	@Override
	public void onMapReady(GoogleMap arg0) {
		Log.i("Chatter", "Map Loaded");
	}
	
	private synchronized void buildGoogleApiClient() {
	    googleApiClient = new GoogleApiClient.Builder(getActivity())
	        .addConnectionCallbacks(this)
	        .addOnConnectionFailedListener(this)
	        .addApi(LocationServices.API)
	        .build();
	}

	@Override
	public void onConnected(Bundle arg0) {
		needReconnect = false;
		needReupdate = false;
		location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
		if (location != null)
			setLocationOnMap();
		if (!requestingLocationUpdates)
			startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.i("Chatter", "Connection suspended");
		Toast.makeText(getActivity(), getString(R.string.fragment_main_google_play_error), Toast.LENGTH_LONG).show();
		
		needReconnect = true;
		while (needReconnect) {
			buildGoogleApiClient();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.i("Chatter", "Connection failed");
		Toast.makeText(getActivity(), getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
		
		needReconnect = true;
		while (needReconnect) {
			buildGoogleApiClient();
		}
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		location = arg0;
		setLocationOnMap();
		
	}
	
	private void startLocationUpdates() {
		LocationRequest locationRequest = new LocationRequest();
		locationRequest.setInterval(20000);
		locationRequest.setFastestInterval(500);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
		requestingLocationUpdates = true;
		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
	}
	
	private void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
		requestingLocationUpdates = false;
	}
	
	private void setLocationOnMap() {
		googleMap.addMarker(new MarkerOptions()
							.title("location")
							.position(new LatLng(location.getLatitude(), location.getLongitude()))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker)));
	}
	
	private void refreshNearbyPois(double latitude, double longitude) {
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
						JSONArray locationArr = arg0.getJSONArray("location");
						for (int i = 0; i < locationArr.length(); i++) {
							JSONObject tempObject = locationArr.getJSONObject(i);
							String userid = tempObject.getString("userid");
							JSONArray tempArr = tempObject.getJSONArray("coordinates");
							googleMap.addMarker(new MarkerOptions()
												.title(userid)
												.position(new LatLng(tempArr.getDouble(0), tempArr.getDouble(1)))
												.icon(BitmapDescriptorFactory.defaultMarker()));
						}
					} catch (JSONException e) {
						Log.i("Chatter", "JSON conversion error");
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
	
	private void updateLocation(String userid, double latitude, double longitude) {	
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
					needReupdate = true;
				}	
			});
		} catch (JSONException e) {
			Log.i("Chatter", "JSON conversion error");
			e.printStackTrace();
		}
	}
}
