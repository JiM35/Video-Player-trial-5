package com.example.videoplayer_trial5;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class BrightnessDialog extends AppCompatDialogFragment {

    private TextView brightness_no;
    private ImageView cross;
    private SeekBar seekBar;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
//        Create object for View
        View view = inflater.inflate(R.layout.brt_dialog_item, null);
//        We are setting this view to AlertDialog
        builder.setView(view);

//        Assign IDs
        cross = view.findViewById(R.id.brt_close);
        brightness_no = view.findViewById(R.id.brt_number);
        seekBar = view.findViewById(R.id.brt_seekbar);

//        Crete int variable for creating brightness. The third attribute is 0.
        int brightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
        brightness_no.setText(brightness + "");
        seekBar.setProgress(brightness);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//                onProgressChanged means user swiped a seekbar, changed the position of seekbar
//                They will first create object for context
                Context context = getContext().getApplicationContext();
                boolean canWrite = Settings.System.canWrite(context);
//                Add permission in AndroidManifest.xml
                if (canWrite) {
                    int sBrightness = progress * 255 / 255;
                    brightness_no.setText(sBrightness + "");
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, sBrightness);
                } else {
//                    In else statement we will check if the write Settings are not enabled, we will navigate the user to Settings where user will enable the write Settings
                    Toast.makeText(context, "Enable write settings for Brightness Control", Toast.LENGTH_SHORT).show();
//                    Navigate the user to Settings using Intent
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    startActivityForResult(intent, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        Dismiss the dialog
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return builder.create();
    }
}
