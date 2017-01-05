package com.example.james.testapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    public String oldfname;
    public String oldlname;
    public String oldemail;
    public String oldpasswd;


    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity( intent );
        return;
    }


    // send a get request to get the current user's profile
    public void getProfile(final TextView fname,
                             final TextView lname,
                             final TextView passwd,
                             final TextView email,
                             final TextView result ){

        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/user/profile";

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest  = new JsonObjectRequest(Request.Method.GET, addr, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try{
                            result.setText("");
                            fname.setText( response.getString("fname") );
                            lname.setText( response.getString("lname") );
                            email.setText( response.getString("email") );
                            passwd.setText( response.getString("passwd") );
                            oldfname = fname.toString();
                            oldlname = lname.toString();
                            oldemail = email.toString();
                            oldpasswd = passwd.toString();
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


    // send a get request to get the current user's profile
    public void sendDelete( final Context context,
                           final TextView result ){

        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/user/profile";

        // Request a string response from the provided URL.
        StringRequest stringRequest  = new StringRequest(Request.Method.DELETE, addr,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try{
                            result.setText("");
                            // check to see whether the cookie's been set. if so, redirect to main
                            AndroidApplication app = (AndroidApplication)getApplication();
                            app.setCookie(null);
                            Intent intent = new Intent(context, NoSessionActivity.class);
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


    // make put request to submit user profile changes
    public void sendChanges(final String fname,
                            final String lname,
                            final String email,
                            final String passwd,
                            final TextView results){
        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/user/profile";

        final JSONObject jsonRequest = new JSONObject();
        try {
            if(!fname.equals(oldfname)) jsonRequest.put("fname", fname);
            if(!lname.equals(oldlname)) jsonRequest.put("lname", lname);
            if(!email.equals(oldemail)) jsonRequest.put("email", email);
            if(!passwd.equals(oldpasswd)) jsonRequest.put("passwd", passwd);

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

    // parse changes in form and send to api for update
    public void submitChanges(View v){
        String fname = ((EditText)findViewById(R.id.fname)).getText().toString();
        String lname = ((EditText)findViewById(R.id.lname)).getText().toString();
        String email = ((EditText)findViewById(R.id.email)).getText().toString();
        String passwd = ((EditText)findViewById(R.id.passwd)).getText().toString();
        TextView results = (TextView) findViewById(R.id.results);
        if( fname != null && !fname.equals("") &&
                lname != null && !lname.equals("") &&
                email != null && !email.equals("") &&
                passwd != null && !passwd.equals("") ){
            sendChanges(fname, lname, email, passwd, results);
        }
        else{
            results.setText("Error: All fields required");
        }

    }

    // parse and send delete order to API
    public void submitDelete(View v){
        TextView results = (TextView) findViewById(R.id.results);
        sendDelete(this, results);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    // populate form when app resumes (or when first created)
    @Override
    protected void onResume() {
        TextView fname = (TextView) findViewById(R.id.fname);
        TextView lname = (TextView) findViewById(R.id.lname);
        TextView passwd = (TextView) findViewById(R.id.passwd);
        TextView email = (TextView) findViewById(R.id.email);
        TextView result = (TextView) findViewById(R.id.results);
        getProfile( fname, lname, passwd, email, result );
        super.onResume();
    }
}
