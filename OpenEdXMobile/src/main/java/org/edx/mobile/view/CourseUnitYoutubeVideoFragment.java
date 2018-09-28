package org.edx.mobile.view;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


import org.edx.mobile.R;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.player.TranscriptManager;


import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;

import subtitleFile.Caption;
import subtitleFile.FormatSRT;
import subtitleFile.TimedTextObject;

public class CourseUnitYoutubeVideoFragment extends CourseUnitVideoFragment implements YouTubePlayer.OnInitializedListener {

    private static final int SUBTITLES_DISPLAY_DELAY_MS = 100;

    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private YouTubePlayer youTubePlayer;
    private Handler subtitleDisplayHandler = new Handler();
    private TimedTextObject subtitlesObj;

    /**
     * Create a new instance of fragment
     */

    public static CourseUnitYoutubeVideoFragment newInstance(VideoBlockModel unit, boolean hasNextUnit, boolean hasPreviousUnit) {
        CourseUnitYoutubeVideoFragment f = new CourseUnitYoutubeVideoFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putBoolean(HAS_NEXT_UNIT_ID, hasNextUnit);
        args.putBoolean(HAS_PREV_UNIT_ID, hasPreviousUnit);
        f.setArguments(args);

        return f;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSubtitlesObj(unit);

        if (subtitlesObj != null) {
            initTranscriptListView();
            updateTranscript(subtitlesObj);
        }

    }

    public void initializeYoutubePlayer() {
        youTubePlayerFragment = new YouTubePlayerSupportFragment();
        youTubePlayerFragment.initialize(environment.getConfig().getEmbeddedYoutubeConfig().getYoutubeApiKey(), this);

        try {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.player_container, youTubePlayerFragment, "player");
            ft.commit();
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUIForOrientation();
    }

    private void updateUIForOrientation() {
        final int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE && youTubePlayer != null) {
            youTubePlayer.setFullscreen(true);
        } else if (youTubePlayer != null) {
            youTubePlayer.setFullscreen(false);
        }
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        initializeYoutubePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subtitleDisplayHandler.removeCallbacks(subtitlesProcessorRunnable);
    }

    @Override
    public void onPageDisappear() {
        super.onPageDisappear();
        try {
            getChildFragmentManager().beginTransaction().replace(R.id.player_container, new Fragment()).commit();
        } catch (IllegalStateException ex) {
            logger.error(ex);
        }
        subtitleDisplayHandler.removeCallbacks(subtitlesProcessorRunnable);
    }

    @Override
    public void onInitializationSuccess(Provider provider,
                                        YouTubePlayer player,
                                        boolean wasRestored) {
        final int orientation = getResources().getConfiguration().orientation;
        if (!wasRestored) {
            Uri uri = Uri.parse(unit.getData().encodedVideos.getYoutubeVideoInfo().url);
            String v = uri.getQueryParameter("v");
            player.loadVideo(v);
            youTubePlayer = player;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                youTubePlayer.setFullscreen(true);
            }
        }
        subtitleDisplayHandler = new Handler();
        subtitleDisplayHandler.post(subtitlesProcessorRunnable);

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

    private void setSubtitlesObj(VideoBlockModel unit) {
        LinkedHashMap<String, TimedTextObject> srtList = new LinkedHashMap<>();
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
                        subtitlesObj = srtList.entrySet().iterator().next().getValue();
                    }
                }
            }

        } catch (Exception localException) {
            logger.error(localException);
        }
    }
}
