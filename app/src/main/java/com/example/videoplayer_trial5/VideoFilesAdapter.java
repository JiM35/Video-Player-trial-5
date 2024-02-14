package com.example.videoplayer_trial5;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import java.util.ArrayList;

// Adapter for showing video files - getting video files in a folder
public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {
    private ArrayList<MediaFiles> videoList;
    private final Context context;
    BottomSheetDialog bottomSheetDialog;
    private final int viewType;

    public VideoFilesAdapter(ArrayList<MediaFiles> videoList, Context context, int viewType) {
        this.videoList = videoList;
        this.context = context;
//        this means this class
        this.viewType = viewType;
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
        holder.videoDuration.setText(Utility.timeConversion((long) milliSeconds));  // We will get the duration in milliseconds. Long value we will pass milliseconds
        Glide.with(context).load(new File(videoList.get(position).getPath())).into(holder.thumbnail);  // For getting the thumbnail, we will use glide - that is image loading format

        if (viewType == 0) {
            holder.menu_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
                    View bsView = LayoutInflater.from(context).inflate(R.layout.video_bs_layout, v.findViewById(R.id.bottom_sheet));  // bottom_sheet is id of LinearLayout in video_bs_layout.xml
//                setOnClickListener on first item with respect to ID of that item
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
//                                We have to add validation in rename video file edit text – when user will click on OK after renaming the file, we will check if the edit text is empty or not.
                                    if (TextUtils.isEmpty(editText.getText().toString())) {
                                        Toast.makeText(context, "Can't rename Empty file", Toast.LENGTH_SHORT).show();
//                                    After showing the toast we will return it
                                        return;
                                    }
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

                    bsView.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                        When user clicks on share we have to share video using URI
                            Uri uri = Uri.parse(videoList.get(position).getPath());
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("video/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            context.startActivity(Intent.createChooser(shareIntent, "Share Video via"));
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bsView.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                        We will show AlertDialog with two buttons, DELETE and CANCEL, when user click on delete video
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Delete");
                            alertDialog.setMessage("Do you want to delete this video?");
                            alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.parseLong(videoList.get(position).getId()));
                                    File file = new File(videoList.get(position).getPath());  // Get the path of video that you want to delete
                                    boolean delete = file.delete();

//                                Check if the file is deleted or not using if statement
                                    if (delete) {
//                                    If the file is deleted we creaate context
                                        context.getContentResolver().delete(contentUri, null, null);
                                        videoList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, videoList.size());
                                        Toast.makeText(context, "Video Deleted", Toast.LENGTH_SHORT).show();
                                    } else {
//                                    In else statement if video is not deleted we have to show Toast
                                        Toast.makeText(context, "Video cannot be Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
//                        Not we work on delete button that will be CANCEL
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
//                                When user clicks on cancel, dialog will dismiss
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bsView.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                        When user will click on properties of video, we will show AlertDialog for showing all the properties
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Properties");


//                        We will get all the properties of video files
//                        Create String variable for getting first property
                            String one = "File: " + videoList.get(position).getDisplayName(); // Static text will be file because first property is file name

//                        Second, we will show the path of that file
                            String path = videoList.get(position).getPath();  // Here we are getting the complete path of video file with video name and extension
                            int indexOfPath = path.lastIndexOf("/");  // Here using lastIndexOf and substring we are cutting the video file and extension and the variable two will contain only the path of video file without video name.
//                        Second property of video files - path
                            String two = "Path: " + path.substring(0, indexOfPath);

//                        The third variable will be size of video file.
//                        We will get the size of video file using Formatter. Press Ctrl and click on size. We are getting the size variable (above) that contains the size of video file.
                            /** String three = "Size: " + android.text.format.Formatter.formatFileSize(context, size); */
//                        We can also write:
                            String three = "Size: " + android.text.format.Formatter.formatFileSize(context, Long.parseLong(videoList.get(position).getSize()));

//                        We will get the forth property of video file. The forth property will be length of video file
                            String four = "Length: " + Utility.timeConversion((long) milliSeconds);  // We will get length of video file from using the timeConversion method

//                        The fifth property is format of video file
                            String nameWithFormat = videoList.get(position).getDisplayName();  // The getDisplayName contains the video file name with extension. We will separate the extension and show in fifth variable
                            int index = nameWithFormat.lastIndexOf(".");
                            String format = nameWithFormat.substring(index + 1);  // The index + 1 will get extension of video file that will be stored in format variable and we set the format variable to fifth property.
                            String five = "Format: " + format;

//                        Get the sixth property of video file
                            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                            mediaMetadataRetriever.setDataSource(videoList.get(position).getPath());
                            String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);  // First we get height of video
                            String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);  // Create second variable for getting the width of video
                            String six = "Resolution: " + width + "x" + height;  // Set the width and height here. "x" will represent multiply


//                        We will pass all the variables one, two, three, four, five and six. These are the six properties of video file, we will set all these properties in setMessage
                            alertDialog.setMessage(one + "\n\n" + two + "\n\n" + three + "\n\n" + four + "\n\n" + five + "\n\n" + six);  // In setMessage, we have to show all the properties of video files one by one
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
//                                When user clicks on OK button after checking all the properties of that video file we will dismiss the AlertDialog
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.show();
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bottomSheetDialog.setContentView(bsView);
                    bottomSheetDialog.show();
                }
            });
        } else {
//            In else statement, if view type has any other int value except 0, then we will set the visibility to GONE, because in playlist we do not want the menu_more icon
            holder.menu_more.setVisibility(View.GONE);
//            Change the color of video name and video size to white
            holder.videoName.setTextColor(Color.WHITE);
            holder.videoSize.setTextColor(Color.WHITE);
        }

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
                if (viewType == 1) {
                    ((Activity) context).finish();
                }
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

    /**
//    We have to change the milliseconds into hours, minutes and seconds according to the duration of video. For this we have to create a method - timeConversion
    @SuppressLint("DefaultLocale")
    public String timeConversion(long value) {
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
     **/

    @SuppressLint("NotifyDataSetChanged")
    void updateVideoFiles(ArrayList<MediaFiles> files) {
        videoList = new ArrayList<>();
        videoList.addAll(files);
        notifyDataSetChanged();
    }
}


// For sending Arraylist through Intent, we will use Bundle. Before using bundle we have to implement media files - our model file. We have to implement it to pass label.
