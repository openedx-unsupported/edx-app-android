package poc.edx;

import java.io.File;
import java.io.FileInputStream;

import poc.edx.stream.VideoDownloader;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

public class VideoPlayerActivity extends FragmentActivity implements
        SurfaceHolder.Callback, VideoDownloader.IDownloadCallback {

    // public static final String URL_VIDEO =
    // "https://youtube.com/v/h2p20wDbr_Y&fs=1&autoplay=1&playerMode=normal&rel=0";
    public static final String URL_VIDEO = "http://haignet.co.uk/html5-video-element-test.mp4";
    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    private VideoDownloader streamer;
    private final Handler handler = new Handler();
    private int offsetPlayer = 0;
    private File videoFile;
    private int lastPercent;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_video_player);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        mediaPlayer = new MediaPlayer();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    public void onClickPlay(View view) {
        try {
            play(URL_VIDEO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void play(String urlVideo) throws Exception {
        streamer = new VideoDownloader(urlVideo, new File(getCacheDir(),
                "test.mp4"));
        streamer.setCallback(this);
        streamer.start();
    }

    private void stop() throws Exception {
        mediaPlayer.stop();
        print("media player stopped");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        print("surface changed");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
        print("surface created");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        print("surface destroyed");
    }

    private void print(String msg) {
        Log.d(getClass().getName(), msg);
    }

    @Override
    public void onReceived(byte[] buffer, int length, int progressPercentage) {
        // print("received bytes % = " + progressPercentage);
    }

    @Override
    public void onError(File destFile, String error) {
        print("error:" + error);
    }

    @Override
    public void onFinished(File destFile) {
        print("download finished");
        // mediaPlayer.setOnCompletionListener(null);
        handler.post(streamUpdated);
    }

    @Override
    public void onStarted(File destFile) {
        print("download started");
        lastPercent = 0;
        videoFile = destFile;
    }

    private void play(File file, int offset) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer player) {
                    offsetPlayer = player.getCurrentPosition();
                    if (lastPercent < 100) {
                        handler.post(streamUpdated);
                    }
                }
            });
            mediaPlayer.setOnErrorListener(new OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer player, int what, int extra) {
                    offsetPlayer = player.getCurrentPosition();
                    handler.post(streamUpdated);
                    return false;
                }
            });
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            FileInputStream in = new FileInputStream(file);
            mediaPlayer.setDataSource(in.getFD());
            in.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.seekTo(offset);

            print("seek=" + offset + " , duration=" + mediaPlayer.getDuration());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onProgress(int percent) {
        if (percent > lastPercent) {
            lastPercent = percent;
            showProgress(lastPercent);

            if (lastPercent % 2 == 0) {
                handler.post(streamUpdated);
            }
        }
    }

    private void showProgress(int progress) {
        ProgressBar pb = (ProgressBar) findViewById(R.id.progress);
        pb.setProgress(progress);
    }

    private Runnable streamUpdated = new Runnable() {
        @Override
        public void run() {
            try {
                if (mediaPlayer.isPlaying()) {
                    print("currently playing");
                    return;
                }

                play(videoFile, offsetPlayer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
}
