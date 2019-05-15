package org.edx.mobile.view;

import android.content.pm.ActivityInfo;
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
import android.view.View;
import android.widget.AdapterView;


import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.player.TranscriptManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.TranscriptAdapter;


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

    private YouTubePlayer youTubePlayer, previousYouTubePlayer;
    private Handler subtitleDisplayHandler = new Handler();
    private Handler transcriptsHandler = new Handler();
    private TimedTextObject subtitlesObj;
    private LinkedHashMap<String, TimedTextObject> srtList = new LinkedHashMap<>();
    private YouTubePlayerSupportFragment youTubePlayerFragment;
    private VideoModel videoModel;
    private boolean fromLandscape, wasHiding;

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
        removeFragment();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            removeFragment();
        }

    }

    public void initializeYoutubePlayer() {

        setVideoModel();

        if(youTubePlayer!=null){
            removeFragment();
        }
        youTubePlayerFragment = new YouTubePlayerSupportFragment();
        String apiKey =environment.getConfig().getEmbeddedYoutubeConfig().getYoutubeApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            return;
        }
        youTubePlayerFragment.initialize(apiKey, this);

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
    protected void updateUIForOrientation() {
        final int orientation = getResources().getConfiguration().orientation;
        try {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE && youTubePlayer != null) {
                youTubePlayer.setFullscreen(true);
            } else if (youTubePlayer != null) {
                youTubePlayer.setFullscreen(false);
            }
            updateUI(orientation);
        }
        catch (IllegalStateException e) {
            return;
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
            transcriptsHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initTranscripts();
                    getActivity().invalidateOptionsMenu();
                }
            }, 1000);
        }
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
    public void onPageDisappear() {
        super.onPageDisappear();
        removeFragment();
        fromLandscape = false;
        transcriptsHandler.removeCallbacksAndMessages(null);
        subtitleDisplayHandler.removeCallbacks(subtitlesProcessorRunnable);
    }

    @Override
    public void onInitializationSuccess(Provider provider,
                                        YouTubePlayer player,
                                        boolean wasRestored) {

        try {
            if(!NetworkUtil.verifyDownloadPossible((BaseFragmentActivity) getActivity())){
                player.release();
                return;
            }
        }
        catch (NullPointerException e) {
            player.release();
            return;
        }

        final int orientation = getResources().getConfiguration().orientation;
        int currentPos = 0;
        if (videoModel != null) {
            currentPos = (int) videoModel.getLastPlayedOffset();
        }
        if (!wasRestored ) {
            Uri uri = Uri.parse(unit.getData().encodedVideos.getYoutubeVideoInfo().url);
            String v = uri.getQueryParameter("v");
            player.loadVideo(v, currentPos);
            previousYouTubePlayer = youTubePlayer;
            youTubePlayer = player;
            youTubePlayer.setPlayerStateChangeListener( new StateChangeListener());
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
                int currentPos = 0;
                try {
                    currentPos = youTubePlayer.getCurrentTimeMillis();
                    DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE ).updateVideoLastPlayedOffset(unit.getId(), currentPos, null);

                }
                catch (Exception e ){
                    return;
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

    private void removeFragment(){
        try {
            if (youTubePlayer != null){
                youTubePlayer.release();
            }

            if (youTubePlayerFragment != null){
                getChildFragmentManager().beginTransaction().remove(youTubePlayerFragment).commit();
            } else {
                getChildFragmentManager().beginTransaction().replace(R.id.player_container, new Fragment()).commit();
            }

        } catch (IllegalStateException ex) {
            logger.error(ex);
        }
    }

    @Override
    protected void initTranscriptListView(){
        transcriptAdapter = new TranscriptAdapter(getContext(), environment);
        transcriptListView.setAdapter(transcriptAdapter);
        transcriptListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Caption currentCaption = transcriptAdapter.getItem(position);
                if (currentCaption != null) {
                    youTubePlayer.seekToMillis(currentCaption.start.getMseconds());

                }
            }
        });
    }

    private void setVideoModel(){
        videoModel = DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE ).getVideoEntryByVideoId(unit.getId(), null);

        if (videoModel == null) {
            DownloadEntry e = new DownloadEntry();
            e.videoId = unit.getId();
            addVideoDatatoDb(e);
            videoModel = DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE ).getVideoEntryByVideoId(unit.getId(), null);
        }
    }

    private  class StateChangeListener implements YouTubePlayer.PlayerStateChangeListener {
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
            if (previousYouTubePlayer != null){
                previousYouTubePlayer.release();
            }
            youTubePlayer.release();
            youTubePlayer = null;
            initializeYoutubePlayer();
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
            if (wasHiding) {
                youTubePlayer.play();
                wasHiding = false;
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
