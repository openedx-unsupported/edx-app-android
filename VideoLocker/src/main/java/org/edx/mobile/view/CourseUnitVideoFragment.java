package org.edx.mobile.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.player.IPlayerEventCallback;
import org.edx.mobile.player.PlayerFragment;
import org.edx.mobile.services.ViewPagerDownloadManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.io.File;

public class CourseUnitVideoFragment extends CourseUnitFragment
    implements IPlayerEventCallback{

    protected final Logger logger = new Logger(getClass().getName());
    VideoBlockModel unit;
    private PlayerFragment playerFragment;
    private boolean myVideosFlag = false;
    private boolean isActivityStarted;
    private static final int MSG_UPDATE_PROGRESS = 1022;
    private String chapterName;
    private LectureModel lecture;
    private EnrolledCoursesResponse enrollment;
    private DownloadEntry videoModel;

    private Runnable playPending;
    private final Handler playHandler = new Handler();
    private View messageContainer;

    private final static String HAS_NEXT_UNIT_ID = "has_next_unit";
    private boolean hasNextUnit;

    private final static String HAS_PREV_UNIT_ID = "has_prev_unit";
    private boolean hasPreviousUnit;

    /**
     * Create a new instance of fragment
     */
    public static CourseUnitVideoFragment newInstance(VideoBlockModel unit, boolean hasNextUnit, boolean hasPreviousUnit) {
        CourseUnitVideoFragment f = new CourseUnitVideoFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putBoolean(HAS_NEXT_UNIT_ID, hasNextUnit);
        args.putBoolean(HAS_PREV_UNIT_ID, hasPreviousUnit);
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
        hasNextUnit = getArguments().getBoolean(HAS_NEXT_UNIT_ID);
        hasPreviousUnit = getArguments().getBoolean(HAS_PREV_UNIT_ID);
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
                    .getSerializableExtra(Router.EXTRA_COURSE_DATA);
            }
        }

        if (chapterName == null) {
            if (enrollment != null && lecture != null) {
                if (lecture.chapter != null) {
                    chapterName = lecture.chapter.chapter;
                }
            }
        }

        if (playerFragment == null) {

            playerFragment = new PlayerFragment();
            playerFragment.setCallback(this);

            final CourseUnitVideoFragment.HasComponent hasComponent = (CourseUnitVideoFragment.HasComponent)getActivity();
            if (hasComponent != null) {
                View.OnClickListener next = null;
                View.OnClickListener prev = null;

                if (hasNextUnit) {
                    next = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hasComponent.navigateNextComponent();
                        }
                    };
                }

                if (hasPreviousUnit) {
                    prev = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hasComponent.navigatePreviousComponent();
                        }
                    };
                }

                playerFragment.setNextPreviousListeners(next, prev);
            }

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
            checkVideoStatusAndPlay(unit);
        }
        if (ViewPagerDownloadManager.instance.inInitialPhase(unit)) {
            ViewPagerDownloadManager.instance.addTask(this);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (playerFragment == null) {
            return;
        }

        if (isVisibleToUser) {
            if (playerFragment.getPlayingVideo() == null) {
                checkVideoStatusAndPlay(unit);
            } else {
                checkVideoStatus(unit);
            }
        } else {
            ((BaseFragmentActivity) getActivity()).hideInfoMessage();
        }

        playerFragment.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void run() {
        ViewPagerDownloadManager.instance.done(this, false);
    }


    private boolean checkDownloadEntry(DownloadEntry entry) {
        if (entry == null || !entry.isDownload()) {
            return false;
        }

        if (entry.isVideoForWebOnly) {
            Toast.makeText(getContext(), getString(R.string.video_only_on_web_short),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkVideoStatus(VideoBlockModel unit) {
        final DownloadEntry entry = unit.getDownloadEntry(environment.getStorage());
        if (checkDownloadEntry(entry) && !entry.isDownloaded()) {
            if (!MediaConsentUtils.canStreamMedia(getContext())) {
                ((BaseFragmentActivity) getActivity()).
                        showInfoMessage(getString(R.string.wifi_off_message));
            }
        }
    }

    private void checkVideoStatusAndPlay(VideoBlockModel unit) {
        final DownloadEntry entry = unit.getDownloadEntry(environment.getStorage());
        if (!checkDownloadEntry(entry)) return;
        if (entry.isDownloaded()) {
            startOnlinePlay(entry);
        } else {
            MediaConsentUtils.requestStreamMedia(getActivity(), new IDialogCallback() {
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
            });
        }
    }

    private void startOnlinePlay(DownloadEntry model){
        if ( !isPlayerVisible()) {
            // don't try to showPlayer() if already shown here
            // this will cause player to freeze
            showPlayer();
        }

        addVideoDatatoDb(model);

        playVideoModel(model);
        notifyAdapter();
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
        if (!(video.filepath != null && video.filepath.length()>0)) {
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

    @Override
    public void onStop() {
        super.onStop();
        isActivityStarted = false;
        AppConstants.videoListDeleteMode = false;

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

    @Override
    public void onResume() {
        super.onResume();
        updateUIForOrientation();
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

    private void updateUIForOrientation() {
        //TODO - should we use load different layout file?
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            messageContainer.setVisibility(View.GONE);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUIForOrientation();
    }
}
