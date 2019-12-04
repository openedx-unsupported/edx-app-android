package org.edx.mobile.view;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.player.TranscriptManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.VideoUtil;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;

import subtitleFile.Caption;
import subtitleFile.FormatSRT;
import subtitleFile.TimedTextObject;

public class CourseUnitYoutubeVideoFragment extends CourseUnitVideoFragment implements YouTubePlayer.OnInitializedListener {

    private static final int SUBTITLES_DISPLAY_DELAY_MS = 50;

    private YouTubePlayer youTubePlayer;
    private Handler subtitleDisplayHandler = new Handler();
    private Handler initializeHandler = new Handler();
    private Handler subtitleFetchHandler = new Handler();

    private TimedTextObject subtitlesObj;
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    @Inject
    private TranscriptManager transcriptManager;
    /**
     * isInForeground is set on false when the app comes to background from foreground
     * so this allow to play a video when the app comes to foreground from background
     */
    private boolean isInForeground = true;
    private int attempts;

    private Runnable subtitlesProcessorRunnable = () -> {
        if (youTubePlayer != null) {
            int currentPos = youTubePlayer.getCurrentTimeMillis();
            if (currentPos >= 0 && youTubePlayer.isPlaying()) {
                saveCurrentPlaybackPosition(currentPos);
            } else {
                currentPos = 0;
            }

            if (subtitlesObj != null) {
                final Collection<Caption> subtitles = subtitlesObj.captions.values();
                int currentSubtitleIndex = 0;
                for (Caption subtitle : subtitles) {
                    final int startMillis = subtitle.start.getMseconds();
                    final int endMillis = subtitle.end.getMseconds();
                    if (currentPos >= startMillis && currentPos <= endMillis) {
                        updateSelection(currentSubtitleIndex);
                        break;
                    }
                    currentSubtitleIndex++;
                }
            }
        }
        subtitleDisplayHandler.postDelayed(this.subtitlesProcessorRunnable, SUBTITLES_DISPLAY_DELAY_MS);
    };
    private TranscriptModel transcript;
    /**
     * This runnable is used the fetch the Subtitle in TimedTextObject
     */
    private Runnable subtitleFetchProcessor = new Runnable() {
        public void run() {
            LinkedHashMap<String, TimedTextObject> srtList = new LinkedHashMap<>();
            try {
                final LinkedHashMap<String, InputStream> localHashMap = transcriptManager
                        .fetchTranscriptsForVideo(transcript);

                if (localHashMap != null) {
                    for (String thisKey : localHashMap.keySet()) {
                        InputStream localInputStream = localHashMap.get(thisKey);
                        if (localInputStream != null) {
                            TimedTextObject localTimedTextObject =
                                    new FormatSRT().parseFile("temp.srt", localInputStream);
                            srtList.put(thisKey, localTimedTextObject);
                            localInputStream.close();
                        }
                    }

                    if (srtList.size() == 0) {
                        subtitleFetchHandler.postDelayed(subtitleFetchProcessor, 100);
                    } else {
                        setSubtitlesObj(srtList);
                        initTranscripts();
                    }
                } else {
                    subtitleFetchHandler.postDelayed(subtitleFetchProcessor, 1000);
                }
            } catch (Exception localException) {
                logger.error(localException);
            }

        }
    };

    /**
     * Create a new instance of fragment
     */
    public static CourseUnitYoutubeVideoFragment newInstance(VideoBlockModel unit, boolean hasNextUnit, boolean hasPreviousUnit) {
        final CourseUnitYoutubeVideoFragment fragment = new CourseUnitYoutubeVideoFragment();
        fragment.setArguments(getCourseUnitBundle(unit, hasNextUnit, hasPreviousUnit));
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && unit != null) {
            setVideoModel();
            /*
             * This method is not called property when the user leaves quickly the view on the view pager
             * so the youtube player can not be released( only one youtube player instance is allowed by the library)
             * so in order to avoid to create multiple youtube player instances, the youtube player only will be initialize
             * after a second and if the view is visible to the user.
             */
            initializeHandler.postDelayed(this::initializeYoutubePlayer, 1000);
        } else {
            releaseYoutubePlayer();
            subtitleDisplayHandler.removeCallbacks(subtitlesProcessorRunnable);
            initializeHandler.removeCallbacks(null);
            subtitleFetchHandler.removeCallbacks(null);
        }
    }

    public void initializeYoutubePlayer() {
        try {
            if (getUserVisibleHint() && youTubePlayerFragment != null && NetworkUtil.verifyDownloadPossible((BaseFragmentActivity) getActivity())) {
                loadTranscriptsData();
                String apiKey = environment.getConfig().getYoutubeInAppPlayerConfig().getYoutubePlayerApiKey();
                if (apiKey == null || apiKey.isEmpty()) {
                    return;
                }
                youTubePlayerFragment.initialize(apiKey, this);
            }
        } catch (NullPointerException localException) {
            logger.error(localException);
        }

    }

    @Override
    protected void updateUIForOrientation() {
        final int orientation = getResources().getConfiguration().orientation;
        final LinearLayout playerContainer = getView().findViewById(R.id.player_container);
        if (playerContainer != null) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                float screenHeight = displayMetrics.heightPixels;
                playerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, (int) screenHeight));
                if (youTubePlayer != null) {
                    youTubePlayer.setFullscreen(true);
                }
            } else {
                float screenWidth = displayMetrics.widthPixels;
                float ideaHeight = screenWidth * 9 / 16;
                playerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, (int) ideaHeight));
                if (youTubePlayer != null) {
                    youTubePlayer.setFullscreen(false);
                }
            }
            playerContainer.requestLayout();
        }
        updateUI(orientation);
    }

    @Override
    public void onStop() {
        super.onStop();
        isInForeground = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subtitleFetchHandler.removeCallbacks(subtitleFetchProcessor);
        subtitleDisplayHandler.removeCallbacks(subtitlesProcessorRunnable);
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
    }

    private void loadTranscriptsData() {
        final TranscriptManager transcriptManager = new TranscriptManager(getContext());
        transcript = getTranscriptModel();
        transcriptManager.downloadTranscriptsForVideo(transcript);
        subtitleFetchHandler.post(subtitleFetchProcessor);
    }

    private void setSubtitlesObj(LinkedHashMap<String, TimedTextObject> srtList) {
        String key = Locale.getDefault().getLanguage();
        // Android return iw in case of Hebrew
        if (key.equals("iw")) {
            key = "he";
        }
        if (srtList.containsKey(key)) {
            subtitlesObj = srtList.get(key);
        } else if (srtList.containsKey("en")) {
            subtitlesObj = srtList.get("en");
        } else {
            subtitlesObj = srtList.entrySet().iterator().next().getValue();
        }
    }

    private void initTranscripts() {
        if (subtitlesObj != null) {
            initTranscriptListView();
            updateTranscript(subtitlesObj);
        }
    }

    private void releaseYoutubePlayer() {
        if (youTubePlayer != null) {
            youTubePlayer.release();
            youTubePlayer = null;
        }
    }

    @Override
    public void seekToCaption(Caption caption) {
        if (caption != null && youTubePlayer != null) {
            youTubePlayer.seekToMillis(caption.start.getMseconds());
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
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            /*
             * The most common errorReason is because there is a previous player running so this sets free it
             * and reloads the fragment
             */
            if (attempts <= 3) {
                releaseYoutubePlayer();
                initializeYoutubePlayer();
                attempts++;
            } else {
                Toast.makeText(getActivity(), errorReason.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class PlaybackListener implements YouTubePlayer.PlaybackEventListener {

        @Override
        public void onPlaying() {
            subtitleDisplayHandler.post(subtitlesProcessorRunnable);
            environment.getAnalyticsRegistry().trackVideoPlaying(videoModel.videoId,
                    youTubePlayer.getCurrentTimeMillis() / AppConstants.MILLISECONDS_PER_SECOND,
                    videoModel.eid, videoModel.lmsUrl, Analytics.Values.YOUTUBE);
        }

        @Override
        public void onPaused() {
            subtitleDisplayHandler.removeCallbacks(subtitlesProcessorRunnable);
            environment.getAnalyticsRegistry().trackVideoPause(videoModel.videoId,
                    youTubePlayer.getCurrentTimeMillis() / AppConstants.MILLISECONDS_PER_SECOND,
                    videoModel.eid, videoModel.lmsUrl, Analytics.Values.YOUTUBE);
        }

        @Override
        public void onStopped() {
            if (!isInForeground && getUserVisibleHint()) {
                /*
                 * isInForeground is set on false when the app comes to background from foreground
                 * so this allow to play a video when the app comes to foreground from background
                 */
                try {
                    isInForeground = true;
                    youTubePlayer.play();
                } catch (Exception error) {
                    initializeYoutubePlayer();
                }
            }
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
