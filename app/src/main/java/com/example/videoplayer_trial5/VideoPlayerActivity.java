package com.example.videoplayer_trial5;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
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

public class VideoPlayerActivity extends AppCompatActivity {
    //    Now we are getting the video list that we are sending through intent. First create arraylist
    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();

    //    Create object for PlayerView and SimpleExoPlayer with variables as playerView and player
    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    String videoTitle;
    TextView title;
    ConcatenatingMediaSource concatenatingMediaSource;
    
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
//        Allocate memory to playerView
        playerView = findViewById(R.id.exoplayer_view);  // This id exoplayer_view - has been passed in activity_video_player
//        Now we have to get position and arraylist of video from VideoFilesAdapter using Intent
        getSupportActionBar().hide();
        position = getIntent().getIntExtra("position", 1);  // Select getIntExtra because the data type of position is int. In quotes, pass the key that you are using for sending the position.
        videoTitle = getIntent().getStringExtra("video_title");  // Put the key that we are using in videofilesadapter for video title.
//        For getting ArrayList from videofilesadapter we will use Parselable
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        title = findViewById(R.id.video_title);
        title.setText(videoTitle);
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
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }
}