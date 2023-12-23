package com.example.videoplayer_trial5;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import javax.xml.namespace.QName;

public class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.ViewHolder> {
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPath;
    private Context context;

    public VideoFoldersAdapter(ArrayList<MediaFiles> mediaFiles, ArrayList<String> folderPath, Context context) {
        this.mediaFiles = mediaFiles;
        this.folderPath = folderPath;
        this.context = context;
    }

    @NonNull
    @Override

//    Here onCreateViewHolder will create a new ViewHolder whenever recyclerView  needs one - means whenever you download a video from any source this onCreateViewHolder method will create a new ViewHolder.
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.folder_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
//    Retrieve other folders of device storage containing video files
//    onBindViewHolder method that is called by recycler view displays data on specified position
//    onBindViewHolder will update RecyclerView and show the actual data
//    We have to bind the data on onBindViewHolder
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

//        lastIndexOf - will return the index of substring that is on last occurrence. Suppose we have a path - /storage/Media/Videos - here lastIndexOf will return a slash that is last on occurrence in this path. The slash between Media and Videos is on last occurrence that is returned and save in index path variable.
        int indexPath = folderPath.get(position).lastIndexOf("/");
//        The slash value is saved in indexPath and here on substring indexPath + 1 means slash value + 1 will return folder name. We will set folder name in setText(nameOfFolder).
        String nameOfFolder = folderPath.get(position).substring(indexPath + 1);
        holder.folderName.setText(nameOfFolder);
        holder.folder_path.setText(folderPath.get(position));
//        We are setting static text for now - 5 Videos
        holder.noOfFiles.setText(noOfFiles(folderPath.get(position)) + " Videos");
//        When user clicks on folder item it will navigate user to next activity using Intent
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoFilesActivity.class);
                intent.putExtra("folderName", nameOfFolder);  // Here we are getting the folder name using Intent
                context.startActivity(intent);

            }
        });
    }

    @Override
//    getItemCount method will return total number of items in adapter
    public int getItemCount() {
        return folderPath.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView folderName, folder_path, noOfFiles;
        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            folder_path = itemView.findViewById(R.id.folderPath);
            noOfFiles = itemView.findViewById(R.id.noOfFiles);
        }
    }

    //    Create method of int type - noOfFiles. We will call the noOfFiles method for getting total number of video files in onBindViewHolder.
    int noOfFiles(String folder_name) {

        int files_no = 0;
        for (MediaFiles mediaFiles : mediaFiles) {
            if (mediaFiles.getPath().substring(0, mediaFiles.getPath().lastIndexOf("/")).endsWith(folder_name)) {
                files_no++;
            }
        }
        return files_no;
    }
}