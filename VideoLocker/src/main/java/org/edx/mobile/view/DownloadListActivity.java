package org.edx.mobile.view;

import java.util.ArrayList;
import java.util.List;

import org.edx.mobile.R;
import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.view.adapters.DownloadEntryAdapter;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DownloadListActivity extends BaseFragmentActivity {

    private static final int MSG_UPDATE_PROGRESS = 1022;
    private DownloadEntryAdapter adapter;
    private View offlineBar;
    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) { 
            if (msg.what == MSG_UPDATE_PROGRESS) {
                if (isActivityStarted()) {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        logger.debug("download list reloaded");
                    }
                    sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 3000);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads_list);

        handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 0);

        try{
            segIO.screenViewsTracking(getString
                    (R.string.title_download));
        }catch(Exception e){
            logger.error(e);
        }


        offlineBar = (View) findViewById(R.id.offline_bar);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            if (offlineBar != null) 
                offlineBar.setVisibility(View.VISIBLE);
        }

        ListView downloadListView = (ListView) findViewById(R.id.my_downloads_list);
        adapter = new DownloadEntryAdapter(this) {

            @Override
            public void onItemClicked(DownloadEntry model) {
                // nothing to do here
            }

            @Override
            public void onDownloadComplete(DownloadEntry model) {
                //showDownloadCompleteView();
                adapter.remove(model);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onDeleteClicked(DownloadEntry model) {
                if(storage.removeDownload(model) >= 1){
                    // update the list data as one download is removed
                    try{
                        adapter.remove(model);
                        adapter.notifyDataSetChanged();
                    }catch(Exception e){
                        logger.error(e);
                    }

                }
            }
        };

        adapter.setStore(storage);
        downloadListView.setAdapter(adapter);
        final ArrayList<DownloadEntry> list = new ArrayList<DownloadEntry>();
        db.getListOfOngoingDownloads(new DataCallback<List<IVideoModel>>() {
            @Override
            public void onResult(List<IVideoModel> result) {
                if(result!=null){
                    for(IVideoModel de : result){
                        list.add((DownloadEntry) de);
                    }
                    adapter.setItems(list);
                    showDownloadCompleteView();
                }
            }
            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
            }
        });
    }



    private void showDownloadCompleteView(){

        if(db!=null){
            long downloadedCount = db.getVideosDownloadedCount(null);
            // display count of downloaded videos
            PrefManager p = new PrefManager(DownloadListActivity.this,
                    PrefManager.Pref.LOGIN);

            // user specific data is stored in his own file
            ProfileModel profile = p.getCurrentUserProfile();
            p = new PrefManager(DownloadListActivity.this, profile.username);

            long count = p.getLong(PrefManager.Key.COUNT_OF_VIDEOS_DOWNLOADED);

            if(count>downloadedCount){
                count = downloadedCount;
            }

            if (count > 0) {
                findViewById(R.id.download_header).setVisibility(View.VISIBLE);
                TextView tvCount = (TextView) findViewById(R.id.text_download_msg_cnt);
                tvCount.setText(String.valueOf(count));

                TextView tvCountMsg = (TextView) findViewById(R.id.text_download_msg);
                if (count ==1) {
                    tvCountMsg.setText(getString(R.string.text_download_msg_singular));
                } else {
                    tvCountMsg.setText(getString(R.string.text_download_msg_plural));
                }

                Button view_btnView = (Button) findViewById(R.id.button_view);
                view_btnView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent myDownloadIntent = new Intent(DownloadListActivity.this,
                                MyVideosTabActivity.class);
                        myDownloadIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity( myDownloadIntent);
                        finish();
                    }
                });
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            ActionBar bar = getActionBar();
            if (bar != null) {
                bar.show();
            }
            setTitle(getString(R.string.title_download));
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 0);
        invalidateOptionsMenu();
    };


    @Override
    protected void onOffline() {
        AppConstants.offline_flag = true;
        offlineBar.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    @Override
    protected void onOnline() {
        AppConstants.offline_flag = false;
        offlineBar.setVisibility(View.GONE);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // inflate menu from xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem menuItem = menu.findItem(R.id.progress_download);
        menuItem.setVisible(false);

        MenuItem offline_tvItem = menu.findItem(R.id.offline);
        if (AppConstants.offline_flag) {
            offline_tvItem.setVisible(true);
        } else {
            offline_tvItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.progress_download);
        menuItem.setVisible(false);

        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        checkBox_menuItem.setVisible(false);

        return true;
    }

}