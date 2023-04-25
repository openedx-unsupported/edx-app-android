package org.edx.mobile.view;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.DeviceSettingUtil;
import org.edx.mobile.util.NetworkUtil;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import subtitleFile.Caption;
import subtitleFile.TimedTextObject;

@AndroidEntryPoint
public class CourseUnitYoutubePlayerFragment extends BaseCourseUnitVideoFragment
        implements YouTubePlayerListener, FullscreenListener {

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youTubePlayer;
    PlayerConstants.PlayerState currentPlayerState = PlayerConstants.PlayerState.UNSTARTED;
    private double currentTimeInSec = 0;
    private int attempts;
    private boolean isFullscreen = false;

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
    public void onResume() {
        super.onResume();
        if (unit != null) {
            setVideoModel();
            initializeYoutubePlayer();
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
    }

    public void initializeYoutubePlayer() {
        try {
            if (getActivity() != null &&
                    NetworkUtil.verifyDownloadPossible((BaseFragmentActivity) getActivity())) {
                downloadTranscript();
                releaseYoutubePlayer();
                youTubePlayerView = new YouTubePlayerView(requireContext());
                youTubePlayerView.setEnableAutomaticInitialization(false);
                youTubePlayerView.addFullscreenListener(this);
                attempts = 0;
                IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder()
                        .controls(1)
                        .fullscreen(1)      // enable full screen button
                        .build();
                youTubePlayerView.initialize(this, false, iFramePlayerOptions);
                addPlayerToScreen(youTubePlayerView);
            }
        } catch (NullPointerException localException) {
            logger.error(localException);
        }
    }

    private void addPlayerToScreen(View youTubePlayerView) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ViewGroup playerContainer = ((ViewGroup) getView().findViewById(R.id.player_container));
        playerContainer.removeAllViews();
        ((ViewGroup) getView().findViewById(R.id.player_container)).addView(youTubePlayerView, params);
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseYoutubePlayer();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
        if (getActivity() == null) {
            return;
        }
        youTubePlayer.addListener(this);
        final int orientation = getActivity().getResources().getConfiguration().orientation;
        int currentPos = 0;
        if (videoModel != null) {
            currentPos = (int) videoModel.getLastPlayedOffset();
        }
        final Uri uri = Uri.parse(unit.getData().encodedVideos.getYoutubeVideoInfo().url);
        /*
         *  Youtube player loads the video using the video id from the url
         *  the url has the following format "https://www.youtube.com/watch?v=3_yD_cEKoCk" where v is the video id
         */
        final String videoId = uri.getQueryParameter("v");
        CourseUnitYoutubePlayerFragment.this.youTubePlayer = youTubePlayer;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            youTubePlayer.toggleFullscreen();
        }
        youTubePlayer.loadVideo(videoId, (float) (currentPos / AppConstants.MILLISECONDS_PER_SECOND));
    }

    @Override
    protected boolean canProcessSubtitles() {
        return youTubePlayer != null && isPlaying();
    }

    @Override
    protected long getPlayerCurrentPosition() {
        return youTubePlayer == null ? 0 : getCurrentTimeMillis();
    }

    @Override
    protected void setFullScreen(boolean fullscreen) {
        if (youTubePlayer != null && DeviceSettingUtil.isDeviceRotationON(getActivity())) {
            final int orientation = getResources().getConfiguration().orientation;
            if ((isFullscreen && orientation == Configuration.ORIENTATION_LANDSCAPE) ||
                    (!isFullscreen && orientation != Configuration.ORIENTATION_LANDSCAPE)) {
                return;
            }
            youTubePlayer.toggleFullscreen();
        }
    }

    @Override
    protected void playPauseVideoPlayback(boolean pauseVideo) {
        if (youTubePlayer != null) {
            if (pauseVideo) {
                youTubePlayer.pause();
            } else {
                youTubePlayer.play();
            }
        }
    }

    protected void updateClosedCaptionData(Caption caption) {
    }

    @Override
    protected void showClosedCaptionData(TimedTextObject subtitles) {
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
                                            unit.getData().encodedVideos.getYoutubeVideoInfo().url, true),
                            getString(R.string.label_ok), null
                    );
        }
    }

    private void releaseYoutubePlayer() {
        if (youTubePlayerView != null && youTubePlayer != null) {
            saveCurrentPlaybackPosition(getCurrentTimeMillis());
            youTubePlayerView.release();
            ((ViewGroup) getView().findViewById(R.id.player_container)).removeAllViews();
            youTubePlayerView = null;
            youTubePlayer = null;
        }
    }

    @Override
    public void seekToCaption(Caption caption) {
        if (youTubePlayer != null) {
            saveCurrentPlaybackPosition(getCurrentTimeMillis());
            if (caption != null) {
                youTubePlayer.seekTo(caption.start.getMseconds() / 1000f);
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

    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        if (getActivity() != null && NetworkUtil.isConnected(getActivity())) {
            initializeYoutubePlayer();
        }
    }

    @Override
    public void onApiChange(@NonNull YouTubePlayer youTubePlayer) {
    }

    @Override
    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float timeInSec) {
        currentTimeInSec = timeInSec;
    }

    @Override
    public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError playerError) {
        /*
         * The most common errorReason is because there is a previous player running so this sets free it
         * and re-initialize the youtube player
         */
        if (attempts <= 3) {
            releaseYoutubePlayer();
            initializeYoutubePlayer();
            attempts++;
        } else {
            redirectToYoutubeDialog();
        }
    }

    @Override
    public void onPlaybackQualityChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackQuality playbackQuality) {
    }

    @Override
    public void onPlaybackRateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlaybackRate playbackRate) {
    }

    @Override
    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState playerState) {
        currentPlayerState = playerState;
        switch (playerState) {
            case ENDED: {
                youTubePlayer.seekTo(0);
                youTubePlayer.pause();
                onPlaybackComplete();
                break;
            }
            case PLAYING: {
                updateTranscriptCallbackStatus(true);
                if (videoModel != null) {
                    environment.getAnalyticsRegistry().trackVideoPlaying(videoModel.videoId,
                            currentTimeInSec, videoModel.eid, videoModel.lmsUrl,
                            Analytics.Values.YOUTUBE);
                }
                break;
            }
            case PAUSED: {
                saveCurrentPlaybackPosition(getPlayerCurrentPosition());
                updateTranscriptCallbackStatus(false);
                if (videoModel != null) {
                    environment.getAnalyticsRegistry().trackVideoPause(videoModel.videoId,
                            currentTimeInSec, videoModel.eid, videoModel.lmsUrl,
                            Analytics.Values.YOUTUBE);
                }
                break;
            }
            case UNKNOWN: {
                redirectToYoutubeDialog();
                break;
            }
        }
    }

    @Override
    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
    }

    @Override
    public void onVideoId(@NonNull YouTubePlayer youTubePlayer, @NonNull String videoId) {
    }

    @Override
    public void onVideoLoadedFraction(@NonNull YouTubePlayer youTubePlayer, float loadedFraction) {
    }

    private boolean isPlaying() {
        return currentPlayerState == PlayerConstants.PlayerState.PLAYING;
    }

    private long getCurrentTimeMillis() {
        return (long) (currentTimeInSec * 1000L);
    }

    @Override
    public void onEnterFullscreen(@NonNull View fullscreenView, @NonNull Function0<Unit> exitFullscreen) {
        isFullscreen = true;
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        addPlayerToScreen(fullscreenView);
        if (videoModel != null) {
            environment.getAnalyticsRegistry().trackVideoOrientation(videoModel.videoId,
                    currentTimeInSec, true, videoModel.eid, videoModel.lmsUrl,
                    Analytics.Values.YOUTUBE);
        }
    }

    @Override
    public void onExitFullscreen() {
        isFullscreen = false;
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        addPlayerToScreen(youTubePlayerView);
        if (videoModel != null) {
            environment.getAnalyticsRegistry().trackVideoOrientation(videoModel.videoId,
                    currentTimeInSec, false, videoModel.eid, videoModel.lmsUrl,
                    Analytics.Values.YOUTUBE);
        }
    }
}
