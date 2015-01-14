package poc.edx;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class NativeDownloadManagerActivity extends Activity {

//  public static final String URL_VIDEO = "http://haignet.co.uk/html5-video-element-test.mp4";
    private long id;
    private BroadcastReceiver onComplete = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            reloadDownloads();
            status("Download complete!");
        }
        
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_dm);
        
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        
        reloadDownloads();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

    public void onStartDownload(View view) {
        EditText input = (EditText) findViewById(R.id.input);
        String url = input.getText().toString();
        
        Request request = new Request(Uri.parse(url));
        request.setDestinationInExternalFilesDir(this,
                Environment.DIRECTORY_MOVIES, System.currentTimeMillis()
                        + ".mp4");

        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        id = dm.enqueue(request);

        status("Download started");
    }

    public void onStopDownload(View view) {
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        int count = dm.remove(id);

        status("Download removed");
    }

    private void status(String text) {
        TextView status = (TextView) findViewById(R.id.status);
        status.setText(text);
    }

    private void reloadDownloads() {
        try {
            String[] files = getExternalFilesDir(Environment.DIRECTORY_MOVIES).list();
            
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, files);
            
            ListView list = (ListView) findViewById(R.id.list);
            list.setAdapter(adapter);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
