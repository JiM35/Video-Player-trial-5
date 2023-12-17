package com.example.videoplayer_trial5;

import static com.example.videoplayer_trial5.AllowAccessActivity.REQUEST_PERMISSION_SETTING;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

//    We will initialize the adapter in RecyclerView
    private ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
    private ArrayList<String> allFolderList = new ArrayList<>();
    RecyclerView recyclerView;
    VideoFoldersAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

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
//        Below if statement we have to allocate memory to recyclerView using id
        recyclerView = findViewById(R.id.folders_rv);
        //        Allocate memory to swipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_folders);
        showFolder();
//        setOnRefreshListener will call when user swipes down for refreshing the layout and when the user swipes down the showFolder method will refresh the adapter and swipeRefreshLayout.setRefreshing(false) will hide the refresh layout.
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showFolder();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    //     Here we are going to initialize the adapter
    @SuppressLint("NotifyDataSetChanged")
    private void showFolder() {
//        fetchMedia() will return all the folders
        mediaFiles = fetchMedia();
        adapter = new VideoFoldersAdapter(mediaFiles, allFolderList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter.notifyDataSetChanged();
    }

    public ArrayList<MediaFiles> fetchMedia() {
        ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//        If cursor is not equal to null means cursor has data then we are going to get all the Strings. We will use do while - do while loop is used to execute a block of statement until the given condition is true.
        if (cursor != null && cursor.moveToNext()) {
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                @SuppressLint("Range") String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                @SuppressLint("Range") String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
//                You have to keep the order of the variables same to the order of variables in MediaFiles model class.
                MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path, dateAdded);
//                Set the folder path to all folder list variable created above  - private ArrayList<String> allFolderList = new ArrayList<>();
//                Path contains the complete path of video files with extension
                int index = path.lastIndexOf("/");
//                The substring will return the path of folder without the file name that is in extension. It will return only the folder path.
                String subString = path.substring(0, index);
                if (!allFolderList.contains(subString)) {
                    allFolderList.add(subString);
                }
                mediaFilesArrayList.add(mediaFiles);
            } while (cursor.moveToNext());
        }
        return mediaFilesArrayList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


/* Video Player App in Android Studio (Part 5) | Swipe to Refresh folders, Rate and Share App - 10:21
https://www.youtube.com/watch?v=Bnhl59v-66c&list=PLrEWK4N0Og0219lp6qxiOU5pSO8hhESUS&index=6 */

    /*
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.rateus:
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.refresh_folders:
                finish();
                startActivity(getIntent());  // It will refresh activity
                break;
            case R.id.share_app:
                Intent share_intent = new Intent();
                share_intent.setAction(Intent.ACTION_SEND);
                share_intent.putExtra(Intent.EXTRA_TEXT, "Check this app via\n" + "https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
                share_intent.setType("text/plain");
                startActivity(Intent.createChooser(share_intent, "Share app via"));
                break;
        }
        return true;
    }
     */

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.rateus) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (id == R.id.refresh_folders) {
            finish();
            startActivity(getIntent());  // It will refresh activity
        } else if (id == R.id.share_app) {
            Intent share_intent = new Intent();
            share_intent.setAction(Intent.ACTION_SEND);
            share_intent.putExtra(Intent.EXTRA_TEXT, "Check this app via\n" + "https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
            share_intent.setType("text/plain");
            startActivity(Intent.createChooser(share_intent, "Share app via"));
        }
        return true;
    }
}