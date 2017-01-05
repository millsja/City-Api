package com.example.james.testapp;

import android.app.Application;
import android.util.Log;

/**
 * Created by James on 12/1/2016.
 */

public class AndroidApplication extends Application {

    private String cookie;

    public void onCreate(){
        super.onCreate();
        cookie = new String("");
    }

    public String getCookie(){
        return this.cookie;
    }

    public void setCookie(String newCookie){
        this.cookie = newCookie;
        if(newCookie != null){
            Log.i("COOKIE: ", newCookie);
        }
        else{
            Log.i("COOKIE: ", "null");
        }
    }
}
