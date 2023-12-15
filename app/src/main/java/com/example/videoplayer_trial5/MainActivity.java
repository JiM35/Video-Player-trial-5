package com.example.videoplayer_trial5;

import static com.example.videoplayer_trial5.AllowAccessActivity.REQUEST_PERMISSION_SETTING;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            // If the user grants the permission by manually going to settings and again user deny the permission in settings and go back to the app, the onResume method will call and that will navigate the user to MainActivity. We will check if permission is denied, then we will send user again to the settings.
            Toast.makeText(this, "Click on permissions and allow storage", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);  // If user clicks on allow button then deny button and goes back then the Main Activity code will compile then show a Toast then again move the user to setting window.
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
        }
    }
}