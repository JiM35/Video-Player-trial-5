package com.example.videoplayer_trial5;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bullhead.equalizer.EqualizerFragment;
import com.bullhead.equalizer.Settings;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

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

//    Create object for picture in picture
    PictureInPictureParams.Builder pictureInPicture;

    boolean isCrossChecked;
//    Create object for FrameLayout
    FrameLayout eqContainer;

//    Create int variables for device height and width, brightness
//    Swipe and zoom variables
    private int device_height, device_width, brightness, media_volume;
    boolean start = false;
//    Create left and right variables
    boolean left, right;
//    Create base x and y float variables
    private float baseX, baseY;
    boolean swipe_move = false;
//    Create long variable for difference X and difference Y
    private long diffX, diffY;
    public static final int MINIMUM_DISTANCE = 100;
    boolean success = false;
//    Initialize TextView
    TextView vol_text, brt_text, total_duration;
    ProgressBar vol_progress, brt_progress;
    LinearLayout vol_progress_container, vol_text_container, brt_progress_container, brt_text_container;
    ImageView vol_icon, brt_icon;
    AudioManager audioManager;
//    Create object for ContentResolver
    private ContentResolver contentResolver;
    private Window window;
    boolean singleTap = false;
//    Initialize RelativeLayout for zoom_layout we created in exo_layout_view
    RelativeLayout zoomLayout;
//    Also RelativeLayout for zoom container that we have created in swipe_zoom_design
    RelativeLayout zoomContainer;
    TextView zoom_percentage;
//    Create object for scale gesture detector
    ScaleGestureDetector scaleGestureDetector;
//    Create a scale factor variable - the scale factor value is 1.0f i.e. 100% by default
    private float scale_factor = 1.0f;
//    Create boolean variable
    boolean double_tap = false;
//    Initialize RelativeLayout
    RelativeLayout double_tap_play_pause;

//    Swipe and zoom variables

    private ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK, FULLSCREEN
    }

//    Create object for ImageView
//    Create variable for video playlist - name it as videoList
    ImageView videoBack, lock, unlock, scaling, videoList;

//    Create object for VideoFilesAdapter
    VideoFilesAdapter videoFilesAdapter;
//    Create object for RelativeLayout, take the variable as root
    RelativeLayout root;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextButton, previousButton;

    @SuppressLint({"NotifyDataSetChanged", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
//        Now we have to get position and arraylist of video from VideoFilesAdapter using Intent
        getSupportActionBar().hide();
        hideBottomBar();
//        Allocate memory to playerView
        playerView = findViewById(R.id.exoplayer_view);  // This id exoplayer_view - has been passed in activity_video_player
        position = getIntent().getIntExtra("position", 1);  // Select getIntExtra because the data type of position is int. In quotes, pass the key that you are using for sending the position.
        videoTitle = getIntent().getStringExtra("video_title");  // Put the key that we are using in VideoFilesAdapter for video title.
//        For getting ArrayList from VideoFilesAdapter we will use Parcelable
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        screenOrientation();

//        Organise the code using methods -
        initViews();

//        Create method for playing the video through Uri
        playVideo();

//        We first check the device width and height - use DisplayMetrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        device_width = displayMetrics.widthPixels;
        device_height = displayMetrics.heightPixels;

//        Initialize the OnTouchListener class
        playerView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
//                    ACTION_DOWN will be called when user taps on the screen
                    case MotionEvent.ACTION_DOWN:
//                        When user is starting the swipe we will show all the controllers of player view
                        playerView.showController();
                        start = true;
                        if (motionEvent.getX() < device_width / 2) {
                            left = true;
                            right = false;
//                            We are dividing the screen width into 2 parts
                        } else {
//                        } else if (motionEvent.getX() > (device_width / 2)) {
//                            We will take the right side as true and left will be false means user will click on right side of the screen
                            left = false;
                            right = true;
                        }
                        baseX = motionEvent.getX();
                        baseY = motionEvent.getY();
                        break;

//                    ACTION_MOVE will be called when user swipes on the screen
                    case MotionEvent.ACTION_MOVE:
                        swipe_move = true;
                        diffX = (long) Math.ceil(motionEvent.getX() - baseX);
                        diffY = (long) Math.ceil(motionEvent.getY() - baseY);
                        double brightnessSpeed = 0.01;
                        if (Math.abs(diffY) > MINIMUM_DISTANCE) {
                            start = true;
                            if (Math.abs(diffY) > Math.abs(diffX)) {
                                boolean value;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                    Check if the user can write or not
                                    value = android.provider.Settings.System.canWrite(getApplicationContext());
                                    if (value) {
//                                        If the user has access to write then we will check if its left side then we will show a Toast
//                                        When the user swipes on the left side...
                                        if (left) {
                                            /* Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show(); */
                                            contentResolver = getContentResolver();
                                            window = getWindow();
                                            try {
                                                android.provider.Settings.System.putInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                                brightness = android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS);
                                            } catch (
                                                    android.provider.Settings.SettingNotFoundException e) {
                                                throw new RuntimeException(e);
                                            }
                                            int new_brightness = (int) (brightness - (diffY * brightnessSpeed));
//                                            250 is the total percentage of brightness
                                            if (new_brightness > 250) {
                                                new_brightness = 250;
                                            } else if (new_brightness < 1) {
                                                new_brightness = 1;
                                            }
//                                            We will show the brightness in 100 not 250
                                            double brt_percentage = Math.ceil((((double) new_brightness / (double) 250) * (double) 100));
                                            brt_progress_container.setVisibility(View.VISIBLE);
                                            brt_text_container.setVisibility(View.VISIBLE);
                                            brt_progress.setProgress((int) brt_percentage);

//                                            If the brightness is less than 30, we will show the icon ic_brightness_low
                                            if (brt_percentage < 30) {
//                                                Create two icons for brightness - ic_brightness_low and ic_brightness_moderate
                                                brt_icon.setImageResource(R.drawable.ic_brightness_low);
                                            } else if (brt_percentage > 30 && brt_percentage < 80) {
//                                                If brightness percentage is greater than 30 and less than 80, the the brightness icon will be ic_brightness_moderate
                                                brt_icon.setImageResource(R.drawable.ic_brightness_moderate);
                                            } else if (brt_percentage > 80) {
//                                                If the brightness percentage is greater than 80, we will show ic_brightness_2 - the icon for full brightness
                                                brt_icon.setImageResource(R.drawable.ic_brightness_2);
                                            }
//                                            We will change the percentage for brightness
                                            brt_text.setText(" " + (int) brt_percentage + "%");
                                            android.provider.Settings.System.putInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS, (new_brightness));
                                            WindowManager.LayoutParams layoutParams = window.getAttributes();
                                            layoutParams.screenBrightness = brightness / (float) 255;
                                            window.setAttributes(layoutParams);

//                                            If it is right side, then Toast will be right swipe
//                                            In the right swipe we will implement the volume swipe
                                        } else if (right) {
                                            /* Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show(); */
                                            vol_text_container.setVisibility(View.VISIBLE);
                                            media_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                                            We will get maximum volume by using audioManager.getStreamMaxVolume()
                                            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                            double cal = (double) diffY * ((double) maxVol / ((double) (device_height * 2) - brightnessSpeed));
                                            int newMediaVolume = media_volume - (int) cal;
                                            if (newMediaVolume > maxVol) {
                                                newMediaVolume = maxVol;
                                            } else if (newMediaVolume < 1) {
                                                newMediaVolume = 0;
                                            }
                                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//                                            Create double variable for percentage volume - we will show percentage volume in textview
                                            double volPer = Math.ceil(((double) newMediaVolume / (double) maxVol) * (double) 100);
                                            vol_text.setText(" " + (int) volPer + "%");
//                                            Check if volume percentage is less than 1, then volume icon will be ic_volume_off
                                            if (volPer < 1) {
                                                vol_icon.setImageResource(R.drawable.ic_volume_off);
                                                vol_text.setVisibility(View.VISIBLE);
                                                vol_text.setText("Off");
                                            } else if (volPer >= 1) {
                                                vol_icon.setImageResource(R.drawable.ic_volume);
                                                vol_text.setVisibility(View.VISIBLE);
                                            }
                                            vol_progress_container.setVisibility(View.VISIBLE);
                                            vol_progress.setProgress((int) volPer);
                                        }
                                        success = true;
                                    } else {
//                                We will check if the user has right access, then we will show this Toast in else if, if user does not have right access we will navigate the user to setting screen for allowing access
//                                Here we will navigate the user to setting screen
                                        Toast.makeText(getApplicationContext(), "Allow write settings for swipe controls", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
//                                        First parameter will be intent, second we will take request code as 111
                                        startActivityForResult(intent, 111);
                                    }
                                }
                            }
                        }
                        break;

//                    ACTION_UP will be called when user removes his finger from the screen
                    case MotionEvent.ACTION_UP:
//                        When user removes his finger we will take the swipe_move variable as false, also start variable as false
                        swipe_move = false;
                        start = false;
//                        When user removes finger from screen, then we will change the visibility to gone for vol_progress_container
                        vol_progress_container.setVisibility(View.GONE);
                        brt_progress_container.setVisibility(View.GONE);
                        vol_text_container.setVisibility(View.GONE);
                        brt_text_container.setVisibility(View.GONE);
                        break;
                }
//                Call scaleGestureDetector in playerView.setOnTouchListener - most important for zoom layout
                scaleGestureDetector.onTouchEvent(motionEvent);
                return super.onTouch(view, motionEvent);
            }

            @Override
            public void onDoubleTouch() {
                super.onDoubleTouch();
                if (double_tap) {
//                    When user double taps on the screen this if statement will play the video
                    player.setPlayWhenReady(true);  // Play the video
                    double_tap_play_pause.setVisibility(View.GONE);  // Hide the double_tap_play_pause design
                    double_tap = false;
//                When for the first time the user double taps on the screen, the else statement will call
                } else {
                    player.setPlayWhenReady(false);  // This will pause the video
                    double_tap_play_pause.setVisibility(View.VISIBLE);
                    double_tap = true;
                }
            }

            @Override
            public void onSingleTouch() {
                super.onSingleTouch();
                if (singleTap) {
//                    First when user taps on the screen we will show controllers
                    playerView.showController();
                    singleTap = false;
                } else {
//                    Then hide all the controllers in second tap
                    playerView.hideController();
                    singleTap = true;
                }
//                Check if double tap is visible, then on single click double_tap_play_pause, visibility will be gone
                if (double_tap_play_pause.getVisibility() == View.VISIBLE) {
                    double_tap_play_pause.setVisibility(View.GONE);
                }
            }
        });

        horizontalIconList();
    }

    private void initViews() {
        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);
        total_duration = findViewById(R.id.exo_duration);
        title = findViewById(R.id.video_title);
//        Declare the RelativeLayout with id root_layout in custom_playback_view.xml.
//        Allocate memory by using id.
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
//        Allocate memory to scaling
        scaling = findViewById(R.id.scaling);
        double milliseconds = Double.parseDouble(mVideoFiles.get(position).getDuration());
        total_duration.setText(Utility.timeConversion((long) milliseconds));

//        Initialize the file picker objects for showing directories when user clicks on subtitles
        dialogProperties = new DialogProperties();
        filePickerDialog = new FilePickerDialog(VideoPlayerActivity.this);
        filePickerDialog.setTitle("Select a Subtitle File");
//        Set positive and negative buttons
        filePickerDialog.setPositiveBtnName("OK");
        filePickerDialog.setNegativeBtnName("Cancel");
//        Initialize the variable - pictureInPicture
//        If device is greater than android 8, then we are going to show picture in picture mode because this picture in picture mode is available in android 8 - Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPicture = new PictureInPictureParams.Builder();
        }

        root = findViewById(R.id.root_layout);
//        Give id to night mode
        nightMode = findViewById(R.id.night_mode);
//        Give the id to videoList - for showing list of videos in a folder (playlist)
        videoList = findViewById(R.id.video_list);
//        Allocate memory to recyclerview
        recyclerViewIcons = findViewById(R.id.recyclerview_icons);
        eqContainer = findViewById(R.id.eqFrame);

//        Initialize all the views
        vol_text = findViewById(R.id.vol_text);
        brt_text = findViewById(R.id.brt_text);

        vol_progress = findViewById(R.id.vol_progress);
        brt_progress = findViewById(R.id.brt_progress);

        vol_progress_container = findViewById(R.id.vol_progress_container);
        brt_progress_container = findViewById(R.id.brt_progress_container);

        vol_text_container = findViewById(R.id.vol_text_container);
        brt_text_container = findViewById(R.id.brt_text_container);

        vol_icon = findViewById(R.id.vol_icon);
        brt_icon = findViewById(R.id.brt_icon);

//        Initialize views
        zoomLayout = findViewById(R.id.zoom_layout);
        zoom_percentage = findViewById(R.id.zoom_percentage);
        zoomContainer = findViewById(R.id.zoom_container);
        double_tap_play_pause = findViewById(R.id.double_tap_play_pause);

//        Initialize the scaleGestureDetector. The first parameter is context, second parameter is class we will create - ScaleDetector
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleDetector());

//        Initialize audioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        title.setText(videoTitle);

        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
//        Set setOnClickListener on videoList (for showing playlist)
        videoList.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);  // Pass the View name
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

//    We will work on swipe


    private void horizontalIconList() {
//        Initialize the recyclerview - first we will add the items in list
        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
        iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_pip_mode, "Popup"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
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
                        iconModelArrayList.add(new IconModel(R.drawable.ic_pip_mode, "Popup"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
//                        When user first clicks on right icon, this else code will execute
//                        We have to expand the list and add the icons in recyclerview.
                        if (iconModelArrayList.size() == 5) {
                            iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_volume, "Volume"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_brightness_2, "Brightness"));
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

//                Icon three for popup
//                TODO: This Third icon was for Mute and Unmute
                if (position == 2) {
/*
                    Open AndroidManifest.xml file and add these attributes android:configChanges="screenLayout|smallestScreenSize|keyboardHidden|uiMode"
                    More attributes will be android:excludeFromRecents="true" - This attribute will prevent the app from showing multiple times in recent apps of device
                    android:excludeFromRecents="true"
                    android:exported="true"
                    android:resizeableActivity="true"
                    android:supportsPictureInPicture="true"
*/

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        We take aspect ratio for showing picture in picture mode, show it in Rational
//                        The default aspect ratio for showing pip mode is 16:9
                        Rational aspectRatio = new Rational(16, 9);
                        pictureInPicture.setAspectRatio(aspectRatio);
//                        Initialize the method enterPictureInPictureMode
                        enterPictureInPictureMode(pictureInPicture.build());
                    } else {
                        Log.wtf("not oreo", "yes");
                    }
                }

//                Equalizer
//                When user clicks on position 3, we will open equalizer
                if (position == 3) {
//                    Check if visibility is gone, we will set the visibility of equalizer as visible
                    if (eqContainer.getVisibility() == View.GONE) {
                        eqContainer.setVisibility(View.VISIBLE);
                    }
//                    Get session id
                    final int sessionId = player.getAudioSessionId();
//                    Create object of Settings - choose com.bullhead.equalizer
                    Settings.isEditing = false;
//                    Create object for equalizer fragment
                    EqualizerFragment equalizerFragment = EqualizerFragment.newBuilder().setAccentColor(Color.parseColor(String.valueOf("#1A78F2"))).setAudioSessionId(sessionId).build();
                    getSupportFragmentManager().beginTransaction().replace(R.id.eqFrame, equalizerFragment).commit();
                    playbackIconsAdapter.notifyDataSetChanged();
                }

//                Rotate
//                TODO: This icon was for Volume
                if (position == 4) {
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

//                Mute
//                TODO: This icon was for Brightness - we will initialize the BrightnessDialog in VideoPlayerActivity.
                if (position == 5) {
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

//                Volume
//                TODO: This icon was for Equalizer - we will get default Equalizer of device. We will navigate the user to default equalizer if the device has using Intent.
//                In else statement if device does not have any equalizer we will show a Toast
                if (position == 6) {
//                    We will control volume using seekbar
//                    First create dialog in java class
//                    Instantiate the VolumeDialog class
//                    Create object for VolumeDialog
                    VolumeDialog volumeDialog = new VolumeDialog();
                    volumeDialog.show(getSupportFragmentManager(), "dialog");
                    playbackIconsAdapter.notifyDataSetChanged();
                }

//                Brightness
//                TODO: Playback speed - Means user clicked on speed, we will show a dialog for choosing playback speed
//                Pass VideoPlayerActivity.this as context
                if (position == 7) {
                    BrightnessDialog brightnessDialog = new BrightnessDialog();
                    brightnessDialog.show(getSupportFragmentManager(), "dialog");
                    playbackIconsAdapter.notifyDataSetChanged();
                }

//                Playback speed
//                TODO: Subtitles
                if (position == 8) {
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
                if (position == 9) {
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
//            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
//            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "app");
//            MediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory).setTreatLoadErrorsAsEndOfStream(true).createMediaSource(Uri.parse(String.valueOf(subtitle)), textFormat, C.TIME_UNSET);
//            MergingMediaSource mergingMediaSource = new MergingMediaSource(mediaSource, subtitleSource);
//            concatenatingMediaSource.addMediaSource(mergingMediaSource);
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.eqFrame);
//        If by clicking onBackPressed if equalizer visibility is gone, then we will use the super call
        if (eqContainer.getVisibility() == View.GONE) {
            super.onBackPressed();
        } else {
            if (fragment.isVisible() && eqContainer.getVisibility() == View.VISIBLE) {
                eqContainer.setVisibility(View.GONE);
            } else {
                if (player != null) {
                    player.release();
                }
                super.onBackPressed();
            }
        }
    }

    //    We will override onPause method - player.setPlayWhenReady() - because when the app pause, the video will also pause through the player.setPlayWhenReady() as false
//    The video is in picture in picture mode but is paused. We have to work on onPause method
    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
//        If the video is in picture in picture mode, then we will play the video
        if (isInPictureInPictureMode()) {
            player.setPlayWhenReady(true);
        } else {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
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

    public void hideBottomBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View view = this.getWindow().getDecorView();
            view.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decodeView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decodeView.setSystemUiVisibility(uiOptions);
        }
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
//            When user clicks on back button, we will check if video is playing, we will release the player
            if (player != null) {
                player.release();
            }
            finish();

        } else if (viewId == R.id.video_list) {
//            Initialize the video list class, create video list class and name it as PlayListDialog
//            Instantiate the PlayListDialog in VideoPlayerActivity
            PlayListDialog playListDialog = new PlayListDialog(mVideoFiles, videoFilesAdapter);
            playListDialog.show(getSupportFragmentManager(), playListDialog.getTag());
            finish();

        } else if (viewId == R.id.lock) {
//            When user will click on lock icon, we will unlock the video and show all controls by using ControlsMode.
            controlsMode = ControlsMode.FULLSCREEN;
            root.setVisibility(View.VISIBLE);
            lock.setVisibility(View.INVISIBLE);  // The icon will be invisible (Part 17 9:07 🤣🤣🤣🤣🤣)
            Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();

        } else if (viewId == R.id.unlock) {
//            When user will first click on unlock button, we will lock the video and hide all the controls.
            controlsMode = ControlsMode.LOCK;  // It will lock the video
            root.setVisibility(View.INVISIBLE);
            lock.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();

        } else if (viewId == R.id.exo_next) {
//            When user clicks on exo_next, we will also change the display name of video
            try {
                player.stop();
                position++;
                playVideo();
                title.setText(mVideoFiles.get(position).getDisplayName());
            } catch (Exception e) {
                Toast.makeText(this, "No Next Video", Toast.LENGTH_SHORT).show();
                finish();
            }

        } else if (viewId == R.id.exo_prev) {
//            When user clicks on previous button we will also change the display name of video
            try {
                player.stop();
                position--;
                playVideo();
                title.setText(mVideoFiles.get(position).getDisplayName());
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

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        isCrossChecked = isInPictureInPictureMode;
        if (isInPictureInPictureMode) {
//            If the video is in picture in picture mode then we will hide all controls by the line - playerView.hideController();
            playerView.hideController();
        } else {
//            Or else we will show the controls if user press on close button of picture in picture mode and play the video in full screen
            playerView.showController();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        If user clicks on close button
        if (isCrossChecked) {
            player.release();
            finish();
        }
    }

    //    We used the request code 111, we will override the method in onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        If requestCode is 111, then we take a boolean value
        if (requestCode == 111) {
            boolean value;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                value = android.provider.Settings.System.canWrite(getApplicationContext());
//                Check if user has write access
                if (value) {
                    success = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Not Granted (Swipe to Control Brightness & Volume)", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

//    Call the ScaleDetector class in initViews();
    private class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            scale_factor *= detector.getScaleFactor();

//            6.0f will be the maximum percentage for swiping - 6.0 will be 600%
//            When user changes the zoom layout the minimum percentage for zoom is 50% and maximum 600%
//            If you want the minimum to be 40% or 30%, then you can change it to 0.4f or 0.3f
            scale_factor = Math.max(0.5f, Math.min(scale_factor, 6.0f));

            zoomLayout.setScaleX(scale_factor);
            zoomLayout.setScaleY(scale_factor);
//            Create the variable for percentage
            int percentage = (int) (scale_factor * 100);
            zoom_percentage.setText(" " + percentage + "%");
            zoomContainer.setVisibility(View.VISIBLE);

//            When user pinch the screen for Zoom we will hide all the swipe controls
            brt_text_container.setVisibility(View.GONE);
            vol_text_container.setVisibility(View.GONE);
            brt_progress_container.setVisibility(View.GONE);
            vol_progress_container.setVisibility(View.GONE);

            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            zoomContainer.setVisibility(View.GONE);
            super.onScaleEnd(detector);
        }
    }
}

