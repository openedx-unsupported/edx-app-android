package org.edx.mobile.view;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.view.adapters.DownloadEntryAdapter;

import java.util.List;

public class DownloadListActivity extends BaseFragmentActivity {

    private DownloadEntryAdapter adapter;
    private View offlineBar;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads_list);

        environment.getSegment().trackScreenView(ISegment.Screens.DOWNLOADS);

        offlineBar = findViewById(R.id.offline_bar);

        adapter = new DownloadEntryAdapter(this, environment) {

            @Override
            public void onItemClicked(DownloadEntry model) {
                // nothing to do here
            }

            @Override
            public void onDeleteClicked(DownloadEntry model) {
                if (environment.getStorage().removeDownload(model) >= 1) {
                    // update the list data as one download is removed
                    adapter.remove(model);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        final ListView downloadListView = (ListView) findViewById(R.id.my_downloads_list);
        downloadListView.setAdapter(adapter);
    }

    private void observeOngoingDownloads() {
        environment.getDatabase().getListOfOngoingDownloads(new DataCallback<List<VideoModel>>() {
            @Override
            public void onResult(final List<VideoModel> result) {
                if (result != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setItems((List) result);
                        }
                    });
                }
            }

            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                observeOngoingDownloads();
            }
        }, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        observeOngoingDownloads();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
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
