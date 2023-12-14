package com.example.videoplayer_trial5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AllowAccessActivity extends AppCompatActivity {

    public static final int STORAGE_PERMISSION = 1;
    Button allow_btn;
    public static final int REQUEST_PERMISSION_SETTING = 12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_access);
        allow_btn = findViewById(R.id.allow_access);
        allow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // First we have to check if user granted permission or not.
                    // If user granted permission we will navigate the user from this activity to Main Activity by using Intent.
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                } else {  // In else statement if user deny that prompt then we will again show that prompt.
                    ActivityCompat.requestPermissions(AllowAccessActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);  // Create a request code - STORAGE_PERMISSION
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {  // Compare the request code with storage permission request code that we gave above. By this we will check whether user clicked on deny or never-ask-again. First of all we have to create for loop.
            for (int i = 0; i < permissions.length; i++) {
                String per = permissions[i];  // Create string variable permission - per
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {  // If user denied permission here we will check whether user clicked on never ask again or not.
                    boolean showRationale = shouldShowRequestPermissionRationale(per);  // Create a boolean value
                    if (!showRationale) {
                        // Means that user clicked on never-ask-again button
                        // We will create an alert dialog for guiding user why we need to allow the runtime permission for this create alert dialog.
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("App Permission").setMessage("To play videos, you must allow this app to access video files on your device" + "\n\n" + "Now follow the below steps" + "\n\n" + "Open Settings from below button" + "\n\n" + "Click on Permissions" + "\n\n" + "Allow access to storage").setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {  // If user clicks on Open settings button, it will navigate the user to app setting. There the user will allow permissions manually.
                                // To move to settings we will create Intent
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            }
                        }).create().show();
                    } else {  // This else statement means user has not clicked on never-ask-again, user clicked on deny. If user clicked on deny we will again show that prompt
                        ActivityCompat.requestPermissions(AllowAccessActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);  //Code copied from above
                    }
                } else {  // In the else statement, we are checking if user clicked on deny button and in this else statement user will click on allow button. If user clicks on allow button we will switch the user to MainActivity by using Intent.
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));  // Code copied from above
                    finish();
                }
            }
        }
    }  // We cannot navigate to MainActivity because the app is paused so after coming back to this Activity on resume method is called. Let's work on Resume method

}