package org.edx.mobile.view;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.http.Api;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.IUnit;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.player.PlayerFragment;
import org.edx.mobile.player.TranscriptManager;
import org.edx.mobile.task.CircularProgressTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
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

/**
 *
 */
public class CourseUnitVideoFragment extends Fragment {

    protected final Logger logger = new Logger(getClass().getName());
    IUnit unit;
    private PlayerFragment playerFragment;
    private boolean isLandscape = false;
    private boolean myVideosFlag = false;
    private boolean isActivityStarted;
    private static final int MSG_UPDATE_PROGRESS = 1022;
    private DeleteVideoDialogFragment downloadSizeExceedDialog;
    private String openInBrowserUrl;
    private String chapterName;
    private LectureModel lecture;
    private EnrolledCoursesResponse enrollment;
    private DownloadEntry videoModel;
    private boolean downloadAvailable = false;
    private Button deleteButton;
    private Api api;

    /**
     * Create a new instance of fragment
     */
    static CourseUnitVideoFragment newInstance(IUnit unit) {
        CourseUnitVideoFragment f = new CourseUnitVideoFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        api = new Api(getActivity());
        unit = getArguments() == null ? null :
            (IUnit) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_video, container, false);
        //TODO - populate view here
        return v;
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
                    .getSerializableExtra(Router.EXTRA_ENROLLMENT);
            }
        }

        if (chapterName == null) {
            if (enrollment != null && lecture != null) {
                if (lecture.chapter != null) {
                    chapterName = lecture.chapter.chapter;
                }
            }
        }


        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false;


            if (!(NetworkUtil.isConnected(getActivity()))) {

                AppConstants.offline_flag = true;
            } else {

                AppConstants.offline_flag = false;
            }


        } else {
            isLandscape = true;
            // probably the landscape player view, so hide action bar
            ActionBar bar = getActivity().getActionBar();
            if(bar!=null){
                bar.hide();
            }
        }
        if (playerFragment == null) {

            playerFragment = new PlayerFragment();
            try{
                FragmentManager fm = getChildFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.player_container, playerFragment, "player");
                ft.commit();
            }catch(Exception ex){
                logger.error(ex);
            }
        }
        checkVideoStatus(unit);
    }

    public void onResume(){
        super.onResume();

    }
    public void onPause(){
        super.onPause();
        //Stop it.
    }


    private void checkVideoStatus(IUnit unit) {
        try {
            final DownloadEntry entry = unit.getDownloadEntry();
            if ( unit.getDownloadEntry() == null )
                return;

            if ( entry.isDownload() ){
                if ( entry.isVideoForWebOnly ){
                    Toast.makeText(getActivity(), getString(R.string.video_only_on_web_short), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!entry.isDownloaded()){
                    IDialogCallback dialogCallback = new IDialogCallback() {
                        @Override
                        public void onPositiveClicked() {
                            startOnlinePlay(entry);
                        }
                        @Override
                        public void onNegativeClicked() {
                            ((VideoListActivity) getActivity()).showInfoMessage(getString(R.string.wifi_off_message));
                            notifyAdapter();
                        }
                    };
                    MediaConsentUtils.consentToMediaPlayback(getActivity(), dialogCallback);
                }else{
                    if (  AppConstants.offline_flag ){
                        //TODO - should use interface to decouple
                        ((CourseBaseActivity) getActivity())
                            .showOfflineAccessMessage();
                    } else {
                        //Video is downloaded. Hence play
                        startOnlinePlay(entry);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void startOnlinePlay(DownloadEntry model){

        if ( !isPlayerVisible()) {
            // don't try to showPlayer() if already shown here
            // this will cause player to freeze
            showPlayer();
        }

        DownloadEntry de = (DownloadEntry) model;
        addVideoDatatoDb(de);


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



    private void play(DownloadEntry model) {
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

                videoModel = v;
            } catch (Exception e) {
                logger.error(e);
            }

        }
    }

    private void showPlayer() {
        hideOpenInBrowserPanel();
        try {
            if(getView()!=null){
                View container = getView().findViewById(R.id.player_container);
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
                    openInBrowserTv.setOnClickListener(new View.OnClickListener() {
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

            hideOpenInBrowserPanel();
            if (!myVideosFlag) {

            }
        }
    }


    public void onOnline() {
        AppConstants.offline_flag = false;
        if (!isLandscape) {

            if (!myVideosFlag) {
                 showOpenInBrowserPanel();
                 handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
            }

        }
    }



    @Override
    public void onStop() {
        super.onStop();
        isActivityStarted = false;
        AppConstants.videoListDeleteMode = false;
        if(myVideosFlag){
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        isActivityStarted = true;
        if (!myVideosFlag) {
            handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        }
    }


    public boolean isActivityStarted() {
        return isActivityStarted;
    }

    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_UPDATE_PROGRESS) {
                if (isActivityStarted()) {
                    if (!AppConstants.offline_flag) {

                        sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 3000);
                    }
                }
            }
        }
    };

    public void markPlaying() {
        new Storage(getActivity()).markVideoPlaying(videoModel, watchedStateCallback);
    }

    /**
     * This method inserts the Download Entry Model in the database
     * Called when a user clicks on a Video in the list
     * @param v - Download Entry object
     */
    public void addVideoDatatoDb(final DownloadEntry v) {
        try {
            if (v != null) {
                DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE).addVideoData(v, new DataCallback<Long>() {
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
                DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE ).updateVideoLastPlayedOffset(v.videoId, offset,
                    setCurrentPositionCallback);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void onPlaybackComplete() {
        try {
            DownloadEntry v = videoModel;
//            if (v == null) {
//                v = (DownloadEntry) adapter.getItem(playingVideoIndex);
//            }

            if (v!=null && v.watched == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                videoModel.watched = DownloadEntry.WatchedState.WATCHED;
                // mark this as partially watches, as playing has started
                DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE )
                    .updateVideoWatchedState(v.videoId, DownloadEntry.WatchedState.WATCHED,
                    watchedStateCallback);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
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



    public void startDownload(final DownloadEntry downloadEntry, final ProgressWheel progressWheel) {
        try{

                boolean isVideoFilePresentByUrl = DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE)
                    .isVideoFilePresentByUrl(
                    downloadEntry.url, null);
                boolean reloadListFlag = true;
                if(isVideoFilePresentByUrl){
                    CircularProgressTask circularTask = new CircularProgressTask();
                    circularTask.setProgressBar(progressWheel);
                    circularTask.execute();
                    reloadListFlag = false;
                }

                if (SegmentFactory.getInstance() != null) {
                    SegmentFactory.getInstance().trackSingleVideoDownload(downloadEntry.videoId, downloadEntry.eid,
                        downloadEntry.lmsUrl);
                }

                if (new Storage(getActivity()).addDownload(downloadEntry) != -1) {
                    ((VideoListActivity) getActivity())
                        .showInfoMessage(getString(R.string.msg_started_one_video_download));
                } else {
                    ((VideoListActivity) getActivity())
                        .showInfoMessage(getString(R.string.msg_video_not_downloaded));
                }
                ((VideoListActivity) getActivity()).updateProgress();

                //If the video is already downloaded, dont reload the adapter
                if (reloadListFlag) {
                    //adapter.notifyDataSetChanged();
                }
                TranscriptManager transManager = new TranscriptManager(getActivity());
                transManager.downloadTranscriptsForVideo(downloadEntry.transcript);

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



    private boolean isPlayerVisible() {
        if (getActivity() == null) {
            return false;
        }
        View container = getActivity().findViewById(R.id.container_player);
        return (container != null && container.getVisibility() == View.VISIBLE);
    }

    public void notifyAdapter() {

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("model", videoModel);
        super.onSaveInstanceState(outState);
    }

    private void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            videoModel = (DownloadEntry) savedInstanceState.getSerializable("model");
        }
    }

    public View.OnClickListener getNextListener(){

        return null;
    }

    public View.OnClickListener getPreviousListener(){

        return null;
    }

    private class NextClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

    private class PreviousClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

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

    /**
     * Returns user's profile.
     * @return
     */
    protected ProfileModel getProfile() {
        PrefManager prefManager = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
        return prefManager.getCurrentUserProfile();
    }



}
