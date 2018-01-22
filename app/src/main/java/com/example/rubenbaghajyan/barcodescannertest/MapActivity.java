package com.example.rubenbaghajyan.barcodescannertest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
		GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


	public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
	public static final int LOCATION_UPDATE_MIN_TIME = 5000;

	public static final float DEFAULT_ZOOM = 15;
	static final double DEFAULT_LAT = -1;
	static final double DEFAULT_LONG = -1;
	private static final int MY_LOCATION_REQUEST_CODE = 123;
	private GoogleMap mMap;
	double latitude = DEFAULT_LAT;
	double longitude = DEFAULT_LONG;
	List<Marker> markers;
	LocationManager mLocationManager;
	Location currentLocation;
	View directionBtn;
	GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;


	private LocationListener mLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				mLocationManager.removeUpdates(mLocationListener);
				currentLocation = location;
			}
		}

		@Override
		public void onStatusChanged(String s, int i, Bundle bundle) {

		}

		@Override
		public void onProviderEnabled(String s) {

		}

		@Override
		public void onProviderDisabled(String s) {

		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		markers = new ArrayList<>();

		directionBtn = findViewById(R.id.button_direction);
		directionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Object[] dataTransfer = new Object[3];
				String url = getDirectionsUrl(currentLocation.getLatitude(), currentLocation.getLongitude(), latitude, longitude);
				GetDirectionData getDirectionsData = new GetDirectionData();
				dataTransfer[0] = mMap;
				dataTransfer[1] = url;
				dataTransfer[2] = new LatLng(latitude, longitude);
				getDirectionsData.execute(dataTransfer);
			}
		});

	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		mMap.setOnMyLocationButtonClickListener(this);

		// Add a marker in Sydney, Australia, and move the camera.

		if (getIntent() != null) {
			latitude = getIntent().getDoubleExtra("latitude", DEFAULT_LAT);
			longitude = getIntent().getDoubleExtra("longitude", DEFAULT_LONG);
		}

		if (latitude < 0 || longitude < 0) {
			Toast.makeText(this, "incorrect location", Toast.LENGTH_SHORT).show();
			finish();
		}

		LatLng location = new LatLng(latitude, longitude);

		String title = getIntent().getStringExtra("marker_title");

		Marker targetMarker = mMap.addMarker(new MarkerOptions().position(location).title(title));
		targetMarker.showInfoWindow();
		markers.add(targetMarker);

		CameraUpdate center =
				CameraUpdateFactory.newLatLng(location);
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM);

		mMap.moveCamera(center);
		mMap.animateCamera(zoom);

		checkLocationPermission();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

		if (requestCode == MY_LOCATION_REQUEST_CODE && grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			checkLocationPermission();
		}
	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		mGoogleApiClient.connect();
	}

	private void checkLocationPermission() {
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
			return;
		}

		mMap.setMyLocationEnabled(true);
		boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (isGPSEnabled) {
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
			currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}

		if (isNetworkEnabled && currentLocation == null) {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
			currentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		if (currentLocation != null) {
			mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
				@Override
				public void onMapLoaded() {
					Marker currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title("Me"));
					currentMarker.showInfoWindow();
					markers.add(currentMarker);

					LatLngBounds.Builder builder = new LatLngBounds.Builder();
					for (Marker marker : markers) {
						builder.include(marker.getPosition());
					}
					LatLngBounds bounds = builder.build();

					int padding = 200; // offset from edges of the map in pixels
					CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
					mMap.moveCamera(cu);
					mMap.animateCamera(cu);

					directionBtn.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	public boolean onMyLocationButtonClick() {
		checkLocationPermission();
		return true;
	}


	private String getDirectionsUrl(double latitude, double longitude, double end_latitude, double end_longitude) {
		return "https://maps.googleapis.com/maps/api/directions/json?"
				+ "origin=" + latitude + "," + longitude
				+ "&destination=" + end_latitude + "," + end_longitude
				+ "&sensor=false";
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(1000);
		mLocationRequest.setFastestInterval(1000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}
}

