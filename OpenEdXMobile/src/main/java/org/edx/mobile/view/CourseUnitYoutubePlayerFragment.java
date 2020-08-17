package org.edx.mobile.view;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.VideoUtil;

import de.greenrobot.event.EventBus;
import subtitleFile.Caption;
import subtitleFile.TimedTextObject;

public class CourseUnitYoutubePlayerFragment extends BaseCourseUnitVideoFragment implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayer youTubePlayer;
    private Handler initializeHandler = new Handler();
    private YouTubePlayerSupportFragment youTubePlayerFragment;

    private int attempts;

    /**
     * Create a new instance of fragment
     */
    public static CourseUnitYoutubePlayerFragment newInstance(VideoBlockModel unit) {
        final CourseUnitYoutubePlayerFragment fragment = new CourseUnitYoutubePlayerFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        releaseYoutubePlayer();
        if (VideoUtil.isYoutubeAPISupported(getContext())) {
            youTubePlayerFragment = new YouTubePlayerSupportFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.player_container, youTubePlayerFragment, "player").commit();
        }
        attempts = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (unit != null) {
            setVideoModel();
            /*
             * This method is not called property when the user leaves quickly the view on the view pager
             * so the youtube player can not be released( only one youtube player instance is allowed by the library)
             * so in order to avoid to create multiple youtube player instances, the youtube player only will be initialize
             * after minor delay.
             */
            initializeHandler.post(this::initializeYoutubePlayer);
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseYoutubePlayer();
        initializeHandler.removeCallbacks(null);
        EventBus.getDefault().unregister(this);
    }

    public void initializeYoutubePlayer() {
        try {
            if (getActivity() != null && youTubePlayerFragment != null &&
                    NetworkUtil.verifyDownloadPossible((BaseFragmentActivity) getActivity())) {
                downloadTranscript();
                String apiKey = environment.getConfig().getYoutubePlayerConfig().getApiKey();
                if (apiKey == null || apiKey.isEmpty()) {
                    logger.error(new Throwable("YOUTUBE_IN_APP_PLAYER:API_KEY is missing or empty"));
                    return;
                }
                youTubePlayerFragment.initialize(apiKey, this);
            }
        } catch (NullPointerException localException) {
            logger.error(localException);
        }
    }

    @Override
    protected boolean canProcessSubtitles() {
        return youTubePlayer != null && youTubePlayer.isPlaying();
    }

    @Override
    protected long getPlayerCurrentPosition() {
        return youTubePlayer == null ? 0 : youTubePlayer.getCurrentTimeMillis();
    }

    @Override
    protected void setFullScreen(boolean fullscreen) {
        /*
         * If the youtube player is not in a proper state then it throws the IllegalStateException.
         * To avoid the crash and continue the flow we are reinitializing the player here.
         *
         * It may occur when the edX app was in background and user kills the on-device YouTube app.
         */
        if (youTubePlayer != null) {
            try {
                youTubePlayer.setFullscreen(fullscreen);
            } catch (IllegalStateException e) {
                logger.error(e);
                releaseYoutubePlayer();
                initializeYoutubePlayer();
            }
        }
    }

    protected void updateClosedCaptionData(Caption caption) {
    }

    @Override
    protected void showClosedCaptionData(TimedTextObject subtitles) {
    }

    @Override
    public void onInitializationSuccess(Provider provider,
                                        YouTubePlayer player,
                                        boolean wasRestored) {
        if (getActivity() == null) {
            return;
        }
        final int orientation = getActivity().getResources().getConfiguration().orientation;
        int currentPos = 0;
        if (videoModel != null) {
            currentPos = (int) videoModel.getLastPlayedOffset();
        }
        if (!wasRestored) {
            final Uri uri = Uri.parse(unit.getData().encodedVideos.getYoutubeVideoInfo().url);
            /*
             *  Youtube player loads the video using the video id from the url
             *  the url has the following format "https://www.youtube.com/watch?v=3_yD_cEKoCk" where v is the video id
             */
            final String videoId = uri.getQueryParameter("v");
            player.loadVideo(videoId, currentPos);
            youTubePlayer = player;
            youTubePlayer.setPlayerStateChangeListener(new StateChangeListener());
            youTubePlayer.setPlaybackEventListener(new PlaybackListener());
            youTubePlayer.setOnFullscreenListener(new FullscreenListener());
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                youTubePlayer.setFullscreen(true);
            }
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult result) {
        redirectToYoutubeDialog();
    }

    private void redirectToYoutubeDialog() {
        releaseYoutubePlayer();
        if (getActivity() != null && !getActivity().isDestroyed()) {
            ((BaseFragmentActivity) getActivity())
                    .showAlertDialog(
                            getString(R.string.assessment_unable_to_play_video),
                            getString(R.string.assessment_unable_to_play_video_message),
                            getString(R.string.assessment_open_on_youtube),
                            (dialog, which) -> BrowserUtil
                                    .open(getActivity(),
                                            unit.getData().encodedVideos.getYoutubeVideoInfo().url),
                            getString(R.string.label_ok), null
                    );
        }
    }

    private void releaseYoutubePlayer() {
        if (youTubePlayer != null) {
            saveCurrentPlaybackPosition(youTubePlayer.getCurrentTimeMillis());
            youTubePlayer.release();
            youTubePlayer = null;
        }
    }

    @Override
    public void seekToCaption(Caption caption) {
        if (youTubePlayer != null) {
            saveCurrentPlaybackPosition(youTubePlayer.getCurrentTimeMillis());
            if (caption != null) {
                youTubePlayer.seekToMillis(caption.start.getMseconds());
            }
        }
    }

    private void setVideoModel() {
        videoModel = (DownloadEntry) DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE).getVideoEntryByVideoId(unit.getId(), null);

        if (videoModel == null) {
            DownloadEntry e = new DownloadEntry();
            e.videoId = unit.getId();
            addVideoDatatoDb(e);
            videoModel = e;
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        if (getActivity() != null && NetworkUtil.isConnected(getActivity())) {
            initializeYoutubePlayer();
        }
    }

    private class StateChangeListener implements YouTubePlayer.PlayerStateChangeListener {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {
            youTubePlayer.seekToMillis(0);
            youTubePlayer.pause();
            onPlaybackComplete();
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            /*
             * The most common errorReason is because there is a previous player running so this sets free it
             * and reloads the fragment
             */
            if (attempts <= 3) {
                releaseYoutubePlayer();
                initializeHandler.postDelayed(CourseUnitYoutubePlayerFragment.this::initializeYoutubePlayer, 500);
                attempts++;
            } else {
                redirectToYoutubeDialog();
            }
        }
    }

    private class PlaybackListener implements YouTubePlayer.PlaybackEventListener {

        @Override
        public void onPlaying() {
            updateTranscriptCallbackStatus(true);
            environment.getAnalyticsRegistry().trackVideoPlaying(videoModel.videoId,
                    youTubePlayer.getCurrentTimeMillis() / AppConstants.MILLISECONDS_PER_SECOND,
                    videoModel.eid, videoModel.lmsUrl, Analytics.Values.YOUTUBE);
        }

        @Override
        public void onPaused() {
            saveCurrentPlaybackPosition(getPlayerCurrentPosition());
            updateTranscriptCallbackStatus(false);
            environment.getAnalyticsRegistry().trackVideoPause(videoModel.videoId,
                    youTubePlayer.getCurrentTimeMillis() / AppConstants.MILLISECONDS_PER_SECOND,
                    videoModel.eid, videoModel.lmsUrl, Analytics.Values.YOUTUBE);
        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    }

    private class FullscreenListener implements YouTubePlayer.OnFullscreenListener {
        @Override
        public void onFullscreen(boolean fullScreen) {
            final int orientation = getResources().getConfiguration().orientation;
            if (getActivity() != null) {
                if (!fullScreen && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            }
            if (videoModel != null) {
                environment.getAnalyticsRegistry().trackVideoOrientation(videoModel.videoId,
                        youTubePlayer.getCurrentTimeMillis() / AppConstants.MILLISECONDS_PER_SECOND,
                        fullScreen, videoModel.eid, videoModel.lmsUrl, Analytics.Values.YOUTUBE);
            }
        }
    }
}
