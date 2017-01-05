package com.example.james.testapp;

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

public class RegisterActivity extends AppCompatActivity {

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, NoSessionActivity.class);
        startActivity( intent );
        return;
    }

    // make post request to submit user registration
    public void sendRegister(final String fname,
                            final String lname,
                            final String email,
                            final String passwd,
                            final TextView results){
        RequestQueue queue = Volley.newRequestQueue(this);
        String addr = "http://35.160.5.169/user";

        final JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("fname", fname);
            jsonRequest.put("lname", lname);
            jsonRequest.put("email", email);
            jsonRequest.put("passwd", passwd);

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
                        results.setText( "User created successfully!" );

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

    public void submitRegister(View v){
        String fname = ((EditText)findViewById(R.id.fname)).getText().toString();
        String lname = ((EditText)findViewById(R.id.lname)).getText().toString();
        String email = ((EditText)findViewById(R.id.email)).getText().toString();
        String passwd = ((EditText)findViewById(R.id.passwd)).getText().toString();
        TextView results = (TextView) findViewById(R.id.results);
        if( fname != null && !fname.equals("") &&
                lname != null && !lname.equals("") &&
                email != null && !email.equals("") &&
                passwd != null && !passwd.equals("") ){
            sendRegister(fname, lname, email, passwd, results);
        }
        else{
            results.setText("Error: All fields required");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
}
