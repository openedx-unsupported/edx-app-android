package org.edx.mobile.view;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


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
import java.util.Locale;

import subtitleFile.Caption;
import subtitleFile.FormatSRT;
import subtitleFile.TimedTextObject;

public class CourseUnitYoutubeVideoFragment extends CourseUnitVideoFragment implements YouTubePlayer.OnInitializedListener {

    private static final int SUBTITLES_DISPLAY_DELAY_MS = 100;

    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private YouTubePlayer youTubePlayer;
    private Handler subtitleDisplayHandler = new Handler();
    private TimedTextObject subtitlesObj;
    private LinkedHashMap<String, TimedTextObject> srtList = new LinkedHashMap<>();

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
        setHasOptionsMenu(true);
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

        if (!initTranscripts()) {
            /*
             The subtitles are not been loaded the first time that the user watch the video component.
             So this allows to reload the subtitles and reload the menu items after a second.
             */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initTranscripts();
                    getActivity().invalidateOptionsMenu();
                }
            }, 1000);
        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        for (String key : srtList.keySet()) {
            Locale loc = new Locale(key);
            String name = loc.getDisplayLanguage(loc);
            menu.add(name);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        String [] codes = Locale.getISOLanguages();
        for (String code : codes){
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

    private void setSubtitlesObj(){
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

    private boolean initTranscripts(){
        loadTranscriptsData(unit);
        if (subtitlesObj != null) {
            initTranscriptListView();
            updateTranscript(subtitlesObj);
            return true;
        }
        return false;
    }
}
