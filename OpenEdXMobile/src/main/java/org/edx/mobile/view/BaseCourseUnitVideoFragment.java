package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.http.callback.Callback;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.player.IPlayerEventCallback;
import org.edx.mobile.player.TranscriptListener;
import org.edx.mobile.player.TranscriptManager;
import org.edx.mobile.util.LocaleUtils;
import org.edx.mobile.view.adapters.TranscriptAdapter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import subtitleFile.Caption;
import subtitleFile.TimedTextObject;

/**
 * Base class implementing the basic functionality of the video player screen.
 * It handles UI updating.
 * It downloads, displays and maintains the transcripts based on player position.
 */
public abstract class BaseCourseUnitVideoFragment extends CourseUnitFragment
        implements IPlayerEventCallback, TranscriptListener {
    /**
     * This method use to seek the player to the particular position based on the given caption
     *
     * @param caption contains the {@link Caption} info
     */
    protected abstract void seekToCaption(Caption caption);

    /**
     * This method is used to check either player is init, and can process subtitles on the basis of
     * current status of player.
     */
    protected abstract boolean canProcessSubtitles();

    /**
     * This method used to get the current position of the media player.
     */
    protected abstract long getPlayerCurrentPosition();

    /**
     * This method used to update the close caption in player screen.
     */
    protected abstract void updateClosedCaptionData(Caption caption);

    /**
     * This method send the downloaded transcript to the player screen.
     */
    protected abstract void showClosedCaptionData(TimedTextObject subtitles);

    protected final static Logger logger = new Logger(BaseCourseUnitVideoFragment.class.getName());
    private final static int UNFREEZE_AUTOSCROLL_DELAY_MS = 3500;
    private static final int SUBTITLES_DISPLAY_DELAY_MS = 100;

    protected DownloadEntry videoModel;

    protected VideoBlockModel unit;
    private TimedTextObject subtitlesObj;

    protected ListView transcriptListView;
    protected TranscriptAdapter transcriptAdapter;

    private View messageContainer;

    // Defines if the user is scrolling the transcript listview
    protected boolean isTranscriptScrolling = false;
    // Top offset to centralize the currently active transcript item in the listview
    private float topOffset = 0;

    @Inject
    LoginPrefs loginPrefs;
    @Inject
    private CourseAPI courseApi;
    @Inject
    private TranscriptManager transcriptManager;

    private ViewTreeObserver.OnGlobalLayoutListener transcriptListLayoutListener;

    private Handler subtitleDisplayHandler = new Handler();

    /**
     * This runnable handles the displaying of
     * Subtitles on the screen per 100 mili seconds
     */
    private Runnable subtitlesProcessorRunnable = () -> {
        if (canProcessSubtitles()) {
            long currentPos = getPlayerCurrentPosition();
            if (subtitlesObj != null) {
                Collection<Caption> subtitles = subtitlesObj.captions.values();
                int currentSubtitleIndex = 0;
                for (Caption subtitle : subtitles) {
                    int startMillis = subtitle.start.getMseconds();
                    int endMillis = subtitle.end.getMseconds();
                    if (currentPos >= startMillis && currentPos <= endMillis) {
                        String subtitleLang = loginPrefs.getSubtitleLanguage();
                        if (subtitleLang == null || !subtitleLang.equalsIgnoreCase(getString(R.string.lbl_cc_none))) {
                            updateClosedCaptionData(subtitle);
                        }
                        updateSelection(currentSubtitleIndex);
                        break;
                    } else if (currentPos > endMillis) {
                        updateClosedCaptionData(null);
                    }
                    currentSubtitleIndex++;
                }
            } else {
                updateClosedCaptionData(null);
            }
        }
        // Only Allow handler to post the runnable when fragment is visible to user
        if (isVisible()) {
            subtitleDisplayHandler.postDelayed(this.subtitlesProcessorRunnable, SUBTITLES_DISPLAY_DELAY_MS);
        } else {
            updateClosedCaptionData(null);
        }
    };

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
        View view = inflater.inflate(R.layout.fragment_course_unit_video, container, false);
        messageContainer = view.findViewById(R.id.message_container);
        transcriptListView = view.findViewById(R.id.transcript_listview);
        if (showCastMiniController()) {
            view.findViewById(R.id.fl_mini_controller).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.fl_mini_controller).setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restore(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && unit == null) {
            updateTranscriptCallbackStatus(false);
        }
    }

    @Override
    public void downloadTranscript() {
        Activity activity = getActivity();
        if (activity != null) {
            TranscriptModel transcript = getTranscriptModel();
            String transcriptUrl = LocaleUtils.getTranscriptURL(activity, transcript);
            transcriptManager.downloadTranscriptsForVideo(transcriptUrl, (TimedTextObject transcriptTimedTextObject) -> {
                subtitlesObj = transcriptTimedTextObject;
                if (!activity.isDestroyed()) {
                    initTranscripts();
                }
            });
        }
    }

    @Override
    public void updateTranscriptCallbackStatus(boolean attach) {
        if (subtitleDisplayHandler != null) {
            if (attach) {
                subtitleDisplayHandler.post(subtitlesProcessorRunnable);
            } else {
                subtitleDisplayHandler.removeCallbacks(subtitlesProcessorRunnable);
            }
        }
    }

    private void initTranscripts() {
        if (subtitlesObj != null) {
            initTranscriptListView();
            updateTranscript(subtitlesObj);
            String subtitleLanguage = LocaleUtils.getCurrentDeviceLanguage(getActivity());
            if (loginPrefs.getSubtitleLanguage() == null &&
                    !android.text.TextUtils.isEmpty(subtitleLanguage) &&
                    getTranscriptModel().containsKey(subtitleLanguage)) {
                loginPrefs.setSubtitleLanguage(subtitleLanguage);
            }
            showClosedCaptionData(subtitlesObj);
        }
    }

    protected TranscriptModel getTranscriptModel() {
        TranscriptModel transcript = null;
        if (unit != null && unit.getData() != null &&
                unit.getData().transcripts != null) {
            transcript = unit.getData().transcripts;
        }
        return transcript;
    }

    @Override
    public void onStop() {
        super.onStop();
        transcriptListView.getViewTreeObserver().removeOnGlobalLayoutListener(transcriptListLayoutListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        transcriptManager.cancelTranscriptDownloading();
        updateTranscriptCallbackStatus(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUIForOrientation();
    }

    private void updateUIForOrientation() {
        final LinearLayout playerContainer = getView().findViewById(R.id.player_container);
        final int orientation = getResources().getConfiguration().orientation;
        if (playerContainer != null) {
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                float screenHeight = displayMetrics.heightPixels;
                playerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, (int) screenHeight));
                setFullScreen(true);
            } else {
                float screenWidth = displayMetrics.widthPixels;
                float ideaHeight = screenWidth * 9 / 16;
                playerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, (int) ideaHeight));
                setFullScreen(false);
            }
            playerContainer.requestLayout();
        }
        updateUI(orientation);
    }

    /**
     * Method to set up the full screen mode of the media player
     *
     * @param fullscreen true if need to enable full screen, otherwise false
     */
    protected void setFullScreen(boolean fullscreen) {
    }

    public void markPlaying() {
        environment.getStorage().markVideoPlaying(videoModel, watchedStateCallback);
    }

    /**
     * This method inserts the Download Entry Model in the database
     * Called when a user clicks on a Video in the list
     *
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

    public void saveCurrentPlaybackPosition(long offset) {
        try {
            DownloadEntry v = videoModel;
            if (v != null) {
                // mark this as partially watches, as playing has started
                DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE).updateVideoLastPlayedOffset(v.videoId, offset,
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
        DownloadEntry v = videoModel;
        if (v != null && v.watched == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
            videoModel.watched = DownloadEntry.WatchedState.WATCHED;
            // mark this as watched, as the playback has ended
            DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE)
                    .updateVideoWatchedState(v.videoId, DownloadEntry.WatchedState.WATCHED,
                            watchedStateCallback);
        }

        courseApi.markBlocksCompletion(unit.getCourseId(), new String[]{unit.getId()}).enqueue(new Callback<JSONObject>() {
            @Override
            protected void onResponse(@NonNull JSONObject responseBody) {
                // Nothing to do here
            }
        });
        // mark offset as zero, so that playback will resume from start next time
        saveCurrentPlaybackPosition(0);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUIForOrientation();
    }

    protected void updateUI(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            messageContainer.setVisibility(View.GONE);
            transcriptListView.setVisibility(View.GONE);
        } else {
            if (transcriptAdapter == null) {
                messageContainer.setVisibility(View.VISIBLE);
                transcriptListView.setVisibility(View.GONE);
                initTranscriptListView();
            } else {
                messageContainer.setVisibility(View.GONE);
                transcriptListView.setVisibility(View.VISIBLE);
                // Calculating the offset required for centralizing the current transcript item
                // p.s. Without this listener the getHeight function returns 0
                transcriptListLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        transcriptListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        final float transcriptRowHeight = getResources().getDimension(R.dimen.transcript_row_height);
                        final float listviewHeight = transcriptListView.getHeight();
                        topOffset = (listviewHeight / 2) - (transcriptRowHeight / 2);
                    }
                };
                transcriptListView.getViewTreeObserver().addOnGlobalLayoutListener(transcriptListLayoutListener);
            }
        }
    }

    @Override
    public void updateTranscript(@NonNull TimedTextObject subtitles) {
        if (transcriptAdapter != null) {
            transcriptAdapter.clear();
            List<Caption> transcript = new ArrayList<>();
            for (Map.Entry<Integer, Caption> entry : subtitles.captions.entrySet()) {
                transcript.add(entry.getValue());
            }
            transcriptAdapter.addAll(transcript);
            transcriptAdapter.notifyDataSetChanged();
            updateUI(getResources().getConfiguration().orientation);
        }
    }

    @Override
    public void updateSelection(final int subtitleIndex) {
        if (transcriptAdapter != null && !isTranscriptScrolling
                && !transcriptAdapter.isSelected(subtitleIndex)) {
            transcriptAdapter.unselectAll();
            transcriptAdapter.select(subtitleIndex);
            transcriptAdapter.notifyDataSetChanged();
            transcriptListView.smoothScrollToPositionFromTop(subtitleIndex, (int) topOffset);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initTranscriptListView() {
        transcriptAdapter = new TranscriptAdapter(getContext(), environment);
        transcriptListView.setAdapter(transcriptAdapter);

        transcriptListView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE: {
                    isTranscriptScrolling = true;
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    transcriptListView.removeCallbacks(UNFREEZE_AUTO_SCROLL);
                    transcriptListView.postDelayed(UNFREEZE_AUTO_SCROLL, UNFREEZE_AUTOSCROLL_DELAY_MS);
                    break;
                }
            }
            return false;
        });

        transcriptListView.setOnItemClickListener((parent, view, position, id) -> {
            final Caption currentCaption = transcriptAdapter.getItem(position);
            if (currentCaption != null) {
                transcriptListView.removeCallbacks(UNFREEZE_AUTO_SCROLL);
                isTranscriptScrolling = false;

                transcriptAdapter.unselectAll();
                transcriptAdapter.select(position);
                transcriptAdapter.notifyDataSetChanged();
                seekToCaption(currentCaption);
            }
        });
    }

    /**
     * Re-enables our auto scrolling logic of transcript listview with respect to video's current
     * playback position.
     */
    final Runnable UNFREEZE_AUTO_SCROLL = () -> isTranscriptScrolling = false;

    public void updateBottomSectionVisibility(int visibility) {
        if (transcriptListView != null && transcriptAdapter != null) {
            if (visibility == View.VISIBLE) {
                transcriptListView.setVisibility(visibility);
                messageContainer.setVisibility(View.GONE);
            } else if (getActivity() != null) {
                updateUI(getActivity().getRequestedOrientation());
            }
        }
    }

    public boolean showCastMiniController() {
        return false;
    }
}
