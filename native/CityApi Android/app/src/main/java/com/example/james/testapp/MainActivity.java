package com.example.james.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends BaseActivity {

    @Override
    public void onBackPressed()
    {
        AndroidApplication app = (AndroidApplication)getApplication();
        app.setCookie(null);
        Intent intent = new Intent(this, NoSessionActivity.class);
        startActivity( intent );
        return;
    }

    public void gotoProfile(View v){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity( intent );

    }

    public void gotoMainCities(View v){
        Intent intent = new Intent(this, MainCitiesActivity.class);
        startActivity( intent );

    }

    public void gotoGetLocalMap(View v){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity( intent );

    }

    public void gotoGetLocation(View v){
        Intent intent = new Intent(this, DisplayLocationActivity.class);
        startActivity( intent );

    }

    public void logout(View v){
        AndroidApplication app = (AndroidApplication)getApplication();
        app.setCookie(null);
        Intent intent = new Intent(this, NoSessionActivity.class);
        startActivity( intent );

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
