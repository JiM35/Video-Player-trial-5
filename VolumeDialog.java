package com.example.videoplayer_trial5;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class VolumeDialog extends AppCompatDialogFragment {

    //    Create object for ImageView and TextView
    private ImageView cross;  // For closing dialog
    private TextView volume_no;  // For showing no of volume
    private SeekBar seekBar;
    AudioManager audioManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        Create alert dialog for showing volume controls
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.vol_dialog_item, null);
        builder.setView(view);  // We have set vol_dialog_item to builder AlertDialog.
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

//        Assign memory using view.findViewById
        cross = view.findViewById(R.id.volume_close);
        volume_no = view.findViewById(R.id.vol_number);
        seekBar = view.findViewById(R.id.vol_seekbar);

        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
//        When user changes the seekbar position, we will work in onProgressChanged.
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                int mediavolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxvol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//                Get the volume percentage. The return type will be double
                double volPer = Math.ceil(((double) mediavolume / (double) maxvol) * (double) 100);
                volume_no.setText("" + volPer);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        setOnClickListener on cross. When user clicks on cross, we will close dialog
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return builder.create();
    }
}
