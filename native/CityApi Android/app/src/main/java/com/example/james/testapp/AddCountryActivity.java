package com.example.james.testapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class AddCountryActivity extends AppCompatActivity {

    public void addCountry(final String name,
                           final String pop,
                           final String area,
                           final TextView results){
        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169";

        final JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("name", name);
            jsonRequest.put("population", pop);
            jsonRequest.put("area", area);

        } catch (JSONException e){
            results.setText("Error: " + e);
            return;
        }

        // Request a string response from the provided URL.
        StringRequest stringRequest  = new StringRequest(Request.Method.POST, addr,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                            results.setText( name + " successfully added!" );

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
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

                    @Override
                    public HashMap<String, String> getHeaders(){
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Accept", "application/json");
                        return headers;
                    }
                };

        // add our post request to the volley queue
        queue.add(stringRequest);

    }

    public void submitAddCountry(View v){
        String countryName = ((EditText)findViewById(R.id.countryName)).getText().toString();
        String countryArea = ((EditText)findViewById(R.id.areaField)).getText().toString();
        String countryPop = ((EditText)findViewById(R.id.popField)).getText().toString();
        TextView results = (TextView) findViewById(R.id.results);
        if( countryName != null && !countryName.equals("") &&
                countryArea != null && !countryArea.equals("") &&
                countryPop != null && !countryPop.equals("") ){
            addCountry(countryName, countryPop, countryArea, results);
        }
        else{
            results.setText("Error: All fields required");
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_country);
    }
}
