package org.edx.mobile.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.player.PlayerFragment;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.io.File;


import subtitleFile.Caption;

public class CourseUnitAndroidVideoPlayerFragment extends CourseUnitVideoFragment {

    private final static int MSG_UPDATE_PROGRESS = 1022;

    private PlayerFragment playerFragment;
    private boolean myVideosFlag = false;
    private boolean isActivityStarted;
    private String chapterName;
    private LectureModel lecture;
    private EnrolledCoursesResponse enrollment;

    private Runnable playPending;
    private final Handler playHandler = new Handler();

    /**
     * Create a new instance of fragment
     */
    public static CourseUnitAndroidVideoPlayerFragment newInstance(VideoBlockModel unit, boolean hasNextUnit, boolean hasPreviousUnit) {
        CourseUnitAndroidVideoPlayerFragment fragment = new CourseUnitAndroidVideoPlayerFragment();
        fragment.setArguments(getCourseUnitBundle(unit, hasNextUnit, hasPreviousUnit));
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent extraIntent = getActivity().getIntent();
        if(extraIntent!=null){
            if (extraIntent.hasExtra(Router.EXTRA_FROM_MY_VIDEOS)) {
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
            if (environment.getConfig().isVideoTranscriptEnabled()) {
                playerFragment.setTranscriptCallback(this);
            }

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
        if (video.filepath != null && video.filepath.length()>0) {
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

    private boolean isPlayerVisible() {
        return getActivity() != null;
    }

    public void notifyAdapter() {

    }

    @Override
    protected void updateUIForOrientation() {
        //TODO - should we use load different layout file?
        final int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout playerContainer = (LinearLayout)getView().findViewById(R.id.player_container);
            if ( playerContainer != null ) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                float screenHeight = displayMetrics.heightPixels;
                playerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, (int) screenHeight));
                playerContainer.requestLayout();
            }
        } else {
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
        updateUI(orientation);
    }

    @Override
    protected void initTranscriptListView() {
        super.initTranscriptListView();
        transcriptListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Caption currentCaption = transcriptAdapter.getItem(position);
                if (currentCaption != null) {
                    transcriptListView.removeCallbacks(UNFREEZE_AUTO_SCROLL);
                    isTranscriptScrolling = false;

                    transcriptAdapter.unselectAll();
                    transcriptAdapter.select(position);
                    transcriptAdapter.notifyDataSetChanged();
                    playerFragment.seekToCaption(currentCaption);
                }
            }
        });

    }

}
