package org.edx.mobile.view;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ListView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.view.adapters.DownloadEntryAdapter;

import java.util.ArrayList;
import java.util.List;

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

        environment.getSegment().trackScreenView(ISegment.Screens.DOWNLOADS);

        offlineBar = findViewById(R.id.offline_bar);

        ListView downloadListView = (ListView) findViewById(R.id.my_downloads_list);
        adapter = new DownloadEntryAdapter(this, environment) {

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
                if(environment.getStorage().removeDownload(model) >= 1){
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

        adapter.setStore(environment.getStorage());
        downloadListView.setAdapter(adapter);
        final ArrayList<DownloadEntry> list = new ArrayList<DownloadEntry>();
        environment.getDatabase().getListOfOngoingDownloads(new DataCallback<List<VideoModel>>() {
            @Override
            public void onResult(List<VideoModel> result) {
                if(result!=null){
                    for(VideoModel de : result){
                        list.add((DownloadEntry) de);
                    }
                    adapter.setItems(list);
                }
            }
            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            ActionBar bar = getSupportActionBar();
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
    }


    @Override
    protected void onOffline() {
        super.onOffline();
        offlineBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onOnline() {
        super.onOnline();
        offlineBar.setVisibility(View.GONE);
    }

}
