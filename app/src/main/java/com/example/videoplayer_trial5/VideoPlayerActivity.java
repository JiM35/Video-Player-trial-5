package com.example.videoplayer_trial5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
    private ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK, FULLSCREEN;
    }

    ImageView videoBack, lock, unlock, scaling;
//    Create object for RelativeLayout, take the variable as root
    RelativeLayout root;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextButton, previousButton;

    @SuppressLint("MissingInflatedId")
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
        root = findViewById(R.id.root_layout);

        title.setText(videoTitle);

        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        scaling.setOnClickListener();

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
//    android:configChanges="orientation|screenSize|layoutDirection" - By using this in AndroidManifest.xml, now our audio will not skip again by changing the orientation and changing screensize or layout direction

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_back:
//                When user clicks on back button, we will check if video is playing, we will release the player
                if (player != null) {
                    player.release();
                }
                finish();
                break;

            case R.id.lock:
//                When user will click on lock icon, we will unlock the video and show all controls by using ControlsMode.
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);  // The icon will be invisible (Part 17 9:07 🤣🤣🤣🤣🤣)
                Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlock:
//                When user will first click on unlock button, we will lock the video and hide all the controls.
                controlsMode = ControlsMode.LOCK;  // It will lock the video
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
                break;

        }
        int viewId = view.getId();
        if (viewId == R.id.exo_next) {
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
}

