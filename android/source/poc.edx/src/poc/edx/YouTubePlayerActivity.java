package poc.edx;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerFragment;

public class YouTubePlayerActivity extends Activity implements OnInitializedListener {
    
    public static final String API_KEY = "AIzaSyD4B3QolbJJffhm9O4Srp7F7txoS8ZFGfg";
//  public static final String VIDEO_ID = "RyMzGMpLpjk";
    private YouTubePlayerFragment fragment;
    private YouTubePlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        fragment = (YouTubePlayerFragment) getFragmentManager()
                .findFragmentById(R.id.fragment);
        fragment.initialize(API_KEY, this);
        
        findViewById(R.id.btn_play).setEnabled(false);
    }
    
    public void onClickPlay(View view) {
        if (player != null) {
            EditText input = (EditText) findViewById(R.id.input);
            String videoId = input.getText().toString();
            
            player.cueVideo(videoId);
        } else {
            print("player NOT initialized");
        }
    }

    @Override
    public void onInitializationFailure(Provider p,
            YouTubeInitializationResult r) {
        print("initialization failure");
        r.getErrorDialog(this, 0).show();
    }

    @Override
    public void onInitializationSuccess(Provider arg0, YouTubePlayer player,
            boolean wasRestored) {
        print("initialization success");
        
        this.player = player;
        findViewById(R.id.btn_play).setEnabled(true);
    }
    
    private void print(String msg) {
        Log.d(getClass().getName(), msg);
    }

}
