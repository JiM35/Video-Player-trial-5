package com.example.videoplayer_trial5;

import static com.google.android.exoplayer2.Format.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;

// ExoPlayer is an alternative to Android's Media Player. By using Media Player it is very easy to play videos but if you want to create advanced player features using media player then it will require much effort for developers - that is why we will use ExoPlayer for creating those advanced features.

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
//    Now we are getting the video list that we are sending through intent. First create arraylist
    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();

//    Create object for PlayerView and SimpleExoPlayer with variables as playerView and player
    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    String videoTitle;
    TextView title;

//    Initialize the Recycler
//    Horizontal RecyclerView variables
    private ArrayList<IconModel> iconModelArrayList = new ArrayList<>();
    PlaybackIconsAdapter playbackIconsAdapter;

//    Create object for recyclerview
    RecyclerView recyclerViewIcons;
    boolean expand = false;
//    Create object of View
    View nightMode;

//    Create boolean value for night_mode
    boolean dark = false;

//    Create boolean value for Mute
    boolean mute = false;

//    We will create an object for playback parameters
    PlaybackParameters parameters;

//    Float variable for speed
    float speed;

//    When user clicks subtitles icon, we are going to use file picker library for showing directories
//    We will create object for DialogProperties
    DialogProperties dialogProperties;
    FilePickerDialog filePickerDialog;

    //    Create object for Uri
    Uri uriSubtitles;

    private ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK, FULLSCREEN;
    }

    ImageView videoBack, lock, unlock, scaling;
//    Create object for RelativeLayout, take the variable as root
    RelativeLayout root;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextButton, previousButton;

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
//        Now we have to get position and arraylist of video from VideoFilesAdapter using Intent
        getSupportActionBar().hide();
//        Allocate memory to playerView
        playerView = findViewById(R.id.exoplayer_view);  // This id exoplayer_view - has been passed in activity_video_player
        position = getIntent().getIntExtra("position", 1);  // Select getIntExtra because the data type of position is int. In quotes, pass the key that you are using for sending the position.
        videoTitle = getIntent().getStringExtra("video_title");  // Put the key that we are using in VideoFilesAdapter for video title.
//        For getting ArrayList from VideoFilesAdapter we will use Parcelable
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        screenOrientation();
        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);
        title = findViewById(R.id.video_title);
//        Declare the RelativeLayout with id root_layout in custom_playback_view.xml.
//        Allocate memory by using id.
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
//        Allocate memory to scaling
        scaling = findViewById(R.id.scaling);

//        Initialize the file picker objects for showing directories when user clicks on subtitles
        dialogProperties = new DialogProperties();
        filePickerDialog = new FilePickerDialog(VideoPlayerActivity.this);
        filePickerDialog.setTitle("Select a Subtitle File");
//        Set positive and negative buttons
        filePickerDialog.setPositiveBtnName("OK");
        filePickerDialog.setNegativeBtnName("Cancel");

        root = findViewById(R.id.root_layout);
//        Give id to night mode
        nightMode = findViewById(R.id.night_mode);
//        Allocate memory to recyclerview
        recyclerViewIcons = findViewById(R.id.recyclerview_icons);

        title.setText(videoTitle);

        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);  // Pass the View name

//        Initialize the recyclerview - first we will add the items in list
        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
        iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));

        playbackIconsAdapter = new PlaybackIconsAdapter(iconModelArrayList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true);
        recyclerViewIcons.setLayoutManager(layoutManager);
        recyclerViewIcons.setAdapter(playbackIconsAdapter);

        playbackIconsAdapter.notifyDataSetChanged();
        playbackIconsAdapter.setOnItemClickListener(new PlaybackIconsAdapter.OnItemClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(int position) {
//                Set click listeners on item according to position
                if (position == 0) {
//                    First create boolean variable - boolean expand = false;
                    if (expand) {
//                        Again when user clicks on left icon we will remove all the icons and show only four icons for this.
//                        Clear the iconModelArrayList
                        iconModelArrayList.clear();
                        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
//                        When user first clicks on right icon, this else code will execute
//                        We have to expand the list and add the icons in recyclerview.
                        if (iconModelArrayList.size() == 4) {
                            iconModelArrayList.add(new IconModel(R.drawable.ic_volume, "Volume"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_brightness, "Brightness"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_speed, "Speed"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_subtitles, "Subtitles"));
                        }
//                        Set the icon to first position - ic_left. The title will be empty
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_left, ""));
                        playbackIconsAdapter.notifyDataSetChanged();

                        expand = true;
                    }
                }

//                Night mode
                if (position == 1) {
//                    We will first create view of night mode in xml file of VideoPlayerActivity - activity_video_player.
                    if (dark) {
//                        When user again clicks on night icon, we will setVisibility to gone.
                        nightMode.setVisibility(View.GONE);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_night_mode, "Night"));  // Change the title to the previous one - Night
                        playbackIconsAdapter.notifyDataSetChanged();
                        dark = false;
                    } else {
//                        When user first clicks on dark mode, this else code will execute
                        nightMode.setVisibility(View.VISIBLE);

                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_night_mode, "Day"));  // Change the title to Day
                        playbackIconsAdapter.notifyDataSetChanged();
                        dark = true;
                    }
                }

//                Third icon for Mute and Unmute
                if (position == 2) {
//                    Create a boolean value of Mute - boolean mute = false;
                    if (mute) {
//                        User again clicked on Mute icon
                        player.setVolume(1);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume_off, "Mute"));  // The text will be Mute
                        playbackIconsAdapter.notifyDataSetChanged();
                        mute = false;
                    } else {
//                        When user first clicks on mute we will set the volume to zero using player.setVolume(0).
                        player.setVolume(0);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume, "Unmute"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        mute = true;
                    }
                }

//                Orientation
                if (position == 3) {
//                    If position equal to 3 means our item is on position 4
//                    Check orientation (landscape or portrait) of screen using (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//                        If the screen orientation is portrait we will change it to landscape using (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playbackIconsAdapter.notifyDataSetChanged();
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                        If screen orientation is landscape, change it to portrait
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        playbackIconsAdapter.notifyDataSetChanged();
                    }

                }

//                Volume
                if (position == 4) {
//                    We will control volume using seekbar
//                    First create dialog in java class
//                    Instantiate the VolumeDialog class
//                    Create object for VolumeDialog
                    VolumeDialog volumeDialog = new VolumeDialog();
                    volumeDialog.show(getSupportFragmentManager(), "dialog");
                    playbackIconsAdapter.notifyDataSetChanged();

                }

//                Brightness - we will initialize the BrightnessDialog in VideoPlayerActivity.
                if (position == 5) {

                    BrightnessDialog brightnessDialog = new BrightnessDialog();
                    brightnessDialog.show(getSupportFragmentManager(), "dialog");
                    playbackIconsAdapter.notifyDataSetChanged();
                }

//                Equalizer - we will get default Equalizer of device. We will navigate the user to default equalizer if the device has using Intent.
//                In else statement if device does not have any equalizer we will show a Toast
                if (position == 6) {
                    Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    if ((intent.resolveActivity(getPackageManager()) != null)) {
                        startActivityForResult(intent, 123);
                    } else {
                        Toast.makeText(VideoPlayerActivity.this, "No Equalizer Found", Toast.LENGTH_SHORT).show();
                    }
                    playbackIconsAdapter.notifyDataSetChanged();
                }

//                Playback speed
//                Means user clicked on speed, we will show a dialog for choosing playback speed
//                Pass VideoPlayerActivity.this as context
                if (position == 7) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VideoPlayerActivity.this);
                    alertDialog.setTitle("Select Playback Speed").setPositiveButton("OK", null);
//                    Create Array type String
//                    0.5x is slow speed than normal
                    String[] items = {"0.5x", "1x Normal Speed", "1.25x", "1.5x", "2x"};
//                    -1 because we do not want any of the item to be selected as default
//                    If you want "1x Normal Speed" to be selected as default then we have to pass 1, but as we do not want any of the option to be selected as default that is why we write -1
                    int checkedItem = -1;
                    alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            switch (which) {
                                case 0:
//                                    If user selects "0,5x", we will write code here
//                                    First we have to take the float variable for speed - float speed;
                                    speed = 0.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;

                                case 1:
//                                    If user selects "1x Normal Speed", we will write code here
//                                    If user selects "1x Normal Speed", we will keep the speed to normal
                                    speed = 1f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;

                                case 2:
//                                    If user selects "1.25x", we will write code here
//                                    If user selects "1.25x", we will change speed to 1.25
                                    speed = 1.25f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;

//                                    If user selects 1.25x and 2x we will change it accordingly
                                case 3:
                                    speed = 1.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;

//                                    5th position
                                case 4:
                                    speed = 2f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;

                                default:
                                    break;

                            }
                        }
                    });
                    AlertDialog alert = alertDialog.create();
                    alert.show();
                }

//                Subtitles
                if (position == 8) {
//                    If user clicked on subtitle icon
//                    First we are going to use file picker library for showing the directories
//                    If we write DialogConfigs.MULTI_MODE it will mean user can select more than one file but we do not want user to select more than one file that is why we will use DialogConfigs.SINGLE_MODE
                    dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
//                    Now we will select the extension. Subtitle file will be in .srt extension
                    dialogProperties.extensions = new String[]{".srt"};
//                    Here we write the default path
                    dialogProperties.root = new File("/storage/emulated/0");
                    filePickerDialog.setProperties(dialogProperties);
                    filePickerDialog.show();
//                    After user will select a subtitle file we will write code here
                    filePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {
                            for (String path : files) {
//                                Object of file
                                File file = new File(path);
                                uriSubtitles = Uri.parse(file.getAbsolutePath().toString());
                            }
                            playVideoSubtitles(uriSubtitles);
                        }
                    });
                }
            }
        });

//        Create method for playing the video through Uri
        playVideo();
    }

    private void playVideo() {
        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).setSeekBackIncrementMs(10000).setSeekForwardIncrementMs(10000).build();
//        Now create object for default data source factory
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
//        We will use concatenating media source
        concatenatingMediaSource = new ConcatenatingMediaSource();
//        Create a for loop for playing the video in loop
        for (int i = 0; i < mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);  // The setKeepScreenOn will prevent the screen from dimming after the screen timeout reaches.
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
//        Pass method for showing the error effects of player got while playing video
        playError();
    }

//    Copy the playVideo method above and paste below the same method. Rename it playVideoSubtitles and make some changes
//    This method will be for playing videos with subtitles
    private void playVideoSubtitles(Uri subtitle) {
        long oldPosition = player.getCurrentPosition();
//        Stop the player
        player.stop();

        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).setSeekBackIncrementMs(10000).setSeekForwardIncrementMs(10000).build();
//        Now create object for default data source factory
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
//        We will use concatenating media source
        concatenatingMediaSource = new ConcatenatingMediaSource();
//        Create a for loop for playing the video in loop
        for (int i = 0; i < mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "app");
            MediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory).setTreatLoadErrorsAsEndOfStream(true).createMediaSource(Uri.parse(String.valueOf(subtitle)), textFormat, C.TIME_UNSET);
            MergingMediaSource mergingMediaSource = new MergingMediaSource(mediaSource, subtitleSource);
            concatenatingMediaSource.addMediaSource(mergingMediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);  // The setKeepScreenOn will prevent the screen from dimming after the screen timeout reaches.
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, oldPosition);
//        Pass method for showing the error effects of player got while playing video
        playError();
    }

    //    Check screen orientation and play the video accordingly
//    We pass the screenOrientation method in onCreate
    private void screenOrientation() {
        try {
//            In try we have to check the width and height of video using bitmap
//            First create object for MediaMetadataRetriever
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//            Create object for Bitmap
            Bitmap bitmap;
//            String for getting path
            String path = mVideoFiles.get(position).getPath();
//            Create Uri object
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this, uri);
            bitmap = retriever.getFrameAtTime();

//            Here we will get video width and height in int variables
            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();

//            Now we will check video height and width and change the orientation accordingly
//            If videoWidth is grater than videoHeight we will change the orientation to landscape
            if (videoWidth > videoHeight) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
//                In else we are changing the orientation as portrait
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (Exception e) {
//            If it catches any exception we are going to use Log.e()
            Log.e("MediaMetadataRetriever", "screenOrientation: ");

        }
    }

    private void playError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

//    When user clicks on back button, create if statement below super call for checking if player is playing, then it will stop the player
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying()) {
            player.stop();
        }
    }

//    We will override onPause method - player.setPlayWhenReady() - because when the app pause, the video will also pause through the player.setPlayWhenReady() as false
    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

//    When our app is resumed again we will play the video
    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

//    If you change the orientation of video from portrait to landscape, the audio keeps playing again. So to prevent the app from audio keeps on playing again we will do some code in Manifest.
//    android:configChanges="orientation|screenSize|layoutDirection" - By using this in AndroidManifest.xml, now our audio will not skip again by changing the orientation and changing screen size or layout direction

//    For hiding status bar, we have to create a method below the onRestart method. We have to call the setFullScreen method in onCreate method.
    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /*
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            Pass the id that we want to be clicked
            case R.id.exo_next:
                try {
//                    First the player will stop and position will be incremented. playVideo() will call
                    player.stop();
                    position++;
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "No Next Video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
//                When user clicks on previous button, we will use try catch statement. Position will be decremented because user wants to play previous video.
            case R.id.exo_prev:
                try {
                    player.stop();
                    position--;
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "No Previous Video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
    To complete the onClick listener we have to pass the ids of exo_next and exo_prev above.
    */

    /*
    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.video_back) {
            // When user clicks on back button, we will check if video is playing, we will release the player
            if (player != null) {
                player.release();
            }
            finish();
        } else if (viewId == R.id.lock) {
            // When user will click on lock icon, we will unlock the video and show all controls by using ControlsMode.
            controlsMode = ControlsMode.FULLSCREEN;
            root.setVisibility(View.VISIBLE);
            lock.setVisibility(View.INVISIBLE);  // The icon will be invisible (Part 17 9:07 🤣🤣🤣🤣🤣)
            Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();
        } else if (viewId == R.id.unlock) {
            // When user will first click on unlock button, we will lock the video and hide all the controls.
            controlsMode = ControlsMode.LOCK;  // It will lock the video
            root.setVisibility(View.INVISIBLE);
            lock.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
        } else if (viewId == R.id.exo_next) {
            try {
                player.stop();
                position++;
                playVideo();
            } catch (Exception e) {
                Toast.makeText(this, "No Next Video", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (viewId == R.id.exo_prev) {
            try {
                player.stop();
                position--;
                playVideo();
            } catch (Exception e) {
                Toast.makeText(this, "No Previous Video", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    */

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.video_back) {
            // When user clicks on back button, we will check if video is playing, we will release the player
            if (player != null) {
                player.release();
            }
            finish();
        } else if (viewId == R.id.lock) {
            // When user will click on lock icon, we will unlock the video and show all controls by using ControlsMode.
            controlsMode = ControlsMode.FULLSCREEN;
            root.setVisibility(View.VISIBLE);
            lock.setVisibility(View.INVISIBLE);  // The icon will be invisible (Part 17 9:07 🤣🤣🤣🤣🤣)
            Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();
        } else if (viewId == R.id.unlock) {
            // When user will first click on unlock button, we will lock the video and hide all the controls.
            controlsMode = ControlsMode.LOCK;  // It will lock the video
            root.setVisibility(View.INVISIBLE);
            lock.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
        } else if (viewId == R.id.exo_next) {
            try {
                player.stop();
                position++;
                playVideo();
            } catch (Exception e) {
                Toast.makeText(this, "No Next Video", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (viewId == R.id.exo_prev) {
            try {
                player.stop();
                position--;
                playVideo();
            } catch (Exception e) {
                Toast.makeText(this, "No Previous Video", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            When user clicks on scaling option we will change the video to full screen mode using playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);

            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(secondListener);
        }
    };

    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);

            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };

    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            When user will click on this thirdListener, we will change the video mode to fit by using playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
//            Change the icon
            scaling.setImageResource(R.drawable.fit);

            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
//            When user will again click on it we will call the firstListener
            scaling.setOnClickListener(firstListener);
        }
    };
}

