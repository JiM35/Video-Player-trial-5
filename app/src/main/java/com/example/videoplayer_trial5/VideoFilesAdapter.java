package com.example.videoplayer_trial5;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
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
