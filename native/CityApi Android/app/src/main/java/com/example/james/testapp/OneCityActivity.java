package com.example.james.testapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OneCityActivity extends AppCompatActivity {

    // these will hold old values of these variables for when
    // we submit changes
    String oldname, oldpopulation, oldcountry, oldcategory;
    String city_id;


    // send a get request to get the delete current city
    public void sendDelete( final String city_id,
                            final Context context,
                            final TextView result ){

        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/user/city/" + city_id;

        // Request a string response from the provided URL.
        StringRequest stringRequest  = new StringRequest(Request.Method.DELETE, addr,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try{
                            result.setText("");
                            Intent intent = new Intent(context, ViewCitiesActivity.class);
                            startActivity( intent );

                        } catch( Exception e ){
                            result.setTextColor(Color.RED);
                            Log.i("DELETE", e.toString());
                            result.setText(("That didn't work!"));
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                result.setTextColor(Color.RED);
                Log.i("DELETE", error.toString());
                result.setText("Error: " + error.toString());
            }

        }) {

            // send our cookie along with the volley request
            @Override
            public Map<String, String> getHeaders(){
                AndroidApplication app = (AndroidApplication)getApplication();
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json" );
                Log.i("PROFILE: ", "session-cookie=" + app.getCookie());
                headers.put("Cookie", "session-cookie=" + app.getCookie() );
                return headers;
            }
        };

        queue.add(stringRequest);
    }

    // send a get request to get the current city based on id
    public void getOneCity(final String id,
                           final TextView name,
                           final TextView country,
                           final TextView population,
                           final TextView category,
                           final TextView result ){

        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/user/city/" + id;

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest  = new JsonObjectRequest(Request.Method.GET, addr, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try{
                            result.setText("");
                            name.setText( response.getString("name") );
                            country.setText( response.getString("country") );
                            population.setText( response.getString("population") );
                            category.setText( response.getString("category") );
                            oldname = name.toString();
                            oldcountry = country.toString();
                            oldpopulation = population.toString();
                            oldcategory = category.toString();
                        } catch( Exception e ){
                            result.setTextColor(Color.RED);
                            result.setText(("Exception occured: " + e.toString() ));
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                result.setTextColor(Color.RED);
                result.setText("That didn't work!");
            }

        }) {

            // send our cookie along with the volley request
            @Override
            public Map<String, String> getHeaders(){
                AndroidApplication app = (AndroidApplication)getApplication();
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json" );
                Log.i("PROFILE: ", "session-cookie=" + app.getCookie());
                headers.put("Cookie", "session-cookie=" + app.getCookie() );
                return headers;
            }
        };

        queue.add(jsonRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.city_id = getIntent().getExtras().getString("city_id");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_city);
    }


    // make put request to submit user profile changes
    public void sendChanges(final String city_id,
                            final String name,
                            final String country,
                            final String population,
                            final String category,
                            final TextView results){
        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/user/city/" + city_id;

        final JSONObject jsonRequest = new JSONObject();
        try {
            if(!name.equals(oldname)) jsonRequest.put("name", name);
            if(!country.equals(oldcountry)) jsonRequest.put("country", country);
            if(!population.equals(oldpopulation)) jsonRequest.put("population", population);
            if(!category.equals(oldcategory)) jsonRequest.put("category", category);

        } catch (JSONException e){
            results.setText("Error: " + e);
            return;
        }

        // Request a string response from the provided URL.
        StringRequest stringRequest  = new StringRequest(Request.Method.PUT, addr,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        results.setTextColor(Color.GREEN);
                        results.setText( "Profile updated successfully!" );

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

    // parse changes to the city form and send to api for update
    public void submitChanges(View v){
        String name = ((EditText)findViewById(R.id.name)).getText().toString();
        String country = ((EditText)findViewById(R.id.country)).getText().toString();
        String population = ((EditText)findViewById(R.id.population)).getText().toString();
        String category = ((EditText)findViewById(R.id.category)).getText().toString();
        TextView results = (TextView) findViewById(R.id.results);
        if( name != null && !name.equals("") &&
                country != null && !country.equals("") &&
                population != null && !population.equals("") &&
                category != null && !category.equals("") ){
            sendChanges(this.city_id, name, country, population, category, results);
        }
        else{
            results.setText("Error: All fields required");
        }

    }

    // send our delete order to the city api
    public void submitDelete(View v){
        TextView results = (TextView) findViewById(R.id.results);
        sendDelete(this.city_id, this, results);

    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, ViewCitiesActivity.class);
        startActivity( intent );
        return;
    }

    // on resume: clear the list of cities and build a new one, a button for each
    @Override
    protected void onResume() {
        TextView name = (TextView) findViewById(R.id.name);
        TextView country = (TextView) findViewById(R.id.country);
        TextView population = (TextView) findViewById(R.id.population);
        TextView category = (TextView) findViewById(R.id.category);
        TextView result = (TextView) findViewById(R.id.results);
        getOneCity( this.city_id,
                name, country, population, category, result );
        super.onResume();
    }
}
