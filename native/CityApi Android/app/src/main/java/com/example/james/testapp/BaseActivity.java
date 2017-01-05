package com.example.james.testapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by James on 12/1/2016.
 */

public class BaseActivity extends AppCompatActivity {
    protected void onResume(){
        AndroidApplication app = (AndroidApplication)getApplication();
        if( app.getCookie() == null || app.getCookie().isEmpty() ){
            Log.i("COOKIE: ", "Cookie not set...\n");
            Intent intent = new Intent(this, NoSessionActivity.class);
            startActivity( intent );
        }
        else{
            Log.i("COOKIE: ", "Cookie is set...\n");
        }
        super.onResume();
    }
}
