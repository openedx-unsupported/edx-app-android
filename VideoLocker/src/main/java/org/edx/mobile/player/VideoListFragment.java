package org.edx.mobile.player;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.Api;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.task.CircularProgressTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.VideoListActivity;
import org.edx.mobile.view.adapters.MyAllVideoAdapter;
import org.edx.mobile.view.adapters.OfflineVideoAdapter;
import org.edx.mobile.view.adapters.OnlineVideoAdapter;
import org.edx.mobile.view.adapters.VideoBaseAdapter;
import org.edx.mobile.view.custom.ProgressWheel;
import org.edx.mobile.view.dialog.DeleteVideoDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideoListFragment extends Fragment {

    public static View offlineBar;
    private boolean isLandscape = false;
    private VideoBaseAdapter<SectionItemInterface> adapter;
    private ListView videoListView;
    private boolean myVideosFlag = false;
    private boolean isActivityStarted;
    protected IDatabase db;
    protected IStorage storage;
    private static final int MSG_UPDATE_PROGRESS = 1022;
    private DeleteVideoDialogFragment confirmDeleteFragment, downloadSizeExceedDialog;
    private String openInBrowserUrl;
    private String chapterName;
    private LectureModel lecture;
    private EnrolledCoursesResponse enrollment;
    private int playingVideoIndex = -1;
    private VideoListCallback callback;
    private DownloadEntry videoModel;
    private boolean downloadAvailable = false;
    private Button deleteButton;
    private ISegment segIO;
    private Api api;
    private final Logger logger = new Logger(getClass().getName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        initDB();
        segIO = SegmentFactory.getInstance();
        api = new Api(getActivity());
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
        if(extraIntent!=null){
            if (extraIntent.hasExtra("FromMyVideos")) {
                myVideosFlag = extraIntent.getBooleanExtra(
                        "FromMyVideos", false);
            }

            // read incoming chapter name
            if (chapterName == null) {
                chapterName = extraIntent.getStringExtra("chapter");
            }

            // read incoming lecture model
            if (lecture == null) {
                lecture = (LectureModel) extraIntent
                        .getSerializableExtra("lecture");
            }
            // read incoming enrollment model
            if (enrollment == null) {
                enrollment = (EnrolledCoursesResponse) extraIntent
                        .getSerializableExtra(BaseFragmentActivity.EXTRA_ENROLLMENT);
            }
        }

        if (chapterName == null) {
            if (enrollment != null && lecture != null) {
                if (lecture.chapter != null) {
                    chapterName = lecture.chapter.chapter;
                }
            }
        }

        videoListView = (ListView) getView().findViewById(R.id.list_video);

        if (videoListView != null) {
            isLandscape = false;

            videoListView.setEmptyView(getView().findViewById(
                    R.id.empty_list_view));
            offlineBar = (View) getView().findViewById(R.id.offline_bar);

            if (!(NetworkUtil.isConnected(getActivity()))) {
                if (offlineBar != null)
                    offlineBar.setVisibility(View.VISIBLE);
                showDeletePanel(getView());
                AppConstants.offline_flag = true;
            } else {
                if (offlineBar != null) {
                    offlineBar.setVisibility(View.GONE);
                }
                AppConstants.offline_flag = false;
            }

            setAdaptertoVideoList();

        } else {
            isLandscape = true;
            // probably the landscape player view, so hide action bar
            ActionBar bar = getActivity().getActionBar();
            if(bar!=null){
                bar.hide();
            }
        }
    }

    public void setAdaptertoVideoList(){
        if (!myVideosFlag) {
            if (AppConstants.offline_flag) {
                addDataToOfflineAdapter();
            } else {
                addDataToOnlineAdapter();
            }
        } else {
            showDeletePanel(getView());
            hideOpenInBrowserPanel();
            addDataToMyVideoAdapter();
        }
    }
    
    
    private void addDataToOnlineAdapter() {
        try {
            String selectedId = null;
            if(adapter!=null){
                selectedId = adapter.getVideoId();
            }
            adapter = new OnlineVideoAdapter(getActivity(), db , storage) {
                @Override
                public void onItemClicked(final SectionItemInterface model, final int position) {
                    if (model.isDownload()) {
                        // hide delete panel first, so that multiple-tap is blocked
                        hideDeletePanel(VideoListFragment.this.getView());

                        if (!NetworkUtil.isConnected(getContext())) {
                            ((VideoListActivity) getActivity()).showInfoMessage(getString(R.string.need_data));
                            notifyAdapter();
                        } else {
                            IDialogCallback dialogCallback = new IDialogCallback() {
                                @Override
                                public void onPositiveClicked() {
                                    startOnlinePlay(model, position);
                                }

                                @Override
                                public void onNegativeClicked() {
                                    //
                                }
                            };
                            MediaConsentUtils.consentToMediaDownload(getActivity(), dialogCallback);
                        }
                    }
                }

                @Override
                public void download(final DownloadEntry videoData, final ProgressWheel progressWheel) {
                    try {
                        if (!NetworkUtil.isConnected(getContext())) {
                            ((VideoListActivity) getActivity()).showInfoMessage(getString(R.string.need_data));
                            notifyAdapter();
                        } else {
                            if (!(getActivity() instanceof VideoListActivity)) {
                                return;
                            }
                            IDialogCallback dialogCallback = new IDialogCallback() {
                                @Override
                                public void onPositiveClicked() {
                                    startOnlineDownload(videoData, progressWheel);
                                }

                                @Override
                                public void onNegativeClicked() {
                                    //
                                }
                            };
                            MediaConsentUtils.consentToMediaDownload(getActivity(), dialogCallback);
                        }

                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            };

            if(selectedId!=null){
                adapter.setVideoId(selectedId);
            }
            videoListView.setAdapter(adapter);
            videoListView.setOnItemClickListener(adapter);

            if (lecture != null) {
                setActivityTitle(lecture.name);
                for (VideoResponseModel m : lecture.videos) {
                    if (openInBrowserUrl == null
                            || openInBrowserUrl.equalsIgnoreCase("")) {
                        openInBrowserUrl = m.section_url;
                    }
                    final DownloadEntry downloadEntry = (DownloadEntry) storage
                            .getDownloadEntryfromVideoResponseModel(m);
                    adapter.add(downloadEntry);
                }

            } else {
                setActivityTitle(chapterName);

                ArrayList<SectionItemInterface> list = api
                        .getLiveOrganizedVideosByChapter(enrollment.getCourse()
                                .getId(), chapterName);
                for (SectionItemInterface m : list) {
                    if (m.isVideo()) {
                        VideoResponseModel vidmodel = (VideoResponseModel) m;
                        DownloadEntry downloadEntry = (DownloadEntry) storage
                                .getDownloadEntryfromVideoResponseModel(vidmodel);
                        adapter.add(downloadEntry);
                    } else {
                        adapter.add(m);
                    }
                }
            }
            adapter.setSelectedPosition(playingVideoIndex);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void startOnlinePlay(SectionItemInterface model, int position){
        // hide delete panel first, so that multiple-tap is blocked
        hideDeletePanel(VideoListFragment.this.getView());

        if ( !isPlayerVisible()) {
            // don't try to showPlayer() if already shown here
            // this will cause player to freeze
            showPlayer();
        }

        DownloadEntry de = (DownloadEntry) model;
        addVideoDatatoDb(de);
        adapter.setVideoId(de.videoId);

        // initialize index for this model
        playingVideoIndex = position;

        play(model);
        notifyAdapter();
    }


    private void startOnlineDownload(DownloadEntry videoData, ProgressWheel progressWheel){
        long downloadSize = videoData.size;
        if (downloadSize > MemoryUtil
                .getAvailableExternalMemory(getActivity())) {
            ((VideoListActivity) getActivity())
                    .showInfoMessage(getString(R.string.file_size_exceeded));
            notifyAdapter();
        } else {
            if (downloadSize < MemoryUtil.GB) {
                startDownload(videoData, progressWheel);
            } else {
                showStartDownloadDialog(videoData, progressWheel);
            }
        }
    }

    private void addDataToOfflineAdapter() {
        try {
            String selectedId = null;
            if(adapter!=null){
                selectedId = adapter.getVideoId();
            }
            adapter = new OfflineVideoAdapter(getActivity(), db) {
                @Override
                public void onItemClicked(SectionItemInterface model,
                        int position) {
                    if (model.isDownload()) {

                        DownloadEntry downloadEntry = (DownloadEntry) model;
                        if (downloadEntry.isDownloaded()) {
                            adapter.setVideoId(downloadEntry.videoId);
                            // hide delete panel first, so that multiple-tap is blocked
                            hideDeletePanel(VideoListFragment.this.getView());

                            showPlayer();
                            // initialize index for this model
                            playingVideoIndex = position;

                            play(model);
                            notifyAdapter();
                        } else {
                            ((VideoListActivity) getActivity())
                                    .showOfflineAccessMessage();
                        }
                    }
                }

                @Override
                public void onSelectItem() {
                    handleCheckboxSelection();
                }
            };
            if(selectedId!=null){
                adapter.setVideoId(selectedId);
            }
            videoListView.setAdapter(adapter);
            videoListView.setOnItemClickListener(adapter);

            setActivityTitle(chapterName);

            ArrayList<SectionItemInterface> list = api
                    .getLiveOrganizedVideosByChapter(enrollment.getCourse()
                            .getId(), chapterName);
            downloadAvailable = false;
            for (SectionItemInterface m : list) {
                if (m.isVideo()) {
                    VideoResponseModel vidmodel = (VideoResponseModel) m;
                    DownloadEntry downloadEntry = (DownloadEntry) storage
                            .getDownloadEntryfromVideoResponseModel(vidmodel);
                    adapter.add(downloadEntry);
                    if(downloadEntry.isDownloaded()){
                        downloadAvailable = true;
                    }
                    // adapx.add(downloadEntry);
                } else {
                    adapter.add(m);
                    // adapx.add(m);
                }
            }

            if(!downloadAvailable){
                hideDeletePanel(getView());
            }

            adapter.setSelectedPosition(playingVideoIndex);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            logger.error(e);
        }

    }

    private void addDataToMyVideoAdapter() {
        try {
            String selectedId = null;
            if(adapter!=null){
                selectedId = adapter.getVideoId();
            }

            adapter = new MyAllVideoAdapter(getActivity(), db) {
                @Override
                public void onItemClicked(SectionItemInterface model,
                        int position) {
                    if (!AppConstants.myVideosDeleteMode) {
                        if (model.isDownload()) {
                            DownloadEntry downloadEntry = (DownloadEntry) model;
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
            if(selectedId!=null){
                adapter.setVideoId(selectedId);
            }

            videoListView.setAdapter(adapter);
            videoListView.setOnItemClickListener(adapter);

            setActivityTitle(enrollment.getCourse().getName());
            try{
                segIO.screenViewsTracking("My Videos - All Videos - "
                        + enrollment.getCourse().getName());
            }catch(Exception e){
                logger.error(e);
            }

            ArrayList<SectionItemInterface> list = storage
                    .getSortedOrganizedVideosByCourse(enrollment.getCourse().getId());
            downloadAvailable = false;
            if(list==null||list.size()==0){
                hideDeletePanel(getView());
                downloadAvailable = false;
            }else{
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
     * @param title
     */
    private void setActivityTitle(String title){
        try{
            getActivity().setTitle(title);
        }catch(Exception e){
            logger.error(e);
        }
    }

    private void play(SectionItemInterface model) {
        if (model instanceof DownloadEntry) {
            hideOpenInBrowserPanel();

            DownloadEntry v = (DownloadEntry) model;
            try {
                String prefName = PrefManager.getPrefNameForLastAccessedBy(getProfile()
                        .username, v.eid);
                PrefManager prefManager = new PrefManager(getActivity(), prefName);
                VideoResponseModel vrm = api.getVideoById(v.eid, v.videoId);
                prefManager.putLastAccessedSubsection(vrm.getSection().id, false);

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
        hideOpenInBrowserPanel();
        adapter.setIsPlayerOn(true);
        adapter.setSelectedPosition(playingVideoIndex);
        adapter.notifyDataSetChanged();
        try {
            if(getView()!=null){
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

    private void hideOpenInBrowserPanel() {
        try {
            getView().findViewById(R.id.open_in_browser_panel).setVisibility(
                    View.GONE);
        } catch (Exception ex) {
            logger.error(ex);
            logger.warn("Error in hideOpenInBrowserPanel");
        }
    }

    private void showOpenInBrowserPanel() {
        try {
            if (!AppConstants.offline_flag) {
                if (isPlayerVisible()) {
                    hideOpenInBrowserPanel();
                } else {
                    final StringBuffer urlStringBuffer = new StringBuffer();
                    if (!openInBrowserUrl.contains("http://") && !openInBrowserUrl.contains("https://")) {
                        urlStringBuffer.append("http://");
                        urlStringBuffer.append(openInBrowserUrl);
                    } else {
                        urlStringBuffer.append(openInBrowserUrl);
                    }
                    getView().findViewById(R.id.open_in_browser_panel)
                    .setVisibility(View.VISIBLE);
                    TextView openInBrowserTv = (TextView) getView().findViewById(
                            R.id.open_in_browser_btn);
                    openInBrowserTv.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            BrowserUtil.open(getActivity(),
                                    urlStringBuffer.toString());
                        }
                    });
                }
            } else {
                hideOpenInBrowserPanel();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.warn("Error in showOpenInBrowserPanel");
        }
    }

    public void onOffline() {
        if (!isLandscape) {
            AppConstants.offline_flag = true;
            if (offlineBar != null) {
                offlineBar.setVisibility(View.VISIBLE);
            }

            hideOpenInBrowserPanel();
            if (!myVideosFlag) {
                handleDeleteView();
                addDataToOfflineAdapter();
            }
        }
    }

    public void handleDeleteView() {
        if (isPlayerVisible()) {
            hideDeletePanel(getView());
        } else {
            if(downloadAvailable){
                showDeletePanel(getView());
            }
        }
    }

    public void onOnline() {
        AppConstants.offline_flag = false;
        if (!isLandscape) {
            if (offlineBar != null) {
                offlineBar.setVisibility(View.GONE);
            }
            if (!myVideosFlag) {
                AppConstants.videoListDeleteMode = false;
                addDataToOnlineAdapter();
                showOpenInBrowserPanel();
                hideDeletePanel(getView());
                hideConfirmDeleteDialog();
                handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
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
                    } catch(Exception ex) {
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
            hideOpenInBrowserPanel();

            if (isPlayerVisible()) {
                hideDeletePanel(view);
            } else {
                getView().findViewById(R.id.delete_button_panel).setVisibility(
                        View.VISIBLE);

                deleteButton = (Button) getView().findViewById(
                        R.id.delete_btn);
                final Button editButton = (Button) getView().findViewById(
                        R.id.edit_btn);
                final Button cancelButton = (Button) getView().findViewById(
                        R.id.cancel_btn);

                if (myVideosFlag) {
                    if (AppConstants.myVideosDeleteMode) {
                        deleteButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        editButton.setVisibility(View.GONE);
                    } else {
                        deleteButton.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);
                        editButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (AppConstants.videoListDeleteMode) {
                        deleteButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        editButton.setVisibility(View.GONE);
                    } else {
                        deleteButton.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);
                        editButton.setVisibility(View.VISIBLE);
                    }
                }

                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<SectionItemInterface> list;
                        if (myVideosFlag)
                            list = adapter.getSelectedItems();
                        else
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
                        if (myVideosFlag) {
                            AppConstants.myVideosDeleteMode = false;
                        } else {
                            AppConstants.videoListDeleteMode = false;
                        }
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

                        if (myVideosFlag) {
                            AppConstants.myVideosDeleteMode = true;
                            adapter.setSelectedPosition(playingVideoIndex);
                            adapter.notifyDataSetChanged();
                        } else {
                            AppConstants.videoListDeleteMode = true;
                            adapter.setSelectedPosition(playingVideoIndex);
                            adapter.notifyDataSetChanged();
                        }

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
        if(myVideosFlag){
            adapter.unselectAll();
        }
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
        if (!myVideosFlag) {
            handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        }
    }

    public void setAllVideosChecked() {
        adapter.selectAll();
        int count  = adapter.getSelectedVideoItemsCount();

        logger.debug("Video Count of selected videos"+ count);
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

    public boolean isActivityStarted() {
        return isActivityStarted;
    }

    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_UPDATE_PROGRESS) {
                if (isActivityStarted()) {
                    if (!AppConstants.offline_flag) {
                        if (adapter != null) {
                            adapter.setSelectedPosition(playingVideoIndex);
                            adapter.notifyDataSetChanged();
                            logger.debug("Download list reloaded");
                        }
                        sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 3000);
                    }
                }
            }
        }
    };

    public void markPlaying() {
        storage.markVideoPlaying(videoModel, watchedStateCallback);
    }

    /**
     * This method inserts the Download Entry Model in the database
     * Called when a user clicks on a Video in the list
     * @param v - Download Entry object
     */
    public void addVideoDatatoDb(final DownloadEntry v) {
        try {
            if (v != null) {
                db.addVideoData(v, new DataCallback<Long>() {
                    @Override
                    public void onResult(Long result) {
                        if(result!=-1){
                            logger.debug("Video entry inserted"+v.videoId);
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
                db.updateVideoLastPlayedOffset(v.videoId, offset, 
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

            if (v!=null && v.watched == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                videoModel.watched = DownloadEntry.WatchedState.WATCHED;
                // mark this as partially watches, as playing has started
                db.updateVideoWatchedState(v.videoId, DownloadEntry.WatchedState.WATCHED,
                        watchedStateCallback);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void onConfirmDelete() {
        ArrayList<SectionItemInterface> list;
        if (myVideosFlag) {
            list = adapter.getSelectedItems();
            if (list != null) {
                for (SectionItemInterface section : list) {
                    if (section.isDownload()) {
                        DownloadEntry de = (DownloadEntry) section;
                        storage.removeDownload(de);
                    }
                }
            }
            addDataToMyVideoAdapter();
            adapter.setSelectedPosition(playingVideoIndex);
            adapter.notifyDataSetChanged();
            ((VideoListActivity) getActivity()).hideCheckBox();
            AppConstants.myVideosDeleteMode = false;
            videoListView.setOnItemClickListener(adapter);
            if(adapter.getCount()<=0){
                finishActivity();
            }
        } else {
            list = adapter.getSelectedItems();
            if (list != null) {
                for (SectionItemInterface section : list) {
                    if (section.isDownload()) {
                        DownloadEntry de = (DownloadEntry) section;
                        if(de.isDownloaded()){
                            storage.removeDownload(de);
                        }
                    }
                }
            }
            addDataToOfflineAdapter();
            adapter.setSelectedPosition(playingVideoIndex);
            adapter.notifyDataSetChanged();
            videoListView.setOnItemClickListener(adapter);
            AppConstants.videoListDeleteMode = false;
            ((VideoListActivity) getActivity()).hideCheckBox();
        }
        getView().findViewById(R.id.delete_btn).setVisibility(View.GONE);
        getView().findViewById(R.id.edit_btn).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.cancel_btn).setVisibility(View.GONE);
    }

    
    private void finishActivity() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isActivityStarted){
                    getActivity().finish();
                }
            }
        }, 300);
    }

    private void handleCheckboxSelection() {
        int selectedItemsNo = adapter.getSelectedVideoItemsCount();
        int totalVideos = adapter.getTotalVideoItemsCount();

        if(selectedItemsNo==0){
            disableDeleteButton();
        }else{
            enableDeleteButton();
        }

        if (selectedItemsNo == totalVideos) {
            ((VideoListActivity) getActivity()).setCheckBoxSelected();
        } else if (selectedItemsNo < totalVideos) {
            ((VideoListActivity) getActivity()).unsetCheckBoxSelected();
        }
    }

    public void startDownload(final DownloadEntry downloadEntry, final ProgressWheel progressWheel) {
        try{
            if (storage != null) {
                boolean isVideoFilePresentByUrl = db.isVideoFilePresentByUrl(
                        downloadEntry.url, null);
                boolean reloadListFlag = true;
                if(isVideoFilePresentByUrl){
                    CircularProgressTask circularTask = new CircularProgressTask();
                    circularTask.setProgressBar(progressWheel);
                    circularTask.execute();
                    reloadListFlag = false;
                }

                if (segIO != null) {
                    segIO.trackSingleVideoDownload(downloadEntry.videoId, downloadEntry.eid,
                            downloadEntry.lmsUrl);
                }

                if (storage.addDownload(downloadEntry) != -1) {
                    ((VideoListActivity) getActivity())
                            .showInfoMessage(getString(R.string.msg_started_one_video_download));
                } else {
                    ((VideoListActivity) getActivity())
                            .showInfoMessage(getString(R.string.msg_video_not_downloaded));
                }
                ((VideoListActivity) getActivity()).updateProgress();

                //If the video is already downloaded, dont reload the adapter
                if (reloadListFlag) {
                    adapter.notifyDataSetChanged();
                }
                TranscriptManager transManager = new TranscriptManager(getActivity());
                transManager.downloadTranscriptsForVideo(downloadEntry.transcript);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    protected void showStartDownloadDialog(final DownloadEntry de, final ProgressWheel progressWheel) {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", getString(R.string.download_exceed_title));
        dialogMap.put("message_1", getString(R.string.download_exceed_message));
        dialogMap.put("yes_button", getString(R.string.label_yes));
        dialogMap.put("no_button",  getString(R.string.label_no));
        downloadSizeExceedDialog = DeleteVideoDialogFragment.newInstance(dialogMap,
                new IDialogCallback() {
            @Override
            public void onPositiveClicked() {
                startDownload(de, progressWheel);
            }

            @Override
            public void onNegativeClicked() {
                notifyAdapter();
                downloadSizeExceedDialog.dismiss();
            }
        });
        downloadSizeExceedDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        downloadSizeExceedDialog.show(getFragmentManager(), "dialog");
        downloadSizeExceedDialog.setCancelable(false);
    }

    protected void showConfirmDeleteDialog(int itemCount) {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", getString(R.string.delete_dialog_title_help));
        dialogMap.put("yes_button", getString(R.string.label_delete));
        dialogMap.put("no_button",  getString(R.string.label_cancel));
        if (itemCount == 1) {
            dialogMap.put("message_1",
                    getString(R.string.delete_single_video_dialog));
        } else {
            dialogMap.put("message_1",
                    getString(R.string.delete_multiple_video_dialog));
        }
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
        try{
            if(confirmDeleteFragment!=null){
                confirmDeleteFragment.dismiss();
            }
        }catch(Exception e){
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

    private void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            playingVideoIndex = savedInstanceState.getInt("playingVideoIndex", -1);
            videoModel = (DownloadEntry) savedInstanceState.getSerializable("model");
        }
    }

    public static interface VideoListCallback {
        public void playVideoModel(DownloadEntry video);
    }

    private void disableDeleteButton(){
        deleteButton.setEnabled(false);
    }

    private void enableDeleteButton(){
        deleteButton.setEnabled(true);

    }

    public void playNext() {
        try{
            //playingVideoIndex++;
            int videoPos = adapter.getPositionByVideoId(videoModel.videoId);
            if(videoPos!=-1){
                // check next playable video entry
                videoPos++;
                while (videoPos < adapter.getCount()) {
                    SectionItemInterface i = adapter.getItem(videoPos);
                    if (i!=null && i instanceof DownloadEntry) {
                        if(AppConstants.offline_flag){
                            DownloadEntry de = (DownloadEntry) i;
                            if(de.isDownloaded()){
                                if (callback != null) {
                                    videoModel = de;
                                    playingVideoIndex=videoPos;
                                    adapter.setSelectedPosition(playingVideoIndex);
                                    adapter.setVideoId(videoModel.videoId);
                                    callback.playVideoModel(videoModel);
                                    break;
                                }
                            }
                        }else{
                            if (callback != null) {
                                videoModel = (DownloadEntry) i;
                                playingVideoIndex=videoPos;
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

        }catch(Exception e){
            logger.error(e);
        }
    }

    public boolean hasNextVideo() {
        try{
            if(videoModel!=null){
                int videoPos = adapter.getPositionByVideoId(videoModel.videoId);
                if(videoPos!=-1){
                    for (int i=(videoPos+1) ; i<adapter.getCount(); i++) {
                        SectionItemInterface d = adapter.getItem(i);
                        if (d!=null && d instanceof DownloadEntry) {
                            DownloadEntry de = (DownloadEntry) d;
                            if(AppConstants.offline_flag){
                                if(de.isDownloaded()){
                                    return true;
                                }
                            }else{
                                return true;
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
        return false;
    }

    public void playPrevious() {
        try{
            //playingVideoIndex--;
            int videoPos = adapter.getPositionByVideoId(videoModel.videoId);
            if(videoPos!=-1){
                // check next playable video entry
                videoPos--;
                while (videoPos >= 0) {
                    SectionItemInterface i = adapter.getItem(videoPos);
                    if (i!=null && i instanceof DownloadEntry) {
                        if(AppConstants.offline_flag){
                            DownloadEntry de = (DownloadEntry) i;
                            if(de.isDownloaded()){
                                if (callback != null) {
                                    videoModel = de;
                                    adapter.setVideoId(videoModel.videoId);
                                    playingVideoIndex=videoPos;
                                    adapter.setSelectedPosition(playingVideoIndex);
                                    callback.playVideoModel(videoModel);
                                    break;
                                }
                            }
                        }else{
                            if (callback != null) {
                                videoModel = (DownloadEntry) i;
                                adapter.setVideoId(videoModel.videoId);
                                playingVideoIndex=videoPos;
                                adapter.setSelectedPosition(playingVideoIndex);
                                callback.playVideoModel(videoModel);
                                break;
                            }
                        }
                    }
                    videoPos--;
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    public boolean hasPreviousVideo() {
        try{
            if(videoModel!=null){
                int videoPos = adapter.getPositionByVideoId(videoModel.videoId);
                if(videoPos!=-1){
                    for (int i=(videoPos-1) ; i>=0; i--) {
                        SectionItemInterface d = adapter.getItem(i);
                        if (d!=null && d instanceof DownloadEntry) {
                            DownloadEntry de = (DownloadEntry) d;
                            if(AppConstants.offline_flag){
                                if(de.isDownloaded()){
                                    return true;
                                }
                            }else{
                                return true;
                            }
                        }
                    }   
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
        return false;
    }

    public View.OnClickListener getNextListener(){
        if(hasNextVideo()){
            return new NextClickListener();
        }
        return null;
    }

    public View.OnClickListener getPreviousListener(){
        if(hasPreviousVideo()){
            return new PreviousClickListener();
        }
        return null;
    }

    private class NextClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            playNext();
        }
    }

    private class PreviousClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            playPrevious();
        }
    }

    private void initDB() {
        storage = new Storage(getActivity());

        UserPrefs userprefs = new UserPrefs(getActivity());
        String username = null;
        if (userprefs != null) {
            ProfileModel profile = userprefs.getProfile();
            if(profile!=null){
                username =profile.username;
            }
        }
        db = DatabaseFactory.getInstance(getActivity(), 
                DatabaseFactory.TYPE_DATABASE_NATIVE, username);

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
    
    /**
     * Returns user's profile.
     * @return
     */
    protected ProfileModel getProfile() {
        PrefManager prefManager = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
        return prefManager.getCurrentUserProfile();
    }

}
