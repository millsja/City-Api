package com.example.james.testapp;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        ConnectionCallbacks, OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback, LocationListener{

    private GoogleMap gMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        refreshLocation();

    }

    GoogleApiClient gClient;
    Location lastLocation;
    LocationRequest locationRequest;

    public void refreshLocation(){
            try{
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(gClient);
                    if (lastLocation != null) {
                        double lat = lastLocation.getLatitude();
                        double lon = lastLocation.getLongitude();
                        Log.i("MAP SUCCESS", "Latitude: " + String.valueOf(lat) + "\n" +
                        "Longitude: " + String.valueOf(lon) );
                        LatLng loc = new LatLng(lat, lon);
                        gMap.addMarker(new MarkerOptions().position(loc).title("Your location"));
                        gMap.moveCamera(CameraUpdateFactory.newLatLng(loc));

                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                            if (addresses == null || addresses.size()  == 0) {
                                Log.i("MAP ERROR", "Error: couldn't retrieve city name");
                            }
                            else {
                                Log.i("MAP SUCCESS", "City: " + addresses.get(0).getLocality() + "\n" +
                                        "State: " + addresses.get(0).getAdminArea());

                            }
                        } catch(IOException e) {
                            Log.i("MAP ERROR", "Error: i/o exception");
                        }
                    }
                    else{
                        Log.i("MAP ERROR", "Error: null location returned. Setting timer.");
                    }

                }
                else{
                    ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 24601);
                }
            } catch(SecurityException e){
                Log.i("MAP ERROR", "Exception: " + e.toString());
            }
    }

    public void runRefresh(View v){
            refreshLocation();

    }


    @Override
    protected void onStart() {
            super.onStart();
            // Set up Google API client
            // Reference: Android documentation
            // See https://developer.android.com/training/location/retrieve-current.html#play-services
            if (gClient == null) {
            gClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
            }
            locationRequest = null;
            gClient.connect();

            }

    @Override
    protected void onResume(){
            Log.i("GOOGLE_API", "Connecting...");
            gClient.connect();
            super.onResume();

    }

    @Override
    protected void onPause(){
            LocationServices.FusedLocationApi.removeLocationUpdates(gClient, this);
            gClient.disconnect();
            super.onPause();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
            Log.i("GOOGLE_API", "Connected...");
            refreshLocation();
            if(locationRequest == null){
                locationRequest = new LocationRequest();
                locationRequest.setInterval(10000);
                locationRequest.setFastestInterval(5000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            }
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(gClient, locationRequest, this);
            } else {
                ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 24601);
            }

    }

    @Override
    public void onConnectionSuspended(int i) {
            Log.i("GOOGLE_API", "Connection suspended...");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.i("GOOGLE_API", "Connection failed: "
            + connectionResult.toString() );

    }

    public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] grantResults){
            TextView results = (TextView) findViewById(R.id.results);
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(gClient, locationRequest, this);
                } catch (SecurityException e){
                    Log.i("MAP ERROR", "Error: security exception");
                }
                refreshLocation();
            }
            else{
                Log.i("MAP ERROR", "Error: location permissions denied");
            }
    }

    @Override
    public void onLocationChanged(Location location) {
            refreshLocation();
    }

}
