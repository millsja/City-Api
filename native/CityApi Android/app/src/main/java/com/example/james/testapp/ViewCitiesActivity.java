package com.example.james.testapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewCitiesActivity extends AppCompatActivity {

    // add a single button using city object's name and id
    public void addOneButton( final Context context,
                              final JSONObject city,
                            final LinearLayout layout ){
        Button newButton = new Button(context);
        try {
            newButton.setText(city.get("name").toString());
            final String city_id = city.get("id").toString();
            newButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    gotoViewCity(v, city_id);
                }
            });
            layout.addView(newButton);
        } catch(JSONException exception){
            Log.i("CITIES", "JSON exception - " + exception.toString());
        }
    }

    // take the json response array and add a button for each object
    public void addButtons( final Context context,
                            final JSONArray cities,
                            final LinearLayout layout ){
        for( int q = 0; q < cities.length(); q++ ){
            try {
                addOneButton(context, (JSONObject)cities.get(q), layout);
            } catch(JSONException exception){
                Log.i("CITIES", "JSON exception - " + exception.toString());
            }
        }

    }

    // send a get request to get the current user's list of cities
    // passing each one to a button generator to link to each individual
    // city's view/edit page
    public void getCities( final Context context,
                           final TextView result,
                           final LinearLayout layout ){

        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/user/city";

        // Request a string response from the provided URL.
        JsonArrayRequest jsonRequest  = new JsonArrayRequest(Request.Method.GET, addr, null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try{

                            if(response.length() == 0){
                                result.setText("Looks like there's nothing here." +
                                        " Add some cities to get started!");
                            } else{
                                result.setText("");
                                addButtons(context, response, layout);
                            }
                        } catch( Exception e ){
                            result.setTextColor(Color.RED);
                            result.setText(("Exception occured: " + e.toString() ));
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                result.setTextColor(Color.RED);
                Log.i("CITIES", error.toString());
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
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainCitiesActivity.class);
        startActivity( intent );
        return;
    }

    // clear the buttons that are on the page. credit to stack exchange
    // question 13834391 for solution to iterating over buttons
    // on activity page
    public void clearCities( LinearLayout layout ){
        View view;
        for(int q = 0; q < layout.getChildCount(); q++ ){
            view = layout.getChildAt(q);
            if(view instanceof Button ){
                view.setVisibility(View.GONE);
            }
        }

    }

    // populate the page with cities on resume
    @Override
    protected void onResume() {
        TextView result = (TextView) findViewById(R.id.results);
        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_view_cities);
        clearCities( layout );
        getCities( this, result, layout );
        super.onResume();
    }

    public void gotoViewCity(View v, String city_id){
        Log.i("viewcities", "city id - " + city_id);
        Intent intent = new Intent(this, OneCityActivity.class);
        intent.putExtra("city_id", city_id);
        startActivity( intent );

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cities);
    }
}
