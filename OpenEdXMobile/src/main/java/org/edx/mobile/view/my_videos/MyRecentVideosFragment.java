package org.edx.mobile.view.my_videos;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.interfaces.NetworkSubject;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.storage.DownloadCompletedEvent;
import org.edx.mobile.module.storage.DownloadedVideoDeletedEvent;
import org.edx.mobile.player.IPlayerEventCallback;
import org.edx.mobile.player.PlayerFragment;
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.task.GetRecentDownloadedVideosTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.CheckboxDrawableUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.adapters.MyRecentVideoAdapter;
import org.edx.mobile.view.dialog.DeleteVideoDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class MyRecentVideosFragment extends BaseFragment
        implements IPlayerEventCallback, Analytics.OnEventListener {

    private MyRecentVideoAdapter adapter;
    private ListView videoListView;
    private PlayerFragment playerFragment;
    private DeleteVideoDialogFragment deleteDialogFragment;
    private int playingVideoIndex = -1;
    private DownloadEntry videoModel;
    private Button deleteButton;
    private MenuItem selectAllMenuItem;
    private final Logger logger = new Logger(getClass().getName());
    private GetRecentDownloadedVideosTask getRecentDownloadedVideosTask;

    @Inject
    LastAccessManager lastAccessManager;

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    private CourseAPI courseApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(!isLandscape());
        EventBus.getDefault().register(this);
        if (getUserVisibleHint()) {
            fireScreenEvent();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_video_list_with_player_container, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            restore(savedInstanceState);
        }

        getView().findViewById(R.id.container_player).setVisibility(
                playerFragment == null ? View.GONE : View.VISIBLE);

        videoListView = (ListView) getView().findViewById(R.id.list_video);

        if (videoListView != null) {
            adapter = new MyRecentVideoAdapter(getActivity(), environment) {

                @Override
                protected void onItemClick(SectionItemInterface model, int position) {
                    showPlayer();
                    // initialize index for this model
                    playingVideoIndex = position;
                    videoModel = (DownloadEntry) model;
                    playVideoModel();
                    notifyAdapter();
                }

                @Override
                public void onSelectItem() {
                    int selectedItemsCount = adapter.getSelectedVideoItemsCount();
                    int totalVideos = adapter.getTotalVideoItemsCount();
                    deleteButton.setEnabled(selectedItemsCount > 0);
                    setSelectAllChecked(selectedItemsCount == totalVideos);
                }
            };
            if (videoModel != null) {
                adapter.setVideoId(videoModel.videoId);
            }
            adapter.setSelectedPosition(playingVideoIndex);
            videoListView.setEmptyView(getView().findViewById(R.id.empty_list_view));
            videoListView.setAdapter(adapter);

            showDeletePanel(getView());

            videoListView.setOnItemClickListener(adapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showDeletePanel(getView());
        setDeleteMode(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        addToRecentAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (getRecentDownloadedVideosTask != null) {
            getRecentDownloadedVideosTask.cancel(true);
            getRecentDownloadedVideosTask = null;
        }
        hideConfirmDeleteDialog();
        AppConstants.myVideosDeleteMode = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (playerFragment != null) {
            ((NetworkSubject) getActivity()).unregisterNetworkObserver(playerFragment);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (playerFragment != null) {
            playerFragment.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                playerFragment.unlockOrientation();
            } else {
                playerFragment.lockOrientation();
            }
        }
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        selectAllMenuItem = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (AppConstants.myVideosDeleteMode) {
            inflater.inflate(R.menu.video_list, menu);
            selectAllMenuItem = menu.findItem(R.id.delete_checkbox);
            setSelectAllChecked(adapter.getSelectedVideoItemsCount() == adapter.getTotalVideoItemsCount());
        }
    }

    private void setSelectAllChecked(boolean isChecked) {
        selectAllMenuItem.setChecked(isChecked);
        selectAllMenuItem.setIcon(CheckboxDrawableUtil.createActionBarDrawable(getActivity(), isChecked));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_checkbox: {
                selectAllMenuItem.setChecked(!selectAllMenuItem.isChecked());
                if (selectAllMenuItem.isChecked()) {
                    adapter.selectAll();
                } else {
                    adapter.unselectAll();
                }
                notifyAdapter();
                deleteButton.setEnabled(selectAllMenuItem.isChecked());
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void setDeleteMode(boolean inDeleteMode) {
        AppConstants.myVideosDeleteMode = inDeleteMode;
        getActivity().supportInvalidateOptionsMenu();
    }

    private void addToRecentAdapter() {
        if (adapter == null) {
            return;
        }
        if (getRecentDownloadedVideosTask != null) {
            getRecentDownloadedVideosTask.cancel(true);
        }
        logger.debug("MyRecentVideoAdapter reloading");
        final String selectedId = adapter.getVideoId();
        getRecentDownloadedVideosTask = new GetRecentDownloadedVideosTask(getActivity()) {
            @Override
            protected void onSuccess(List<SectionItemInterface> list) throws Exception {
                super.onSuccess(list);
                if (list != null) {
                    adapter.clear();
                    adapter.addAll(list);
                    logger.debug("MyRecentVideoAdapter reloaded.");
                }

                if (adapter.getCount() <= 0) {
                    hideDeletePanel(getView());
                } else {
                    showDeletePanel(getView());
                }

                videoListView.setOnItemClickListener(adapter);
                if (selectedId != null) {
                    adapter.setVideoId(selectedId);
                }
                notifyAdapter();
                // Refresh the previous and next buttons visibility on the video
                // player if a video is playing, based on the new data set.
                if (playerFragment != null) {
                    playerFragment.setNextPreviousListeners(getNextListener(), getPreviousListener());
                }
            }
        };
        getRecentDownloadedVideosTask.execute();
    }

    private void playVideoModel() {
        if (playerFragment == null) {
            return;
        }

        if (playerFragment.isPlaying() && videoModel.getVideoId().equals(
                playerFragment.getPlayingVideo().getVideoId())) {
            logger.debug("this video is already being played, skipping play event");
            return;
        }

        VideoResponseModel vrm;
        try {
            vrm = courseApi.getVideoById(videoModel.eid, videoModel.videoId);
        } catch (Exception e) {
            logger.error(e);
            return;
        }
        lastAccessManager.setLastAccessed(videoModel.eid, vrm.getSection().getId());

        // reload this model
        environment.getStorage().reloadDownloadEntry(videoModel);

        logger.debug("Resumed= " + playerFragment.isResumed());

        TranscriptModel transcript = null;
        try {
            transcript = courseApi.getTranscriptsOfVideo(videoModel.eid, videoModel.videoId);
        } catch (Exception e) {
            logger.error(e);
        }

        String filepath = null;
        // check if file available on local
        if (!TextUtils.isEmpty(videoModel.filepath)) {
            if (videoModel.isDownloaded()) {
                File f = new File(videoModel.filepath);
                if (f.exists()) {
                    // play from local
                    filepath = videoModel.filepath;
                    logger.debug("Playing from local file");
                }
            }
        } else {
            DownloadEntry de = (DownloadEntry) environment.getDatabase()
                    .getIVideoModelByVideoUrl(videoModel.url, null);
            if (de != null && de.filepath != null) {
                File f = new File(de.filepath);
                if (f.exists()) {
                    // play from local
                    filepath = de.filepath;
                    logger.debug("Playing from local file for " +
                            "another Download Entry");
                }
            }
        }

        if (filepath == null || filepath.length() <= 0) {
            // not available on local, so play online
            logger.warn("Local file path not available");
            filepath = videoModel.getBestEncodingUrl(getContext());
        }

        playerFragment.play(filepath, videoModel.lastPlayedOffset,
                videoModel.getTitle(), transcript, videoModel);

        adapter.setVideoId(this.videoModel.videoId);
    }

    private void destroyPlayer() {
        if (playerFragment == null) {
            return;
        }
        showDeletePanel(getView());
        playerFragment.lockOrientation();
        getChildFragmentManager().beginTransaction().remove(playerFragment).commitAllowingStateLoss();
        View container = getView().findViewById(R.id.container_player);
        container.setVisibility(View.GONE);
        playerFragment = null;
    }

    private void showPlayer() {
        if (playerFragment != null) {
            return;
        }

        hideDeletePanel(getView());

        View container = getView().findViewById(R.id.container_player);
        if (container.getVisibility() != View.VISIBLE) {
            container.setVisibility(View.VISIBLE);
        }

        // add and display player fragment
        playerFragment = new PlayerFragment();
        // set callback for player events
        playerFragment.setCallback(this);
        playerFragment.setNextPreviousListeners(getNextListener(), getPreviousListener());
        FragmentManager childManager = getChildFragmentManager();
        childManager.beginTransaction().add(R.id.container_player, playerFragment).commit();
        // the fragment needs to be added immediately in order to be playable
        childManager.executePendingTransactions();
        ((NetworkSubject) getActivity()).registerNetworkObserver(playerFragment);
        playerFragment.unlockOrientation();
    }


    private void hideDeletePanel(View view) {
        // hide delete button panel at bottom
        view.findViewById(R.id.delete_button_panel).setVisibility(View.GONE);

        // hide checkbox in action bar
        setDeleteMode(false);

        // hide checkboxes in list
        notifyAdapter();
    }

    private void showDeletePanel(View view) {
        if (playerFragment != null) {
            return;
        }

        LinearLayout deletePanel = (LinearLayout) view
                .findViewById(R.id.delete_button_panel);
        deletePanel.setVisibility(View.VISIBLE);

        deleteButton = (Button) view
                .findViewById(R.id.delete_btn);
        final Button editButton = (Button) view
                .findViewById(R.id.edit_btn);
        editButton.setVisibility(View.VISIBLE);
        final Button cancelButton = (Button) view
                .findViewById(R.id.cancel_btn);

        if (AppConstants.myVideosDeleteMode) {
            deleteButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
        }


        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<SectionItemInterface> list = adapter
                        .getSelectedItems();
                if (list != null && list.size() > 0) {
                    showConfirmDeleteDialog(list.size());
                }
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editButton.setVisibility(View.VISIBLE);
                videoListView.setOnItemClickListener(adapter);
                setDeleteMode(false);
                adapter.unselectAll();
                notifyAdapter();
                deleteButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
            }
        });

        editButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                editButton.setVisibility(View.GONE);
                setDeleteMode(true);
                notifyAdapter();
                videoListView.setOnItemClickListener(null);
                deleteButton.setEnabled(false);
                deleteButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        });
    }

    protected void showConfirmDeleteDialog(int itemCount) {
        Map<String, String> dialogMap = new HashMap<>();
        dialogMap.put("title", getString(R.string.delete_dialog_title_help));
        dialogMap.put("message_1", getResources().getQuantityString(R.plurals.delete_video_dialog_msg, itemCount));
        dialogMap.put("yes_button", getString(R.string.label_delete));
        dialogMap.put("no_button", getString(R.string.label_cancel));
        deleteDialogFragment = DeleteVideoDialogFragment.newInstance(dialogMap,
                new IDialogCallback() {
                    @Override
                    public void onPositiveClicked() {
                        onConfirmDelete();
                        deleteDialogFragment.dismiss();
                    }

                    @Override
                    public void onNegativeClicked() {
                        deleteDialogFragment.dismiss();
                    }
                });
        deleteDialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        deleteDialogFragment.show(getFragmentManager(), "dialog");
        deleteDialogFragment.setCancelable(false);
    }

    protected void hideConfirmDeleteDialog() {
        if (deleteDialogFragment != null) {
            deleteDialogFragment.dismissAllowingStateLoss();
        }
    }

    //Deleting Downloaded videos on getting confirmation
    private void onConfirmDelete() {
        int deletedVideoCount = 0;
        ArrayList<SectionItemInterface> list = adapter.getSelectedItems();
        if (list != null) {
            for (SectionItemInterface section : list) {
                if (section.isDownload()) {
                    // TODO The removeDownloads() triggers a callback upon video deletion.
                    // Would be better if removeDownloads() could take a list of videos to delete.
                    DownloadEntry de = (DownloadEntry) section;
                    environment.getStorage().removeDownload(de);
                    deletedVideoCount++;
                    // Although the adapter is refreshed below, we update the adapter here to
                    // prevent a user from being able to click a deleted video while the adapter is
                    // refreshing. 
                    adapter.remove(section);
                }
            }
        }
        // Although the videos are removed from the adapter above, the section the videos are in
        // is not available so we refresh the adapter here.
        addToRecentAdapter();
        notifyAdapter();
        videoListView.setOnItemClickListener(adapter);
        setDeleteMode(false);
        if (deletedVideoCount > 0) {
            UiUtil.showMessage(getView(), ResourceUtil.getFormattedStringForQuantity(getResources(),
                    R.plurals.deleted_video, "video_count", deletedVideoCount).toString());
        }
        getView().findViewById(R.id.delete_btn).setVisibility(View.GONE);
        getView().findViewById(R.id.edit_btn).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.cancel_btn).setVisibility(View.GONE);
    }


    @Override
    public void onError() {
    }

    @Override
    public void onPlaybackStarted() {
        environment.getStorage().markVideoPlaying(videoModel, watchedStateCallback);
        notifyAdapter();
    }

    @Override
    public void saveCurrentPlaybackPosition(int offset) {
        DownloadEntry v = videoModel;
        if (v != null) {
            // mark this as partially watches, as playing has started
            environment.getDatabase().updateVideoLastPlayedOffset(v.videoId, offset,
                    setCurrentPositionCallback);
        }
        notifyAdapter();
    }

    @Override
    public void onPlaybackComplete() {
        DownloadEntry v = videoModel;
        if (v != null) {
            if (v.watched == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                videoModel.watched = DownloadEntry.WatchedState.WATCHED;
                // mark this as partially watches, as playing has started
                environment.getDatabase().updateVideoWatchedState(v.videoId, DownloadEntry.WatchedState.WATCHED,
                        watchedStateCallback);
            }
        }
        notifyAdapter();
    }

    private void notifyAdapter() {
        if (adapter == null) {
            return;
        }
        adapter.setSelectedPosition(playingVideoIndex);
        adapter.notifyDataSetChanged();
    }

    /**
     * @return true if current orientation is LANDSCAPE, false otherwise.
     */
    protected boolean isLandscape() {
        return (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        logger.debug("In onSaveInstance State");
        outState.putInt("playingVideoIndex", playingVideoIndex);
        outState.putSerializable("model", videoModel);
        super.onSaveInstanceState(outState);
    }

    /**
     * Container tab will call this method to restore the saved data
     */
    protected void restore(Bundle savedInstanceState) {
        playingVideoIndex = savedInstanceState.getInt("playingVideoIndex", -1);
        videoModel = (DownloadEntry) savedInstanceState.getSerializable("model");
        playerFragment = (PlayerFragment) getChildFragmentManager()
                .findFragmentById(R.id.container_player);
        if (playerFragment != null) {
            playerFragment.setCallback(this);
            ((NetworkSubject) getActivity()).registerNetworkObserver(playerFragment);
        }
    }

    //Play the nextVideo if user selects next from Dialog
    private void playNext() {
        playingVideoIndex++;
        // check next playable video entry
        while (playingVideoIndex < adapter.getCount()) {
            SectionItemInterface i = adapter.getItem(playingVideoIndex);
            if (i != null && i instanceof DownloadEntry) {
                videoModel = (DownloadEntry) i;
                adapter.setSelectedPosition(playingVideoIndex);
                adapter.setVideoId(videoModel.videoId);
                playVideoModel();
                break;
            }
            // try next
            playingVideoIndex++;
        }
    }

    //Check if next video is available in the Video List
    private boolean hasNextVideo(int index) {
        if (index == -1) {
            index = playingVideoIndex;
        }
        for (int i = index + 1; i < adapter.getCount(); i++) {
            SectionItemInterface d = adapter.getItem(i);
            if (d != null && d instanceof DownloadEntry) {
                return true;
            }
        }
        return false;
    }

    private void playPrevious() {
        playingVideoIndex--;
        // check next playable video entry
        while (playingVideoIndex >= 0) {
            SectionItemInterface i = adapter.getItem(playingVideoIndex);
            if (i != null && i instanceof DownloadEntry) {
                videoModel = (DownloadEntry) i;
                adapter.setSelectedPosition(playingVideoIndex);
                adapter.setVideoId(videoModel.videoId);
                playVideoModel();
                break;
            }
            // try next
            playingVideoIndex--;
        }
    }

    private boolean hasPreviousVideo(int playingIndex) {
        for (int i = playingIndex - 1; i >= 0; i--) {
            SectionItemInterface d = adapter.getItem(i);
            if (d != null && d instanceof DownloadEntry) {
                return true;
            }
        }
        return false;
    }

    private View.OnClickListener getNextListener() {
        if (hasNextVideo(playingVideoIndex)) {
            return new NextClickListener();
        }
        return null;
    }

    private View.OnClickListener getPreviousListener() {
        if (hasPreviousVideo(playingVideoIndex)) {
            return new PreviousClickListener();
        }
        return null;
    }

    @Override
    public void fireScreenEvent() {
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.MY_VIDEOS_RECENT);
    }

    private class NextClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            playNext();
        }
    }

    private class PreviousClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            playPrevious();
        }
    }

    private DataCallback<Integer> watchedStateCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
            logger.debug("Watched State Updated");
        }

        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };

    private DataCallback<Integer> setCurrentPositionCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
            logger.debug("Current Playback Position Updated");
        }

        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };

    public void onEventMainThread(DownloadCompletedEvent e) {
        addToRecentAdapter();
    }

    public void onEventMainThread(DownloadedVideoDeletedEvent e) {
        destroyPlayer();
    }

}
