package com.nanb.alpha;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;


public class videocalling extends AppCompatActivity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videocalling);

        String currentuserid = getIntent().getExtras().get("currentuser").toString();


    }


}
    /**private static final String APP_KEY = "707474fe-50a7-4436-9691-e282f8dd9f20";
    private static final String APP_SECRET = "kiZidiuSSUeDcF9H0SCZZA==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";**/