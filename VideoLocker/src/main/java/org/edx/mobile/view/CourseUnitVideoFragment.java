package org.edx.mobile.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.player.IPlayerEventCallback;
import org.edx.mobile.player.PlayerFragment;
import org.edx.mobile.player.TranscriptManager;
import org.edx.mobile.services.ViewPagerDownloadManager;
import org.edx.mobile.task.CircularProgressTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.custom.ProgressWheel;
import org.edx.mobile.view.dialog.DeleteVideoDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CourseUnitVideoFragment extends CourseUnitFragment
    implements IPlayerEventCallback{

    protected final Logger logger = new Logger(getClass().getName());
    VideoBlockModel unit;
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

    private Runnable playPending;
    private final Handler playHandler = new Handler();
    private View messageContainer;

    @Inject
    TranscriptManager transcriptManager;


    /**
     * Create a new instance of fragment
     */
    static CourseUnitVideoFragment newInstance(VideoBlockModel unit) {
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
        unit = getArguments() == null ? null :
            (VideoBlockModel) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_video, container, false);
        messageContainer = v.findViewById(R.id.message_container);
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
        } else {
            isLandscape = true;
            // probably the landscape player view, so hide action bar
            ActionBar bar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if(bar!=null){
                bar.hide();
            }
        }
        if (playerFragment == null) {

            playerFragment = new PlayerFragment();
            playerFragment.setInViewPager(true);
            try{
                FragmentManager fm = getChildFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.player_container, playerFragment, "player");
                ft.commit();
            }catch(Exception ex){
                logger.error(ex);
            }
        }
        if (getUserVisibleHint()) {
            checkVideoStatus(unit);
        }
        if (ViewPagerDownloadManager.instance.inInitialPhase(unit)) {
            ViewPagerDownloadManager.instance.addTask(this);
        }
    }

    public void onResume() {
        super.onResume();
        if ( hasComponentCallback != null ){
            CourseComponent component = hasComponentCallback.getComponent();
            if (component != null && component.equals(unit)){
                setVideoPlayerState(true);
            }
        }
    }

    public void onPause(){
        super.onPause();
        setVideoPlayerState(false);
    }

    //we use user visible hint, not onResume() for video
    //as the original playerfragment code use onResume to
    //control the lifecycle of the player.
    //the problem with viewpager is that it loads this fragment
    //and calls onResume even it is not visible.
    //which breaks the normal behavior of activity/fragment
    //lifecycle.
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (ViewPagerDownloadManager.instance.inInitialPhase(unit))
            return;
        if (isVisibleToUser) {
            checkVideoStatus(unit);
            setVideoPlayerState(true);
        } else {
            // fragment is no longer visible
            if (getActivity() != null) {
                ((BaseFragmentActivity) getActivity()).hideInfoMessage();
            }
            setVideoPlayerState(false);
        }
    }

    @Override
    public void run() {
        if (this.isRemoving() || this.isDetached()) {
            ViewPagerDownloadManager.instance.done(this, false);
        } else {
            setVideoPlayerState(true);
            ViewPagerDownloadManager.instance.done(this, true);
        }
    }

    /**
     * Sets the playing/paused state of the video player in {@link PlayerFragment}
     *
     * @param playing <code>true</code> for playing the video player, <code>false</code> for
     *                pausing it.
     */
    private void setVideoPlayerState(boolean playing) {
        if (playerFragment != null) {
            if (playing) {
                playerFragment.handleOnResume();
                playerFragment.setCallback(this);
            } else {
                playerFragment.handleOnPause();
                playerFragment.setCallback(null);
            }
        }
    }


    private void checkVideoStatus(VideoBlockModel unit) {
        final DownloadEntry entry = unit.getDownloadEntry(environment.getStorage());
        if (entry == null)
            return;

        if (entry.isDownload()) {
            if (entry.isVideoForWebOnly) {
                Toast.makeText(getActivity(), getString(R.string.video_only_on_web_short),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!entry.isDownloaded()) {
                IDialogCallback dialogCallback = new IDialogCallback() {
                    @Override
                    public void onPositiveClicked() {
                        startOnlinePlay(entry);
                    }

                    @Override
                    public void onNegativeClicked() {
                        ((BaseFragmentActivity) getActivity()).
                                showInfoMessage(getString(R.string.wifi_off_message));
                        notifyAdapter();
                    }
                };
                MediaConsentUtils.consentToMediaPlayback(getActivity(), dialogCallback,
                        environment.getConfig());
            } else if (playerFragment != null && !playerFragment.isFrozen()) {
                //Video is downloaded. Hence play
                startOnlinePlay(entry);
            }
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


        playVideoModel(model);
        notifyAdapter();
    }


    private void startOnlineDownload(DownloadEntry videoData, ProgressWheel progressWheel){
        long downloadSize = videoData.size;
        if (downloadSize > MemoryUtil
            .getAvailableExternalMemory(getActivity())) {
            ((BaseFragmentActivity) getActivity())
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


    public synchronized void playVideoModel(final DownloadEntry video) {
        try {
            if (playerFragment.isPlaying()) {
                if (video.getVideoId().equals(playerFragment.getPlayingVideo().getVideoId())) {
                    logger.debug("this video is already being played, skipping play event");
                    return;
                }
            }
        } catch(Exception ex) {
            logger.debug(ex.toString());
        }
        try{

            // reload this model
            environment.getStorage().reloadDownloadEntry(video);

            logger.debug("Resumed= " + playerFragment.isResumed());
            if ( !playerFragment.isResumed()) {
                // playback can work only if fragment is resume
                if (playPending != null) {
                    playHandler.removeCallbacks(playPending);
                }
                playPending = new Runnable() {
                    public void run() {
                        playVideoModel(video);
                    }
                };
                playHandler.postDelayed(playPending, 200);
                return;
            } else {
                if (playPending != null) {
                    playHandler.removeCallbacks(playPending);
                }
            }

            TranscriptModel transcript = getTranscriptModel(video);
            String filepath = getVideoPath(video);


            playerFragment.prepare(filepath, video.lastPlayedOffset,
                video.getTitle(), transcript, video);


            try {
                // capture chapter name
                if (chapterName == null) {
                    // capture the chapter name of this video
                    chapterName = video.chapter;
                }

                videoModel = video;
            } catch (Exception e) {
                logger.error(e);
            }
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    private String getVideoPath(DownloadEntry video){
        String filepath = null;
        // check if file available on local
        if( video.isVideoForWebOnly ){
            //don't download anything
        }
        else if (video.filepath != null && video.filepath.length()>0) {
            if (video.isDownloaded()) {
                File f = new File(video.filepath);
                if (f.exists()) {
                    // play from local
                    filepath = video.filepath;
                    logger.debug("playing from local file");
                }
            }
        } else {
            DownloadEntry de = (DownloadEntry)DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE )
                .getIVideoModelByVideoUrl(
                    video.url, null);
            if(de!=null){
                if(de.filepath!=null){
                    File f = new File(de.filepath);
                    if (f.exists()) {
                        // play from local
                        filepath = de.filepath;
                        logger.debug("playing from local file for " +
                            "another Download Entry");
                    }
                }
            }
        }

        if(TextUtils.isEmpty(filepath)){
            // not available on local, so play online
            logger.warn("Local file path not available");

            filepath = video.getBestEncodingUrl(getActivity());
        }
        return filepath;
    }

    private TranscriptModel getTranscriptModel(DownloadEntry video){
        TranscriptModel transcript = null;
        if(unit!=null && unit.getData() != null &&
            unit.getData().transcripts != null) {
            transcript = unit.getData().transcripts;
        }
        if ( transcript == null ) {
            try {
                if (video.videoId != null) {
                    transcript =  environment.getServiceManager().getTranscriptsOfVideo(video.eid, video.videoId);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return transcript;
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
            if (NetworkUtil.isConnected(getActivity())) {
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
            hideOpenInBrowserPanel();
            if (!myVideosFlag) {

            }
        }
    }


    public void onOnline() {
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
        try {
            if (playerFragment != null) {
                playerFragment.onStop();
            }
        } catch (Exception ex) {
            logger.error(ex);
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
                    if (NetworkUtil.isConnected(getActivity())) {

                        sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 3000);
                    }
                }
            }
        }
    };

    public void markPlaying() {
        environment.getStorage().markVideoPlaying(videoModel, watchedStateCallback);
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

    @Override
    public void onError() {

    }

    @Override
    public void onPlaybackStarted() {
         markPlaying();
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

                if (environment.getSegment() != null) {
                    environment.getSegment().trackSingleVideoDownload(downloadEntry.videoId, downloadEntry.eid,
                        downloadEntry.lmsUrl);
                }

                if (environment.getStorage().addDownload(downloadEntry) != -1) {
                    ((BaseFragmentActivity) getActivity())
                        .showInfoMessage(getString(R.string.msg_started_one_video_download));
                } else {
                    ((BaseFragmentActivity) getActivity())
                        .showInfoMessage(getString(R.string.msg_video_not_downloaded));
                }

                //If the video is already downloaded, dont reload the adapter
                if (reloadListFlag) {
                    //adapter.notifyDataSetChanged();
                }
                transcriptManager.downloadTranscriptsForVideo(downloadEntry.transcript);

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

    /**
     * mostly the orientation changes.
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //TODO - should we use load different layout file?
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            messageContainer.setVisibility(View.GONE);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            LinearLayout playerContainer = (LinearLayout)getView().findViewById(R.id.player_container);
            if ( playerContainer != null ) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                float screenHeight = displayMetrics.heightPixels;
                playerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) screenHeight));
                playerContainer.requestLayout();
            }
        } else {
            messageContainer.setVisibility(View.VISIBLE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            LinearLayout playerContainer = (LinearLayout)getView().findViewById(R.id.player_container);
            if ( playerContainer != null ) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                float screenWidth = displayMetrics.widthPixels;
                float ideaHeight = screenWidth * 9 / 16;

                playerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) ideaHeight));
                playerContainer.requestLayout();
            }
        }
    }


}
