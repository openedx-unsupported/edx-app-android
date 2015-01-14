package org.edx.mobile.player;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.media.MediaCodec.CryptoException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.R;
import com.google.android.exoplayer.VideoSurfaceView;

public class PlayerDemoActivity extends Activity implements
        SurfaceHolder.Callback, ExoPlayer.Listener,
        MediaCodecVideoTrackRenderer.EventListener {

    /**
     * Builds renderers for the player.
     */
    public interface RendererBuilder {
        void buildRenderers(RendererBuilderCallback callback);
    }

    public static final int RENDERER_COUNT = 2;
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;

    private static final String TAG = "PlayerDemoActivity";

    public static final int TYPE_OTHER = 2;

    private EPlayerController mediaController;
    private Handler mainHandler;
    private View shutterView;
    private VideoSurfaceView surfaceView;

    private IEPlayer mPlayer;
    private RendererBuilder builder;
    private RendererBuilderCallback callback;
    private MediaCodecVideoTrackRenderer videoRenderer;

    private boolean autoPlay = true;
    private int playerPosition;
    
    private IVideo video = new IVideo() {
        
        private IClosedCaption selectedClosedCaption;

        @Override
        public String getVideoLink() {
            String contentUri = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";;
            return contentUri;
        }
        
        @Override
        public String getTitle() {
            return "video title here";
        }
        
        @Override
        public String getExternalLink() {
            return "https://www.edx.org/";
        }
        
        @Override
        public List<IClosedCaption> getClosedCaptions() {
            // TODO: use CC URLs here
            List<IClosedCaption> list = new ArrayList<IVideo.IClosedCaption>();
            list.add(new IClosedCaption() {
                
                @Override
                public String getPath() {
                    // TODO: give a file path of srt file
                    return "fake path";
                }
                
                @Override
                public String getLanguage() {
                    return "English";
                }
            });
            return list;
        }

        @Override
        public void setSelectedClosedCaption(IClosedCaption lang) {
            this.selectedClosedCaption = lang;
        }
        
        @Override
        public IClosedCaption getSelectedClosedCaption() {
            return selectedClosedCaption;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler(getMainLooper());
        builder = getRendererBuilder();

        setContentView(R.layout.activity_player_demo);
        ViewGroup root = (ViewGroup) findViewById(R.id.root);
        root.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlsVisibility();
                }
                arg0.performClick();
                return true;
            }
            
        });

        mediaController = new EPlayerController(this);
        mediaController.setAnchorView(root);
        shutterView = findViewById(R.id.shutter);
        surfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Setup the player
        ExoPlayer player = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
        player.addListener(this);
        player.seekTo(playerPosition);
        // Build the player controls
        mPlayer = new EPlayerImpl(player);
        // set the video information
        mPlayer.setVideo(video);
        mediaController.setMediaPlayer(mPlayer);
        mediaController.setEnabled(true);
        // Request the renderers
        callback = new RendererBuilderCallback();
        builder.buildRenderers(callback);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Release the player
        if (mPlayer != null) {
            playerPosition = mPlayer.getCurrentPosition();
            mPlayer.release();
            mPlayer = null;
        }
        callback = null;
        videoRenderer = null;
        shutterView.setVisibility(View.VISIBLE);
    }

    // Public methods

    public Handler getMainHandler() {
        return mainHandler;
    }

    // Internal methods

    private void toggleControlsVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            mediaController.show(0);
        }
    }

    private RendererBuilder getRendererBuilder() {
        return new DefaultRendererBuilder(this, Uri.parse(video.getVideoLink()));
    }

    private void onRenderers(RendererBuilderCallback callback,
            MediaCodecVideoTrackRenderer videoRenderer,
            MediaCodecAudioTrackRenderer audioRenderer) {
        if (this.callback != callback) {
            return;
        }
        this.callback = null;
        this.videoRenderer = videoRenderer;
        mPlayer.getExoPlayer().prepare(videoRenderer, audioRenderer);
        maybeStartPlayback();
    }

    private void maybeStartPlayback() {
        Surface surface = surfaceView.getHolder().getSurface();
        if (videoRenderer == null || surface == null || !surface.isValid()) {
            // We're not ready yet.
            return;
        }
        mPlayer.getExoPlayer().sendMessage(videoRenderer,
                MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        if (autoPlay) {
            mPlayer.start();
            autoPlay = false;
        }
    }

    private void onRenderersError(RendererBuilderCallback callback, Exception e) {
        if (this.callback != callback) {
            return;
        }
        this.callback = null;
        onError(e);
    }

    private void onError(Exception e) {
        Log.e(TAG, "Playback failed", e);
        Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
        finish();
    }

    // ExoPlayer.Listener implementation

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        // Do nothing.
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        onError(e);
    }

    // MediaCodecVideoTrackRenderer.Listener

    @Override
    public void onVideoSizeChanged(int width, int height) {
        surfaceView.setVideoWidthHeightRatio(height == 0 ? 1 : (float) width
                / height);
    }

    @Override
    public void onDrawnToSurface(Surface surface) {
        shutterView.setVisibility(View.GONE);
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
        Log.d(TAG, "Dropped frames: " + count);
    }

    @Override
    public void onDecoderInitializationError(DecoderInitializationException e) {
        // This is for informational purposes only. Do nothing.
    }

    @Override
    public void onCryptoError(CryptoException e) {
        // This is for informational purposes only. Do nothing.
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        maybeStartPlayback();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (videoRenderer != null) {
            mPlayer.getExoPlayer().blockingSendMessage(videoRenderer,
                    MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
        }
    }

    final class RendererBuilderCallback {

        public void onRenderers(MediaCodecVideoTrackRenderer videoRenderer,
                MediaCodecAudioTrackRenderer audioRenderer) {
            PlayerDemoActivity.this.onRenderers(this, videoRenderer,
                    audioRenderer);
        }

        public void onRenderersError(Exception e) {
            PlayerDemoActivity.this.onRenderersError(this, e);
        }

    }
}
