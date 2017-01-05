package com.example.james.testapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddCityActivity extends AppCompatActivity {

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
        String name = ((EditText)findViewById(R.id.name)).getText().toString();
        String country = ((EditText)findViewById(R.id.country)).getText().toString();
        String population = ((EditText)findViewById(R.id.population)).getText().toString();
        String category = ((EditText)findViewById(R.id.category)).getText().toString();
        TextView results = (TextView) findViewById(R.id.results);
        if( name != null && !name.equals("") &&
                country != null && !country.equals("") &&
                population != null && !population.equals("") &&
                category != null && !category.equals("") ){
            sendAddCity(this, name, country, population, category, results);
        }
        else{
            results.setText("Error: All fields required");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainCitiesActivity.class);
        startActivity( intent );
        return;
    }
}
