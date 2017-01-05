package com.example.james.testapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainCitiesActivity extends AppCompatActivity {

    public void gotoViewCities(View v){
        Intent intent = new Intent(this, ViewCitiesActivity.class);
        startActivity( intent );

    }

    public void gotoAddCity(View v){
        Intent intent = new Intent(this, AddCityActivity.class);
        startActivity( intent );

    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity( intent );
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cities);
    }
}
