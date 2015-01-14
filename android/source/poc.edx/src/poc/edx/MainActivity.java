package poc.edx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity implements OnItemClickListener {

    private String[] demos = new String[] { "Video Playback",
            "Native Downloader",
            "Media Player - Progressive",
            "YouTube Player",
            "JPlayer"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView list = new ListView(this);
        setContentView(list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, demos);
        list.setAdapter(adapter);

        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        if (position == 0) {
            Intent i = new Intent(this, VideoPlayerActivity.class);
            startActivity(i);
        } else if (position == 1) {
            Intent i = new Intent(this, NativeDownloadManagerActivity.class);
            startActivity(i);
        }  else if (position == 2) {
            Intent i = new Intent(this, MediaPlayerActivity.class);
            startActivity(i);
        }  else if (position == 3) {
            Intent i = new Intent(this, YouTubePlayerActivity.class);
            startActivity(i);
        }  else if (position == 4) {
//          Intent i = new Intent(this, JPlayerActivity.class);
//          startActivity(i);
        } 
    }
}
