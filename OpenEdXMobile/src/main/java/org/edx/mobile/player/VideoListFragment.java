package org.edx.mobile.player;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.VideoListActivity;
import org.edx.mobile.view.adapters.MyAllVideoAdapter;
import org.edx.mobile.view.adapters.VideoBaseAdapter;
import org.edx.mobile.view.dialog.DeleteVideoDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideoListFragment extends BaseFragment {

    private VideoBaseAdapter<SectionItemInterface> adapter;
    private ListView videoListView;
    private boolean isActivityStarted;
    private static final int MSG_UPDATE_PROGRESS = 1022;
    private DeleteVideoDialogFragment confirmDeleteFragment, downloadSizeExceedDialog;
    private String openInBrowserUrl;
    private String chapterName;
    private EnrolledCoursesResponse enrollment;
    private int playingVideoIndex = -1;
    private VideoListCallback callback;
    private DownloadEntry videoModel;
    private boolean downloadAvailable = false;
    private Button deleteButton;

    @Inject
    TranscriptManager transcriptManager;

    @Inject
    LastAccessManager lastAccessManager;

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    private CourseAPI courseApi;

    private final Logger logger = new Logger(getClass().getName());

    @Inject
    LoginPrefs loginPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_video_list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restore(savedInstanceState);

        Intent extraIntent = getActivity().getIntent();
        if (extraIntent != null) {
            // read incoming enrollment model
            if (enrollment == null) {
                enrollment = (EnrolledCoursesResponse) extraIntent
                        .getSerializableExtra(Router.EXTRA_COURSE_DATA);
            }
        }
        videoListView = (ListView) getView().findViewById(R.id.list_video);

        if (videoListView != null) {
            videoListView.setEmptyView(getView().findViewById(
                    R.id.empty_list_view));
            setAdaptertoVideoList();
        } else {
            // probably the landscape player view, so hide action bar
            ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (bar != null) {
                bar.hide();
            }
        }
    }

    public void setAdaptertoVideoList() {
        showDeletePanel(getView());
        addDataToMyVideoAdapter();
    }

    private void addDataToMyVideoAdapter() {
        try {
            String selectedId = null;
            if (adapter != null) {
                selectedId = adapter.getVideoId();
            }

            adapter = new MyAllVideoAdapter(getActivity(), environment) {
                @Override
                public void onItemClicked(SectionItemInterface model,
                                          int position) {
                    if (!AppConstants.myVideosDeleteMode) {
                        //Check if the model is a DownloadEntry
                        if (model.isDownload()) {
                            DownloadEntry downloadEntry = (DownloadEntry) model;
                            if (downloadEntry.isVideoForWebOnly) {
                                Toast.makeText(getActivity(), R.string.video_only_on_web_short, Toast.LENGTH_SHORT).show();
                            }
                            if (downloadEntry.isDownloaded()) {
                                adapter.setVideoId(downloadEntry.videoId);
                                // hide delete panel first, so that multiple-tap is blocked
                                hideDeletePanel(VideoListFragment.this.getView());
                                showPlayer();

                                // initialize index for this model
                                playingVideoIndex = position;
                                play(model);
                                notifyAdapter();
                            }
                        }
                    }
                }

                @Override
                public void onSelectItem() {
                    handleCheckboxSelection();
                }
            };
            if (selectedId != null) {
                adapter.setVideoId(selectedId);
            }

            videoListView.setAdapter(adapter);
            videoListView.setOnItemClickListener(adapter);

            final CourseEntry course = enrollment.getCourse();
            setActivityTitle(course.getName());
            environment.getAnalyticsRegistry().trackScreenView(
                    Analytics.Screens.MY_VIDEOS_COURSE_VIDEOS, course.getId(), null);

            ArrayList<SectionItemInterface> list = environment.getStorage()
                    .getSortedOrganizedVideosByCourse(course.getId());
            downloadAvailable = false;
            if (list == null || list.size() == 0) {
                hideDeletePanel(getView());
                downloadAvailable = false;
            } else {
                downloadAvailable = true;
            }

            for (SectionItemInterface m : list) {
                adapter.add(m);
            }
            notifyAdapter();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * This function sets text to the title in Action Bar
     *
     * @param title
     */
    private void setActivityTitle(String title) {
        try {
            getActivity().setTitle(title);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void play(SectionItemInterface model) {
        if (model instanceof DownloadEntry) {
            DownloadEntry v = (DownloadEntry) model;
            try {
                final VideoResponseModel vrm = courseApi.getVideoById(v.eid, v.videoId);
                lastAccessManager.setLastAccessed(v.eid, vrm.getSection().getId());

                // capture chapter name
                if (chapterName == null) {
                    // capture the chapter name of this video
                    chapterName = v.chapter;
                }

                if (callback != null) {
                    videoModel = v;
                    callback.playVideoModel(v);
                    adapter.setVideoId(videoModel.videoId);
                    adapter.setSelectedPosition(playingVideoIndex);
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                logger.error(e);
            }

        }
    }

    private void showPlayer() {
        hideDeletePanel(getView());
        adapter.setIsPlayerOn(true);
        adapter.setSelectedPosition(playingVideoIndex);
        adapter.notifyDataSetChanged();
        try {
            if (getView() != null) {
                View container = getView().findViewById(R.id.container_player);
                if (container != null) {
                    container.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.warn("Error in showing player");
        }
    }

    public void handleDeleteView() {
        if (isPlayerVisible()) {
            hideDeletePanel(getView());
        } else {
            if (downloadAvailable) {
                showDeletePanel(getView());
            }
        }
    }

    private void hideDeletePanel(View view) {
        try {
            View v = view.findViewById(R.id.delete_button_panel);
            if (v != null) {
                v.setVisibility(View.GONE);
            }

            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
                        // hide checkbox in action bar
                        ((VideoListActivity) getActivity()).hideCheckBox();

                        // hide checkboxes in list
                        notifyAdapter();
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }
            }, 300);

        } catch (Exception ex) {
            logger.warn("Error in hiding delete button Panel");
            logger.error(ex);
        }
    }

    private void showDeletePanel(View view) {
        try {
            if (isPlayerVisible()) {
                hideDeletePanel(view);
            } else {
                view.findViewById(R.id.delete_button_panel).setVisibility(
                        View.VISIBLE);

                deleteButton = (Button) view.findViewById(
                        R.id.delete_btn);
                final Button editButton = (Button) view.findViewById(
                        R.id.edit_btn);
                final Button cancelButton = (Button) view.findViewById(
                        R.id.cancel_btn);

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
                        ArrayList<SectionItemInterface> list;
                        list = adapter.getSelectedItems();

                        if (list != null && list.size() > 0) {
                            // Confirmation Dialog before deleting Videos
                            showConfirmDeleteDialog(list.size());
                        }
                    }
                });

                cancelButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        editButton.setVisibility(View.VISIBLE);
                        AppConstants.myVideosDeleteMode = false;
                        if (getActivity() instanceof VideoListActivity) {
                            ((VideoListActivity) getActivity()).hideCheckBox();
                        }
                        adapter.unselectAll();

                        videoListView.setOnItemClickListener(adapter);
                        adapter.setSelectedPosition(playingVideoIndex);
                        adapter.notifyDataSetChanged();
                        deleteButton.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);
                    }
                });

                editButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        editButton.setVisibility(View.GONE);

                        AppConstants.myVideosDeleteMode = true;
                        adapter.setSelectedPosition(playingVideoIndex);
                        adapter.notifyDataSetChanged();

                        disableDeleteButton();
                        deleteButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        if (getActivity() instanceof VideoListActivity) {
                            ((VideoListActivity) getActivity()).showCheckBox();
                        }
                    }
                });
            }

        } catch (Exception ex) {
            logger.warn("Error in showing delete panel");
            logger.error(ex);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        isActivityStarted = false;
        AppConstants.videoListDeleteMode = false;
        hideConfirmDeleteDialog();
        adapter.unselectAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.setIsPlayerOn(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        isActivityStarted = true;
    }

    public void setAllVideosChecked() {
        adapter.selectAll();
        int count = adapter.getSelectedVideoItemsCount();

        logger.debug("Video Count of selected videos" + count);
        enableDeleteButton();
        adapter.setSelectedPosition(playingVideoIndex);
        adapter.notifyDataSetChanged();
    }

    public void unsetAllVideosChecked() {
        adapter.unselectAll();
        disableDeleteButton();
        adapter.setSelectedPosition(playingVideoIndex);
        adapter.notifyDataSetChanged();
    }

    private final Handler handler = new Handler();

    public void markPlaying() {
        environment.getStorage().markVideoPlaying(videoModel, watchedStateCallback);
    }

    /**
     * This method inserts the Download Entry Model in the database
     * Called when a user clicks on a Video in the list
     *
     * @param v - Download Entry object
     */
    public void addVideoDatatoDb(final DownloadEntry v) {
        try {
            if (v != null) {
                environment.getDatabase().addVideoData(v, new DataCallback<Long>() {
                    @Override
                    public void onResult(Long result) {
                        if (result != -1) {
                            logger.debug("Video entry inserted" + v.videoId);
                        }
                    }

                    @Override
                    public void onFail(Exception ex) {
                        logger.error(ex);
                    }
                });
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void saveCurrentPlaybackPosition(int offset) {
        try {
            DownloadEntry v = videoModel;
            if (v != null) {
                // mark this as partially watches, as playing has started
                environment.getDatabase().updateVideoLastPlayedOffset(v.videoId, offset,
                        setCurrentPositionCallback);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void onPlaybackComplete() {
        try {
            DownloadEntry v = videoModel;
            if (v == null) {
                v = (DownloadEntry) adapter.getItem(playingVideoIndex);
            }

            if (v != null && v.watched == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                videoModel.watched = DownloadEntry.WatchedState.WATCHED;
                // mark this as partially watches, as playing has started
                environment.getDatabase().updateVideoWatchedState(v.videoId, DownloadEntry.WatchedState.WATCHED,
                        watchedStateCallback);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void onConfirmDelete() {
        ArrayList<SectionItemInterface> list;
        int deletedVideoCount = 0;
        list = adapter.getSelectedItems();
        if (list != null) {
            for (SectionItemInterface section : list) {
                if (section.isDownload()) {
                    DownloadEntry de = (DownloadEntry) section;
                    environment.getStorage().removeDownload(de);
                    deletedVideoCount++;
                }
            }
        }
        addDataToMyVideoAdapter();
        adapter.setSelectedPosition(playingVideoIndex);
        adapter.notifyDataSetChanged();
        ((VideoListActivity) getActivity()).hideCheckBox();
        AppConstants.myVideosDeleteMode = false;
        videoListView.setOnItemClickListener(adapter);
        if (adapter.getCount() <= 0) {
            finishActivity();
        }

        if (deletedVideoCount > 0) {
            try {
                String format = ResourceUtil.getFormattedStringForQuantity(getResources(), R.plurals.deleted_video,
                        "video_count", deletedVideoCount).toString();

                ((VideoListActivity) getActivity())
                        .showInfoMessage(String.format(format, deletedVideoCount));
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        getView().findViewById(R.id.delete_btn).setVisibility(View.GONE);
        getView().findViewById(R.id.edit_btn).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.cancel_btn).setVisibility(View.GONE);
    }


    private void finishActivity() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isActivityStarted) {
                    getActivity().finish();
                }
            }
        }, 300);
    }

    private void handleCheckboxSelection() {
        int selectedItemsNo = adapter.getSelectedVideoItemsCount();
        int totalVideos = adapter.getTotalVideoItemsCount();

        if (selectedItemsNo == 0) {
            disableDeleteButton();
        } else {
            enableDeleteButton();
        }

        if (selectedItemsNo == totalVideos) {
            ((VideoListActivity) getActivity()).setSelectAllChecked(true);
        } else if (selectedItemsNo < totalVideos) {
            ((VideoListActivity) getActivity()).setSelectAllChecked(false);
        }
    }

    protected void showConfirmDeleteDialog(int itemCount) {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", getString(R.string.delete_dialog_title_help));
        dialogMap.put("yes_button", getString(R.string.label_delete));
        dialogMap.put("no_button", getString(R.string.label_cancel));
        dialogMap.put("message_1", getResources().getQuantityString(R.plurals.delete_video_dialog_msg, itemCount));

        confirmDeleteFragment = DeleteVideoDialogFragment.newInstance(dialogMap,
                new IDialogCallback() {

                    @Override
                    public void onPositiveClicked() {
                        onConfirmDelete();
                    }

                    @Override
                    public void onNegativeClicked() {
                        confirmDeleteFragment.dismiss();
                    }
                });
        confirmDeleteFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        confirmDeleteFragment.show(getFragmentManager(), "dialog");
        confirmDeleteFragment.setCancelable(false);
    }

    protected void hideConfirmDeleteDialog() {
        try {
            if (confirmDeleteFragment != null) {
                confirmDeleteFragment.dismiss();
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }


    private boolean isPlayerVisible() {
        if (getActivity() == null) {
            return false;
        }
        View container = getActivity().findViewById(R.id.container_player);
        return (container != null && container.getVisibility() == View.VISIBLE);
    }

    public void notifyAdapter() {
        adapter.setSelectedPosition(playingVideoIndex);
        adapter.notifyDataSetChanged();
    }

    public void setCallback(VideoListCallback callback) {
        this.callback = callback;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("playingVideoIndex", playingVideoIndex);
        outState.putSerializable("model", videoModel);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            playingVideoIndex = savedInstanceState.getInt("playingVideoIndex", -1);
            videoModel = (DownloadEntry) savedInstanceState.getSerializable("model");
        }
    }

    public static interface VideoListCallback {
        public void playVideoModel(DownloadEntry video);
    }

    private void disableDeleteButton() {
        deleteButton.setEnabled(false);
    }

    private void enableDeleteButton() {
        deleteButton.setEnabled(true);

    }

    public void playNext() {
        try {
            //playingVideoIndex++;
            int videoPos = adapter.getPositionByVideoId(videoModel.videoId);
            if (videoPos != -1) {
                // check next playable video entry
                videoPos++;
                while (videoPos < adapter.getCount()) {
                    SectionItemInterface i = adapter.getItem(videoPos);
                    if (i != null && i instanceof DownloadEntry) {
                        if (!NetworkUtil.isConnected(getActivity())) {
                            DownloadEntry de = (DownloadEntry) i;
                            if (de.isDownloaded()) {
                                if (callback != null) {
                                    videoModel = de;
                                    playingVideoIndex = videoPos;
                                    adapter.setSelectedPosition(playingVideoIndex);
                                    adapter.setVideoId(videoModel.videoId);
                                    callback.playVideoModel(videoModel);
                                    break;
                                }
                            }
                        } else {
                            if (callback != null) {
                                videoModel = (DownloadEntry) i;
                                playingVideoIndex = videoPos;
                                adapter.setVideoId(videoModel.videoId);
                                adapter.setSelectedPosition(playingVideoIndex);
                                callback.playVideoModel(videoModel);
                                break;
                            }
                        }
                    }
                    // try next
                    videoPos++;
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    public boolean hasNextVideo() {
        try {
            if (videoModel != null) {
                int videoPos = adapter.getPositionByVideoId(videoModel.videoId);
                if (videoPos != -1) {
                    for (int i = (videoPos + 1); i < adapter.getCount(); i++) {
                        SectionItemInterface d = adapter.getItem(i);
                        if (d != null && d instanceof DownloadEntry) {
                            DownloadEntry de = (DownloadEntry) d;
                            if (!NetworkUtil.isConnected(getActivity())) {
                                if (de.isDownloaded()) {
                                    return true;
                                }
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public void playPrevious() {
        try {
            //playingVideoIndex--;
            int videoPos = adapter.getPositionByVideoId(videoModel.videoId);
            if (videoPos != -1) {
                // check next playable video entry
                videoPos--;
                while (videoPos >= 0) {
                    SectionItemInterface i = adapter.getItem(videoPos);
                    if (i != null && i instanceof DownloadEntry) {
                        if (!NetworkUtil.isConnected(getActivity())) {
                            DownloadEntry de = (DownloadEntry) i;
                            if (de.isDownloaded()) {
                                if (callback != null) {
                                    videoModel = de;
                                    adapter.setVideoId(videoModel.videoId);
                                    playingVideoIndex = videoPos;
                                    adapter.setSelectedPosition(playingVideoIndex);
                                    callback.playVideoModel(videoModel);
                                    break;
                                }
                            }
                        } else {
                            if (callback != null) {
                                videoModel = (DownloadEntry) i;
                                adapter.setVideoId(videoModel.videoId);
                                playingVideoIndex = videoPos;
                                adapter.setSelectedPosition(playingVideoIndex);
                                callback.playVideoModel(videoModel);
                                break;
                            }
                        }
                    }
                    videoPos--;
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public boolean hasPreviousVideo() {
        try {
            if (videoModel != null) {
                int videoPos = adapter.getPositionByVideoId(videoModel.videoId);
                if (videoPos != -1) {
                    for (int i = (videoPos - 1); i >= 0; i--) {
                        SectionItemInterface d = adapter.getItem(i);
                        if (d != null && d instanceof DownloadEntry) {
                            DownloadEntry de = (DownloadEntry) d;
                            if (!NetworkUtil.isConnected(getActivity())) {
                                if (de.isDownloaded()) {
                                    return true;
                                }
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    public View.OnClickListener getNextListener() {
        if (hasNextVideo()) {
            return new NextClickListener();
        }
        return null;
    }

    public View.OnClickListener getPreviousListener() {
        if (hasPreviousVideo()) {
            return new PreviousClickListener();
        }
        return null;
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
}
