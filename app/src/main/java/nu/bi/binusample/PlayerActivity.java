package nu.bi.binusample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import nu.bi.binuproxy.http.Http;

public class PlayerActivity extends AppCompatActivity {
    // WARNING: streaming using datafree may be costly.

    private SimpleExoPlayer mPlayer;
    private PlayerView mPlayerView;
    private boolean mPlayWhenReady;
    private int mCurrentWindow = 0;
    private long mPlaybackPosition = 0;

    private String mPlayerUrl;

    // default site for PlayerActivity
    private static final String mDefaultPlayerUrl = "http://us4.internet-radio.com:8266";
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mPlayerView = findViewById(R.id.video_view);

        Intent intent = getIntent();
        mPlayerUrl = intent.hasExtra("site")? intent.getStringExtra("site") : mDefaultPlayerUrl;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        hideSystemUi();
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        // for DASH, replace null with BANDWIDTH_METER
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(null);

        mPlayer = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(adaptiveTrackSelectionFactory),
                new DefaultLoadControl());

        mPlayerView.setPlayer(mPlayer);

        mPlayer.setPlayWhenReady(mPlayWhenReady);
        mPlayer.seekTo(mCurrentWindow, mPlaybackPosition);

        Uri uri = Uri.parse(mPlayerUrl);
        MediaSource mMediaSource = buildMediaSource(uri);
        mPlayer.prepare(mMediaSource, true, false);
    }

    private void releasePlayer() {
        if (mPlayer == null) return;

        mPlaybackPosition = mPlayer.getCurrentPosition();
        mCurrentWindow = mPlayer.getCurrentWindowIndex();
        mPlayWhenReady = mPlayer.getPlayWhenReady();
        mPlayer.release();
        mPlayer = null;
    }

    private MediaSource buildMediaSource(Uri uri) {
        OkHttpDataSourceFactory manifestDataSourceFactory =
                new OkHttpDataSourceFactory(Http.mClient, null, null);
        // direct call without datafree proxy, replace 'Http.mClient' with 'new OkHttpClient()'
        OkHttpDataSourceFactory dataSourceFactory =
                new OkHttpDataSourceFactory(Http.mClient, null, null);

        return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

        // below is for DASH streaming protocol, such as those used by YouTube
//        DashChunkSource.Factory dashChunkSourceFactory =
//                new DefaultDashChunkSource.Factory(dataSourceFactory);
//
//        return new DashMediaSource.Factory(dashChunkSourceFactory,
//                manifestDataSourceFactory).createMediaSource(uri);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        mPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
