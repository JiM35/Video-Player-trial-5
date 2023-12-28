package com.example.videoplayer_trial5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaybackIconsAdapter extends RecyclerView.Adapter<PlaybackIconsAdapter.ViewHolder> {

    private ArrayList<IconModel> iconModelsList;
    private Context context;

    public PlaybackIconsAdapter(ArrayList<IconModel> iconModelsList, Context context) {
        this.iconModelsList = iconModelsList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlaybackIconsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        Create object for view
        View view = LayoutInflater.from(context).inflate(R.layout.icons_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaybackIconsAdapter.ViewHolder holder, int position) {

        holder.icon.setImageResource(iconModelsList.get(position).getImageView());
        holder.iconName.setText(iconModelsList.get(position).getIconTitle());

    }

    @Override
    public int getItemCount() {
//        It will get the size of List
        return iconModelsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //        Create object for TextView
        TextView iconName;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            icon = itemView.findViewById(R.id.playback_icon);
            iconName = itemView.findViewById(R.id.icon_title);
        }
    }
}
