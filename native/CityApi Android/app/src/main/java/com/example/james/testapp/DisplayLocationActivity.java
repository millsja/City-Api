package com.example.james.testapp;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class DisplayLocationActivity extends AppCompatActivity implements
    ConnectionCallbacks, OnConnectionFailedListener,
    ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    String country, name;

    GoogleApiClient gClient;

    static final String GMAPS_KEY = "AIzaSyBNLFyk6JtufB39zKZOu1z9NagEEpoI5jk";
    Location lastLocation;
    LocationRequest locationRequest;

    public void refreshLocation(){
        TextView results = (TextView) findViewById(R.id.results);
        try{
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                lastLocation = LocationServices.FusedLocationApi.getLastLocation(gClient);
                if (lastLocation != null) {
                    double lat = lastLocation.getLatitude();
                    double lon = lastLocation.getLongitude();
                    results.setText("Latitude: " + String.valueOf(lat) + "\n" +
                            "Longitude: " + String.valueOf(lon) );
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                        if (addresses == null || addresses.size()  == 0) {
                            results.setText("Error: couldn't retrieve city name");
                        }
                        else {
                            this.name = addresses.get(0).getLocality();
                            this.country = addresses.get(0).getCountryName();
                            results.setText("City: " + this.name + "\n" +
                                            "Country: " + this.country);

                        }
                    } catch(IOException e) {
                        results.setText("Error: i/o exception");
                    }
                }
                else{
                    results.setText("Error: null location returned. Setting timer.");
                }

            }
            else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 24601);
            }

        } catch(SecurityException e){
            results.setText("Exception: " + e.toString());
        }
    }

    // make put request to submit user profile changes
    public void sendAddCity(final Context context,
                            final String name,
                            final String country,
                            final String population,
                            final String category,
                            final TextView results){
        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/user/city";

        final JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("name", name);
            jsonRequest.put("country", country);
            jsonRequest.put("population", population);
            jsonRequest.put("category", category);

        } catch (JSONException e){
            results.setText("Error: " + e);
            return;
        }

        // Request a string response from the provided URL.
        StringRequest stringRequest  = new StringRequest(Request.Method.POST, addr,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        results.setTextColor(Color.GREEN);
                        results.setText( "City added successfully!" );
                        Intent intent = new Intent(context, ViewCitiesActivity.class);
                        startActivity( intent );
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                results.setTextColor(Color.RED);
                results.setText("Error: " + error.toString());
            }
        }){
            @Override
            public byte[] getBody(){
                return jsonRequest.toString().getBytes();
            }

            @Override
            public String getBodyContentType(){
                return "application/json";
            }

            // send our cookie along with the volley request
            @Override
            public Map<String, String> getHeaders(){
                AndroidApplication app = (AndroidApplication)getApplication();
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json" );
                headers.put("Cookie", "session-cookie=" + app.getCookie() );
                return headers;
            }
        };

        // add our post request to the volley queue
        queue.add(stringRequest);

    }

    // parse the add city form and send to api for update
    public void submitAddCity(View v){
        TextView results = (TextView) findViewById(R.id.results);
        String population = "0";
        String category = "NA";
        if( this.name != null && !this.name.equals("") &&
                this.country != null && !this.country.equals("") ){
            sendAddCity(this, name, country, population, category, results);
        }
        else{
            results.setText("Error: All fields required");
        }

    }

    public void runRefresh(View v){
        refreshLocation();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_location);
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
        LocationServices.FusedLocationApi.removeLocationUpdates(
                gClient, this);
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
                results.setText("Error: security exception");
            }
            refreshLocation();
        }
        else{
            results.setText("Error: location permissions denied");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        refreshLocation();
    }
}
