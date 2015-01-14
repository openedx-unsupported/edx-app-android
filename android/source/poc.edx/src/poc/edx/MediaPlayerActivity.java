package poc.edx;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.VideoView;

public class MediaPlayerActivity extends Activity {

    private VideoView video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        video = (VideoView) findViewById(R.id.video);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(video);
        video.setMediaController(mediaController);
    }

    @Override
    protected void onStop() {
        super.onStop();

        video.stopPlayback();
    }

    public void onClickPlay(View view) {
        try {
            EditText input = (EditText) findViewById(R.id.input);
            input.setText("http://www.bu.edu/av/bux/sabr101x-module-1a-course-intro.mp4");
            String url = input.getText().toString();

            video.stopPlayback();
            video.setVideoURI(Uri.parse(url));
            video.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void print(String msg) {
        Log.d(getClass().getName(), msg);
    }
}
