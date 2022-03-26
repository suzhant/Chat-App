package com.sushant.whatsapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.sushant.whatsapp.databinding.ActivityFullScreenVideoBinding;

public class FullScreenVideo extends AppCompatActivity {
    ActivityFullScreenVideoBinding binding;
    String videoUrl;
    ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullScreenVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        videoUrl = getIntent().getStringExtra("videoUrl");
        initializeExoPlayer(Uri.parse(videoUrl));

    }

    private void initializeExoPlayer(Uri video) {
        // Create a data source factory.
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);

        // Create a progressive media source pointing to a stream uri.
        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true);
        ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                .createMediaSource(MediaItem.fromUri(video));
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(this).build();

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .setBandwidthMeter(bandwidthMeter)
                .setTrackSelector(trackSelector)
                .build();
        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        player.setMediaSource(mediaSource);
        binding.exoPlayer.setPlayer(player);
        player.setPlayWhenReady(false);
        player.prepare();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        player.stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        player.stop();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        player.stop();
        super.onStop();
    }
}