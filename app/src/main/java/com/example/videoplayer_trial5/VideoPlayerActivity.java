package com.example.videoplayer_trial5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
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
        videoTitle = getIntent().getStringExtra("video_title");  // Put the key that we are using in videofilesadapter for video title.
//        For getting ArrayList from videofilesadapter we will use Parcelable
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
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

//                Brightness
                if (position == 5) {

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
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
//        Pass method for showing the error effects of player got while playing video
        playError();
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

