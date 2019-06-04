package org.edx.mobile.view;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.player.TranscriptManager;
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

    private YouTubePlayer youTubePlayer, previousYouTubePlayer;
    private Handler subtitleDisplayHandler = new Handler();
    private Handler transcriptsHandler = new Handler();
    private Handler initializeHandler = new Handler();
    private TimedTextObject subtitlesObj;
    private LinkedHashMap<String, TimedTextObject> srtList = new LinkedHashMap<>();
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private boolean fromLandscape, wasHiding;
    private int attempts;

    /**
     * Create a new instance of fragment
     */

    public static CourseUnitYoutubeVideoFragment newInstance(VideoBlockModel unit, boolean hasNextUnit, boolean hasPreviousUnit) {
        CourseUnitYoutubeVideoFragment fragment = new CourseUnitYoutubeVideoFragment();
        fragment.setArguments(getCourseUnitBundle(unit, hasNextUnit, hasPreviousUnit));
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        releaseYoutubePlayer();
        if (VideoUtil.isAPIYoutubeSupported(getContext())) {
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
             * This method is not called  property when the user leaves quickly the view on the view pager
             * so the youtube player can not be released( only one youtube player instance is allowed by the library)
             * so in order to avoid to create multiple youtube player instances, the youtube player only will be initialize
             * after a second and if the view is visible to the user.
             */
            initializeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initializeYoutubePlayer();
                }
            }, 1000);
        } else {
            releaseYoutubePlayer();
            fromLandscape = false;
            transcriptsHandler.removeCallbacksAndMessages(null);
            subtitleDisplayHandler.removeCallbacks(subtitlesProcessorRunnable);
            initializeHandler.removeCallbacks(null);
        }
    }

    public void initializeYoutubePlayer() {
        if (getUserVisibleHint() && youTubePlayerFragment != null && NetworkUtil.verifyDownloadPossible((BaseFragmentActivity) getActivity())) {

            initTranscripts();
            String apiKey = environment.getConfig().getEmbeddedYoutubeConfig().getYoutubeApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                return;
            }
            youTubePlayerFragment.initialize(apiKey, this);
        }
    }

    @Override
    protected void updateUIForOrientation() {
        final int orientation = getResources().getConfiguration().orientation;
        if (youTubePlayer != null) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                youTubePlayer.setFullscreen(true);
            } else {
                youTubePlayer.setFullscreen(false);
            }
        }
        updateUI(orientation);

    }

    @Override
    public void onStop() {
        super.onStop();
        wasHiding = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subtitleDisplayHandler.removeCallbacks(subtitlesProcessorRunnable);
    }

    @Override
    public void onInitializationSuccess(Provider provider,
                                        YouTubePlayer player,
                                        boolean wasRestored) {

        final int orientation = getResources().getConfiguration().orientation;
        int currentPos = 0;
        if (videoModel != null) {
            currentPos = (int) videoModel.getLastPlayedOffset();
        }
        if (!wasRestored) {
            Uri uri = Uri.parse(unit.getData().encodedVideos.getYoutubeVideoInfo().url);
            /*
             *  Youtube player loads the video using the video id from the url
             *  the url has the following format "https://www.youtube.com/watch?v=3_yD_cEKoCk" where v is the video id
             */
            final String videoId = uri.getQueryParameter("v");
            player.loadVideo(videoId, currentPos);
            previousYouTubePlayer = youTubePlayer;
            youTubePlayer = player;
            youTubePlayer.setPlayerStateChangeListener(new StateChangeListener());
            youTubePlayer.setPlaybackEventListener(new PlaybackListener());
            youTubePlayer.setOnFullscreenListener(new FullscreenListener());
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                youTubePlayer.setFullscreen(true);
            }
        }
        subtitleDisplayHandler = new Handler();
        subtitleDisplayHandler.postDelayed(subtitlesProcessorRunnable, SUBTITLES_DISPLAY_DELAY_MS);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        for (String key : srtList.keySet()) {
            Locale loc = new Locale(key);
            String name = loc.getDisplayLanguage(loc);
            menu.add(name);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String[] codes = Locale.getISOLanguages();
        for (String code : codes) {
            Locale loc = new Locale(code);
            if (item.toString().equals(loc.getDisplayName(loc))) {
                subtitlesObj = srtList.get(code);
                initTranscriptListView();
                updateTranscript(subtitlesObj);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult result) {
    }

    private Runnable subtitlesProcessorRunnable = new Runnable() {
        @Override
        public void run() {
            if (youTubePlayer != null) {
                int currentPos = youTubePlayer.getCurrentTimeMillis();
                if (currentPos >= 0 && youTubePlayer.isPlaying()) {
                    saveCurrentPlaybackPosition(currentPos);
                } else {
                    currentPos = 0;
                }

                if (subtitlesObj != null) {
                    Collection<Caption> subtitles = subtitlesObj.captions.values();
                    int currentSubtitleIndex = 0;
                    for (Caption subtitle : subtitles) {
                        int startMillis = subtitle.start.getMseconds();
                        int endMillis = subtitle.end.getMseconds();
                        if (currentPos >= startMillis && currentPos <= endMillis) {
                            updateSelection(currentSubtitleIndex);
                            break;
                        }
                        currentSubtitleIndex++;
                    }
                }
            }
            subtitleDisplayHandler.postDelayed(this, SUBTITLES_DISPLAY_DELAY_MS);
        }
    };

    private void loadTranscriptsData(VideoBlockModel unit) {
        TranscriptManager transcriptManager = new TranscriptManager(getContext());
        TranscriptModel transcript = getTranscriptModel(unit.getDownloadEntry(environment.getStorage()));
        transcriptManager.downloadTranscriptsForVideo(transcript);

        try {
            LinkedHashMap<String, InputStream> localHashMap = transcriptManager
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
                if (!srtList.entrySet().isEmpty()) {
                    setSubtitlesObj();
                }
            }

        } catch (Exception localException) {
            logger.error(localException);
        }
    }

    private void setSubtitlesObj() {
        String key = Locale.getDefault().getLanguage();
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
        loadTranscriptsData(unit);

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
    protected void initTranscriptListView() {
        super.initTranscriptListView();
        transcriptListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Caption currentCaption = transcriptAdapter.getItem(position);
                if (currentCaption != null && youTubePlayer != null) {
                    youTubePlayer.seekToMillis(currentCaption.start.getMseconds());
                }
            }
        });
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
            /**
             * The most common errorReason is because there is a previous player running so this sets free it
             * and reloads the fragment
             */
            if (attempts <= 3) {
                if (previousYouTubePlayer != null) {
                    previousYouTubePlayer.release();
                }
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
            if (getActivity() != null && !fromLandscape) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {
            if (wasHiding && getUserVisibleHint()) {
                /*
                 * wasHiding is set on true when the app comes to background from foreground
                 * so this allow to play a video when the app comes to foreground from background
                 */
                try {
                    wasHiding = false;
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
            if (!fullScreen && getActivity() != null && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                fromLandscape = true;
            } else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        }
    }
}
