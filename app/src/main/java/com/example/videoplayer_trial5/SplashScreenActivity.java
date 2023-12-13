package com.example.videoplayer_trial5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

// Splash screen is an initial screen when user opens the app. It shows the logo or a company name.

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                Here we will use intent for navigating this activity to another activity that will allow access activity where we will get the storage access from the user
                startActivity(new Intent(SplashScreenActivity.this, AllowAccessActivity.class));
                finish();
            }
        }, 3000);  // Assign for seconds for splash activity (3 seconds)
    }
}