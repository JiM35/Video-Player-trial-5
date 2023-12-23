package com.example.videoplayer_trial5;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

// Adapter for showing video files - getting video files in a folder
public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {
    private final ArrayList<MediaFiles> videoList;
    private final Context context;
    BottomSheetDialog bottomSheetDialog;

    public VideoFilesAdapter(ArrayList<MediaFiles> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoFilesAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.videoName.setText(videoList.get(position).getDisplayName());  // Here we get the video name
        String size = videoList.get(position).getSize();  // The size variable will return the size of video in bytes.
        holder.videoSize.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(size)));  // Now we have to change the byte size to MB or KB using formatter.
        double milliSeconds = Double.parseDouble(videoList.get(position).getDuration());  // This variable contains the duration of video in milliseconds. We have to change the milliseconds into hours, minutes and seconds according to the duration of video. For this we have to create a method - timeConversion
        holder.videoDuration.setText(timeConversion((long) milliSeconds));  // We will get the duration in milliseconds. Long value we will pass milliseconds
        Glide.with(context).load(new File(videoList.get(position).getPath())).into(holder.thumbnail);  // For getting the thumbnail, we will use glide - that is image loading format
        holder.menu_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
                View bsView = LayoutInflater.from(context).inflate(R.layout.video_bs_layout, v.findViewById(R.id.bottom_sheet));  // bottom_sheet is id of LinearLayout in video_bs_layout.xml
//                setOnClickListener on first item with respect to ID of thet item
                bsView.findViewById(R.id.bs_play).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.itemView.performClick();
//                        After perfoming click we have to dismiss the bottomSheetDialog.
                        bottomSheetDialog.dismiss();
                    }
                });

                bsView.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle("Rename to");
                        EditText editText = new EditText(context);
                        String path = videoList.get(position).getPath();
                        final File file = new File(path);
                        String videoName = file.getName();
//                        Suppose you have a file with the name abc.mp4. We are getting video file name file.getName();, with the extension that will save to the variable videoName.
//                        Below, we are removing the extension using substring. Substring is from 0 to dot (.). 0 means start to dot that will remove the mp4 extension - abc.mp4. The variable videoName contains the video file name with NO extension.
                        videoName = videoName.substring(0, videoName.lastIndexOf("."));
//                        Set the video name to edit text.
                        editText.setText(videoName);
                        alertDialog.setView(editText);
                        editText.requestFocus();

//                        Create two buttons for OK and CANCEL.
                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
//                                When user clicks on OK after changing the name of file, we will create a string variable. This variable will contain only path without file name. This will return the path of video file.
                                String onlyPath = file.getParentFile().getAbsolutePath();
//                                Here we will get extension
                                String ext = file.getAbsolutePath();
                                ext = ext.substring(ext.lastIndexOf("."));
//                                Suppose we have a video file name with path Media/Videos/abc.mp4. This (Media/Videos/abc.mp4) complete path will be saved in onlyPath variable. We are using second slash then abc, the filename changed by user and on last filename with extension.mp4.
//                                This complete path of video file will be changed in newPath variable (below).
                                String newPath = onlyPath + "/" + editText.getText().toString() + ext;
                                File newFile = new File(newPath);
                                boolean rename = file.renameTo(newFile);
//                                We have to check if the video is renamed or not by using if statement.
                                if (rename) {
                                    ContentResolver resolver = context.getApplicationContext().getContentResolver();
                                    resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + "=?", new String[]{file.getAbsolutePath()});
                                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    intent.setData(Uri.fromFile(newFile));
                                    context.getApplicationContext().sendBroadcast(intent);

                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Video Renamed!", Toast.LENGTH_SHORT).show();

                                    SystemClock.sleep(200);
                                    ((Activity) context).recreate();  // This will refresh the app automatically and there will be no need to close and open the app again.
                                } else {
//                                    In else statement, if video is not renamed we have to show a toast.
                                    Toast.makeText(context, "Process Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

//                        Create a negative button (CANCEL).
                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
//                                When user clicks on cancel, we will dismiss the dialog.
                                dialogInterface.dismiss();
                            }
                        });

                        alertDialog.create().show();
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.setContentView(bsView);
                bottomSheetDialog.show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Part 6 - 18:09, Part 7
                Intent intent = new Intent(context, VideoPlayerActivity.class);  // When user clicks on video item, it will navigate to next activity using Intent. The second activity will be video player activity - we will have to create it.
                intent.putExtra("position", position);
//                We will also get video name for showing the video in title bar
                intent.putExtra("video_title", videoList.get(position).getDisplayName());  // Get video name from videoList. videoList.get(position).getDisplayName() will return video name.
//                For sending ArrayList through intent we will use bundle. Before using bundle we will have to implement media files that is our model file.
//                We have to implement it to Parcelable. In video files adapter, in Intent, we use bundle for sending video list to videoplayeractivity through intent.
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("videoArrayList", videoList);
                intent.putExtras(bundle);  // Select putExtras - with (s). We will get these values in VideoPlayerActivity.
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }  // Name of adapter is VideoFilesAdapter

    public class ViewHolder extends RecyclerView.ViewHolder {
//        Assign IDs to TextViews and ImageViews
        ImageView thumbnail, menu_more;
        TextView videoName, videoSize, videoDuration;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            menu_more = itemView.findViewById(R.id.video_menu_more);
            videoName = itemView.findViewById(R.id.video_name);
            videoSize = itemView.findViewById(R.id.video_size);
            videoDuration = itemView.findViewById(R.id.video_duration);
        }
    }

    //    We have to change the milliseconds into hours, minutes and seconds according to the duration of video. For this we have to create a method - timeConversion
    @SuppressLint("DefaultLocale")
    public String timeConversion(long value){
        String videoTime;
        int duration = (int) value;
        int hrs = (duration / 3600000);  // 3600000 - milliseconds in one hour
        int mns = (duration / 60000) % 60000; // 60000 - milliseconds in one minute
        int scs = (duration % 60000 / 1000); // Divide by 1000 to change to seconds
        if (hrs > 0) {
            videoTime = String.format("%02d:%02d:%02d", hrs, mns, scs);  // If we do duration more than one hour, then we will use format - hour, minutes and seconds
        } else {
            videoTime = String.format("%02d:%02d", mns, scs);  // If video had duration less than 1 hour then we will use this format - minutes, seconds
        }
        return videoTime;
    }
}


// For sending Arraylist through Intent, we will use Bundle. Before using bundle we have to implement media files - our model file. We have to implement it to pass label.
