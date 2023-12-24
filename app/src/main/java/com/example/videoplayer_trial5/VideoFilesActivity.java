package com.example.videoplayer_trial5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import java.util.ArrayList;


public class VideoFilesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String MY_PREF = "my_pref";
    //    In VideoFilesAdapter we get the thumbnail and video duration now we will initialize the adapter in VideoFilesActivity.
    RecyclerView recyclerView;
    private ArrayList<MediaFiles> videoFilesArrayList = new ArrayList<>();
    static VideoFilesAdapter videoFilesAdapter;  // Make it static so we can call
    String folder_name;
    //    Create object for SwipeRefreshLayout
    SwipeRefreshLayout swipeRefreshLayout;
    //    Create String variable that will be global variable.
    String sortOrder;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);
        folder_name = getIntent().getStringExtra("folderName");  // The folder_name has to be above the getSupportActionBar(), if you are not doing this, you will get an error
        getSupportActionBar().setTitle(folder_name);  // We have to set this folder_name on toolbar title
        recyclerView = findViewById(R.id.videos_rv);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_videos);
        showVideoFiles();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                When user swipes down for refreshing the video files, we will call showVideoFiles method.
                showVideoFiles();
                swipeRefreshLayout.setRefreshing(false);  // Means we hide the refresh layout

            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showVideoFiles() {
        videoFilesArrayList = fetchMedia(folder_name);  // Pass the folder name in fetchMedia method
        videoFilesAdapter = new VideoFilesAdapter(videoFilesArrayList, this);
        recyclerView.setAdapter(videoFilesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        videoFilesAdapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchMedia(String folderName) {
//        Create SharedPreferences, we are going to get all the values that we are sending when user clicks on Sort By through AlertDialog. We are getting all the values in fetchMedia method through SharedPreferences
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
//        Create String variable name it sort_value. In getString, we have to pass key through which we are sending the value  (The key is sort - In onOptionsItemSelected method)
        String sort_value = preferences.getString("sort", "abcd");


        ArrayList<MediaFiles> videoFiles = new ArrayList<>(); // This method wil return the media files
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

//        If you want to change the sortOrder from Big-to-Small to Small-to-Big or from New-to-Old to Old-to-New and Length from Long-to-Short to Short-to-Long, you have to change ASC, DESC order.
//        If the String variable sort_value equals the value of the first item - sortName, then we will sort the video items according to Name (from A to Z).
        if (sort_value.equals("sortName")) {
            sortOrder = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";  // By using this video file will be in ascending order when user selects first item.
        } else if (sort_value.equals("sortSize")) {  // sort_value.equals to the second value i.e. Size and the value that we are sending through SharedPreferences - sortSize.
//            If user selects the second option according to size then we will sort the video item using MediaStore.MediaColumns.SIZE + " DESC".
            sortOrder = MediaStore.MediaColumns.SIZE + " DESC";
        } else if (sort_value.equals("sortDate")) {
            sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        } else {  // In else statement we will sort video file according to length - forth option.
            sortOrder = MediaStore.Video.Media.DURATION + " DESC";
        }  // Pass sortOrder below in Cursor

        String selection = MediaStore.Video.Media.DATA + " like?";  // DATA for path
        String[] selectionArg = new String[]{"%" + folderName + "%"};  // Take String of Array type
        Cursor cursor = getContentResolver().query(uri, null, selection, selectionArg, sortOrder);  // selection and selection argument will return the video files in a specific folder
        if (cursor != null && cursor.moveToNext()) {
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                @SuppressLint("Range") String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                @SuppressLint("Range") String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path, dateAdded);
                videoFiles.add(mediaFiles);  // We have to add media files to video files. Add variable of modal class - mediaFiles.
            } while (cursor.moveToNext());
        }
        return videoFiles;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_video);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }



//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//
//        int id = item.getItemId();
//        switch (id) {
//            case R.id.refresh_files:
//                finish();
//                startActivity(getIntent());
//                break;
//            case R.id.sort_by:
////                If user clicks on Sort by, we will show AlertDialog that contains 4 radio buttons through which user can sort video by name, length, size and duration accordingly.
//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//                alertDialog.setTitle("Sort By");
//                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
////                        When user will click on OK we will hide the AlertDialog
////                        By clicking on OK after selecting any option in all of the four options, we will apply the changes accordingly.
//                        editor.apply();
//                        finish();
//                        startActivity(getIntent());
//                        dialogInterface.dismiss();
//                    }
//                });
////                We will create a String variable of Array tyoe
//                String[] items = {"Name (A to Z)", "Size (Big to Small)", "Date (New to Old", "Length (Long to Short)"};
//                alertDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {  // -1 because we do not want any item selected as default. If we write 0 means we want first item selected as default
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        switch (i) {
//                            case 0:
////                                If the user selects the first item i.e. Name from A to Z, we will send the data through SharedPreferences editor.putString(). Key will be sort and the value we want to send for Name sortName.
//                                editor.putString("sort", "sortName");
//                                break;
//                            case 1:
////                                When user select the second option i.e. Size (Big to Small), we will send the value as sortSize.
//                                editor.putString("sort", "sortSize");
//                                break;
//                            case 2:
////                                Third option is Date. Value will be sortDate.
//                                editor.putString("sort", "sortDate");
//                                break;
//                            case 3:
////                                We will keep keep key as same in all the four items.
//                                editor.putString("sort", "sortLength");
//                                break;
//                        }
//                    }
//                });
//                alertDialog.create().show();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int id = item.getItemId();
        if (id == R.id.refresh_files) {
            finish();
            startActivity(getIntent());
        } else if (id == R.id.sort_by) {
//            If user clicks on Sort by, we will show AlertDialog that contains 4 radio buttons through which user can sort video by name, length, size and duration accordingly.
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Sort By");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
//                        When user will click on OK we will hide the AlertDialog
//                        By clicking on OK after selecting any option in all of the four options, we will apply the changes accordingly.
                    editor.apply();
                    finish();
                    startActivity(getIntent());
                    dialogInterface.dismiss();
                }
            });
//                We will create a String variable of Array tyoe
            String[] items = {"Name (A to Z)", "Size (Big to Small)", "Date (New to Old)", "Length (Long to Short)"};
            alertDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {  // -1 because we do not want any item selected as default. If we write 0 means we want first item selected as default
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case 0:
//                                If the user selects the first item i.e. Name from A to Z, we will send the data through SharedPreferences editor.putString(). Key will be sort and the value we want to send for Name sortName.
                            editor.putString("sort", "sortName");
                            break;
                        case 1:
//                                When user select the second option i.e. Size (Big to Small), we will send the value as sortSize.
                            editor.putString("sort", "sortSize");
                            break;
                        case 2:
//                                Third option is Date. Value will be sortDate.
                            editor.putString("sort", "sortDate");
                            break;
                        case 3:
//                                We will keep keep key as same in all the four items.
                            editor.putString("sort", "sortLength");
                            break;
                    }
                }
            });
            alertDialog.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
//        When user will write any text, we will first create string variable
        String inputs = newText.toLowerCase();
        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        for (MediaFiles media : videoFilesArrayList) {
            if (media.getTitle().toLowerCase().contains(inputs)) {
//                If the search item contains the name of any video file, we will show the results
                mediaFiles.add(media);
            }
        }
//        Below for loop we have to update VideoFilesAdapter.
        VideoFilesActivity.videoFilesAdapter.updateVideoFiles(mediaFiles);
        return true;
    }
}
