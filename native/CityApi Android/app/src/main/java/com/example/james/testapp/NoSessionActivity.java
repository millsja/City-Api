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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NoSessionActivity extends AppCompatActivity {

    public void attemptLogin(final Context context,
                           final String email,
                           final String passwd,
                           final TextView results ){
        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/session";

        final JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("email", email);
            jsonRequest.put("passwd", passwd);

        } catch (JSONException e){
            results.setTextColor(Color.RED);
            results.setText("Error: " + e.toString());

            return;
        }

        // Request a string response from the provided URL.
        StringRequest stringRequest  = new StringRequest(Request.Method.POST, addr,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        results.setTextColor(Color.GREEN);
                            results.setText("Successfully logged in...");
                            // check to see whether the cookie's been set. if so, redirect to main
                            AndroidApplication app = (AndroidApplication)getApplication();
                            if(app.getCookie() != null){
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity( intent );
                            }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                results.setTextColor(Color.RED);
                if( error instanceof ServerError){
                    results.setText("Error: check your credentials");
                }
                else{
                    results.setText("Error: " + error.toString());
                }
            }

        }){

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                AndroidApplication app = (AndroidApplication)getApplication();
                String sessionCookie = response.headers.get("Set-Cookie");
                sessionCookie = sessionCookie.replace("session-cookie=","");
                app.setCookie(sessionCookie);
                return super.parseNetworkResponse(response);
            }

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

    public void gotoRegister(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void submitLogin(View v){
        String email = ((EditText)findViewById(R.id.email)).getText().toString();
        String passwd = ((EditText)findViewById(R.id.passwd)).getText().toString();
        TextView results = (TextView) findViewById(R.id.results);

        // make sure our credential fields are filled in
        if( email != null && !email.equals("") &&
                passwd != null && !passwd.equals("") ){
            // use credentials to attempt to log in
            attemptLogin(this, email, passwd, results);

        }
        else{
            results.setText("Error: All fields required");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("NOSESSION: ", "No session activity starting...\n");
        setContentView(R.layout.activity_no_session);
        Log.i("NOSESSION: ", "No session activity started...\n");

    }

}
