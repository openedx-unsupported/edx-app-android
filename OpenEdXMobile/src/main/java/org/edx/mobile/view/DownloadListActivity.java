package org.edx.mobile.view;

import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDbCallback;
import org.edx.mobile.module.db.ObservableDataCallback;
import org.edx.mobile.view.adapters.DownloadEntryAdapter;

import java.util.ArrayList;
import java.util.List;

public class DownloadListActivity extends BaseFragmentActivity {

    public static int REFRESH_INTERVAL_IN_MILLISECONDS = 3000;

    @Nullable
    private DownloadEntryAdapter adapter;

    @NonNull
    private final Handler handler = new Handler();

    @NonNull
    private final ObservableDataCallback<List<DownloadEntryAdapter.Item>> observable = new ObservableDataCallback<>();

    private ListView downloadListView;
    private View loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads_list);
        super.setToolbarAsActionBar();

        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.DOWNLOADS);

        adapter = new DownloadEntryAdapter(this, environment) {
            @Override
            public void onItemClicked(DownloadEntryAdapter.Item model) {
                // nothing to do here
            }

            @Override
            public void onDeleteClicked(DownloadEntryAdapter.Item item) {
                assert adapter != null;
                final VideoModel videoModel = ((DownloadItem) item).model;
                if (environment.getStorage().removeDownload(videoModel) >= 1) {
                    adapter.remove(item);
                }
            }
        };
        loadingIndicator = findViewById(R.id.loading_indicator);
        downloadListView = (ListView) findViewById(R.id.my_downloads_list);
        downloadListView.setAdapter(adapter);

        loadingIndicator.setVisibility(View.VISIBLE);
        downloadListView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        observable.setObserver(new IDbCallback<List<DownloadEntryAdapter.Item>>() {
            @Override
            public void sendResult(List<DownloadEntryAdapter.Item> result) {
                if (result != null) {
                    adapter.setItems(result);
                }
                loadingIndicator.setVisibility(View.GONE);
                downloadListView.setVisibility(View.VISIBLE);
                fetchOngoingDownloadsAfterDelay();
            }

            @Override
            public void sendException(Exception ex) {
                logger.error(ex);
                fetchOngoingDownloadsAfterDelay();
            }

            private void fetchOngoingDownloadsAfterDelay() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fetchOngoingDownloads();
                    }
                }, REFRESH_INTERVAL_IN_MILLISECONDS);
            }
        });
        fetchOngoingDownloads();
    }

    @Override
    protected void onPause() {
        super.onPause();
        observable.setObserver(null);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If this activity was opened from notification
                if (isTaskRoot()) {
                    finish();
                    environment.getRouter().showSplashScreen(this);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If this activity was opened from notification
        if (isTaskRoot()) {
            finish();
            environment.getRouter().showSplashScreen(this);
            return;
        }
        super.onBackPressed();
    }

    private void fetchOngoingDownloads() {
        environment.getDatabase().getListOfOngoingDownloads(new DataCallback<List<VideoModel>>(false) {
            @Override
            public void onResult(List<VideoModel> result) {
                final List<DownloadEntryAdapter.Item> downloadItems = new ArrayList<>(result.size());
                for (VideoModel model : result) {
                    final DownloadEntry downloadEntry = (DownloadEntry) model;
                    final NativeDownloadModel nativeModel = environment.getStorage().getNativeDownload(downloadEntry.dmId);
                    if (null != nativeModel) {
                        downloadItems.add(new DownloadItem(downloadEntry, nativeModel));
                    }
                }
                observable.sendResult(downloadItems);
            }

            @Override
            public void onFail(Exception ex) {
                observable.sendException(ex);
            }
        });
    }

    private static class DownloadItem implements DownloadEntryAdapter.Item {

        @NonNull
        private final DownloadEntry model;

        @NonNull
        private final NativeDownloadModel nativeModel;

        public DownloadItem(@NonNull DownloadEntry model, @NonNull NativeDownloadModel nativeModel) {
            this.model = model;
            this.nativeModel = nativeModel;
        }

        @Override
        @Nullable
        public Long getTotalByteCount() {
            if (model.size <= 0) {
                if (nativeModel.size <= 0) {
                    return null; // Size not known
                }
                return nativeModel.size;
            }
            return model.size;
        }

        @Override
        @NonNull
        public String getTitle() {
            return model.getTitle();
        }

        @Override
        @NonNull
        public String getDuration() {
            return model.getDurationReadable();
        }

        @Override
        public long getDownloadedByteCount() {
            return nativeModel.downloaded;
        }

        @Override
        @NonNull
        public Status getStatus() {
            if (nativeModel.status == DownloadManager.STATUS_FAILED) {
                return Status.FAILED;
            }
            if (nativeModel.status == DownloadManager.STATUS_PENDING
                    || nativeModel.size == -1) {
                return Status.PENDING;
            }
            return Status.DOWNLOADING;
        }

        @Override
        public int getPercent() {
            return nativeModel.getPercentDownloaded();
        }
    }
}
