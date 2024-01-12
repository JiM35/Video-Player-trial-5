package com.example.videoplayer_trial5;

import static com.example.videoplayer_trial5.VideoFilesActivity.MY_PREF;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;


public class PlayListDialog extends BottomSheetDialogFragment {
//    Here we will include the bottom sheet dialog
//    Create object for ArrayList of MediaFiles
    ArrayList<MediaFiles> arrayList = new ArrayList<>();
    VideoFilesAdapter videoFilesAdapter;
    BottomSheetDialog bottomSheetDialog;
    RecyclerView recyclerView;
    //    TextView for showing folder name
    TextView folder;

    //    Create constructor
    public PlayListDialog(ArrayList<MediaFiles> arrayList, VideoFilesAdapter videoFilesAdapter) {
        this.arrayList = arrayList;
        this.videoFilesAdapter = videoFilesAdapter;
    }

    @SuppressLint({"NotifyDataSetChanged"})
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

//        Before inflating the layout we have to equalize the bottomSheetDialog to return call
//        Create object for BottomSheetDialog
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
//        Create object for View
        View view = LayoutInflater.from(getContext()).inflate(R.layout.playlist_bs_layout, null);
        bottomSheetDialog.setContentView(view);

//        Initialize recyclerView
        recyclerView = view.findViewById(R.id.playlist_rv);
        folder = view.findViewById(R.id.playlist_name);

//        Get the folderName from VideoFilesActivity
//        In VideoFilesActivity we will save the folder_name (in onCreate method) in SharedPreferences and receive the folder_name in PlayListDialog class
//        Copy the key playListFolderName, in PlayListDialog we will retrieve the folder_name using SharedPreferences
        SharedPreferences preferences = this.getActivity().getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
//        Pass the key playListFolderName here - keep the key same as in VideoFilesActivity
        String folderName = preferences.getString("playListFolderName", "abc");

        folder.setText(folderName);

//        In fetchMedia method input we have to pass folderName
        arrayList = fetchMedia(folderName);
//        Initialize videoFilesAdapter
        videoFilesAdapter = new VideoFilesAdapter(arrayList, getContext(), 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoFilesAdapter);
        videoFilesAdapter.notifyDataSetChanged();

        return bottomSheetDialog;
    }

//    Fetch all the videos for showing in playlist - we will copy fetchMedia method from VideoFilesActivity
    private ArrayList<MediaFiles> fetchMedia(String folderName) {

        ArrayList<MediaFiles> videoFiles = new ArrayList<>(); // This method wil return the media files
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Video.Media.DATA + " like?";  // DATA for path
        String[] selectionArg = new String[]{"%" + folderName + "%"};  // Take String of Array type
        Cursor cursor = getContext().getContentResolver().query(uri, null, selection, selectionArg, null);  // selection and selection argument will return the video files in a specific folder
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
}