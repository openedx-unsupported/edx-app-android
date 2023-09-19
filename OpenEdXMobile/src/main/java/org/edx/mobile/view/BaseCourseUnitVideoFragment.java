package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.databinding.FragmentCourseUnitVideoBinding;
import org.edx.mobile.event.VideoPlaybackEvent;
import org.edx.mobile.http.callback.Callback;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.player.IPlayerEventCallback;
import org.edx.mobile.player.TranscriptListener;
import org.edx.mobile.player.TranscriptManager;
import org.edx.mobile.util.LocaleUtils;
import org.edx.mobile.view.adapters.TranscriptAdapter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import subtitleFile.Caption;
import subtitleFile.TimedTextObject;

/**
 * Base class implementing the basic functionality of the video player screen.
 * It handles UI updating.
 * It downloads, displays and maintains the transcripts based on player position.
 */
public abstract class BaseCourseUnitVideoFragment extends CourseUnitFragment
        implements IPlayerEventCallback, TranscriptListener {

    private FragmentCourseUnitVideoBinding binding;

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

    /**
     * Method to pause the video player from external call.
     *
     * @param pauseVideo status of the player
     */
    protected abstract void playPauseVideoPlayback(boolean pauseVideo);

    protected final static Logger logger = new Logger(BaseCourseUnitVideoFragment.class.getName());
    private final static int UNFREEZE_AUTOSCROLL_DELAY_MS = 3500;
    private static final int SUBTITLES_DISPLAY_DELAY_MS = 100;

    protected DownloadEntry videoModel;

    protected VideoBlockModel unit;
    private TimedTextObject subtitlesObj;

    protected TranscriptAdapter transcriptAdapter;

    // Defines if the user is scrolling the transcript list
    protected boolean isTranscriptScrolling = false;
    // Top offset to centralize the currently active transcript item in the list
    private int topOffset = 0;

    @Inject
    UserPrefs userPrefs;

    @Inject
    CourseAPI courseApi;

    @Inject
    TranscriptManager transcriptManager;

    private ViewTreeObserver.OnGlobalLayoutListener transcriptsLayoutListener;

    private final Handler subtitleDisplayHandler = new Handler();

    /**
     * This runnable handles the displaying of
     * Subtitles on the screen per 100 milli seconds
     */
    private final Runnable subtitlesProcessorRunnable = () -> {
        if (canProcessSubtitles()) {
            long currentPos = getPlayerCurrentPosition();
            if (subtitlesObj != null) {
                Collection<Caption> subtitles = subtitlesObj.captions.values();
                int currentSubtitleIndex = 0;
                for (Caption subtitle : subtitles) {
                    int startMillis = subtitle.start.getMseconds();
                    int endMillis = subtitle.end.getMseconds();
                    if (currentPos >= startMillis && currentPos <= endMillis) {
                        if (userPrefs.getHasSubtitleLanguage()) {
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
        unit = getArguments() == null ? null :
                (VideoBlockModel) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
        EventBus.getDefault().register(this);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCourseUnitVideoBinding.inflate(inflater, container, false);
        if (showCastMiniController()) {
            binding.flMiniController.setVisibility(View.VISIBLE);
        } else {
            binding.flMiniController.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restore(savedInstanceState);
    }

    @Override
    public void downloadTranscript() {
        Activity activity = getActivity();
        if (activity != null) {
            TranscriptModel transcript = getTranscriptModel();
            String transcriptUrl = LocaleUtils.getTranscriptURL(activity, transcript, userPrefs.getSubtitleLanguage());
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
            initTranscriptList();
            updateTranscript(subtitlesObj);
            if (userPrefs.getHasSubtitleLanguage() && !getTranscriptModel().containsKey(userPrefs.getSubtitleLanguage())) {
                String deviceLanguage = LocaleUtils.getCurrentDeviceLanguage(requireActivity());
                if (!TextUtils.isEmpty(deviceLanguage) && getTranscriptModel().containsKey(deviceLanguage)) {
                    userPrefs.setSubtitleLanguage(deviceLanguage);
                } else {
                    userPrefs.setSubtitleLanguage(getTranscriptModel().keySet().toArray()[0].toString());
                }
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
        binding.rvTranscripts.getViewTreeObserver().removeOnGlobalLayoutListener(transcriptsLayoutListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        transcriptManager.cancelTranscriptDownloading();
        updateTranscriptCallbackStatus(false);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (unit == null) {
            updateTranscriptCallbackStatus(false);
        }
        updateUIForOrientation();
    }

    private void updateUIForOrientation() {
        final int orientation = getResources().getConfiguration().orientation;
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.playerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            setFullScreen(true);
        } else {
            float screenWidth = displayMetrics.widthPixels;
            float ideaHeight = screenWidth * 9 / 16;
            binding.playerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int) ideaHeight));
            setFullScreen(false);
        }
        binding.playerContainer.requestLayout();
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
    public void addVideoDataToDb(final DownloadEntry v) {
        try {
            if (v != null) {
                DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE).addVideoData(v, new DataCallback<>() {
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
        if (!unit.isCompleted()) {
            markComponentCompletion(true);
            courseApi.markBlocksCompletion(unit.getCourseId(), List.of(unit.getId())).enqueue(new Callback<>() {
                @Override
                protected void onResponse(@NonNull JSONObject responseBody) {
                    // Nothing to do here
                }
            });
        }
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


    private final DataCallback<Integer> watchedStateCallback = new DataCallback<>() {
        @Override
        public void onResult(Integer result) {
            logger.debug("Watched State Updated");
        }

        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };

    private final DataCallback<Integer> setCurrentPositionCallback = new DataCallback<>() {
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUIForOrientation();
    }

    protected void updateUI(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.messageContainer.setVisibility(View.GONE);
            binding.rvTranscripts.setVisibility(View.GONE);
        } else {
            if (transcriptAdapter == null) {
                binding.messageContainer.setVisibility(View.VISIBLE);
                binding.rvTranscripts.setVisibility(View.GONE);
                initTranscriptList();
            } else {
                binding.messageContainer.setVisibility(View.GONE);
                binding.rvTranscripts.setVisibility(View.VISIBLE);
                // Calculating the offset required for centralizing the current transcript item
                // p.s. Without this listener the getHeight function returns 0
                transcriptsLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        if (getActivity() != null) {
                            binding.rvTranscripts.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            final float transcriptRowHeight = getActivity().getResources().getDimension(R.dimen.transcript_row_height);
                            final float listHeight = binding.rvTranscripts.getHeight();
                            topOffset = (int) ((listHeight / 2) - (transcriptRowHeight / 2));
                        }
                    }
                };
                binding.rvTranscripts.getViewTreeObserver().addOnGlobalLayoutListener(transcriptsLayoutListener);
            }
        }
    }

    @Override
    public void updateTranscript(@NonNull TimedTextObject subtitles) {
        if (transcriptAdapter != null) {
            List<Caption> transcript = new ArrayList<>();
            for (Map.Entry<Integer, Caption> entry : subtitles.captions.entrySet()) {
                transcript.add(entry.getValue());
            }
            transcriptAdapter.submitList(transcript);
            updateUI(getResources().getConfiguration().orientation);
        }
    }

    @Override
    public void updateSelection(final int subtitleIndex) {
        if (transcriptAdapter != null && !isTranscriptScrolling
                && !transcriptAdapter.isSelected(subtitleIndex)) {
            transcriptAdapter.select(subtitleIndex);
            LinearLayoutManager layoutManager = ((LinearLayoutManager) binding.rvTranscripts.getLayoutManager());
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(subtitleIndex, topOffset);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initTranscriptList() {
        transcriptAdapter = new TranscriptAdapter(requireContext(), currentCaption -> {
            if (currentCaption != null) {
                binding.rvTranscripts.removeCallbacks(UNFREEZE_AUTO_SCROLL);
                isTranscriptScrolling = false;
                seekToCaption(currentCaption);
            }
        });
        binding.rvTranscripts.setAdapter(transcriptAdapter);

        binding.rvTranscripts.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                        isTranscriptScrolling = true;
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.rvTranscripts.removeCallbacks(UNFREEZE_AUTO_SCROLL);
                    binding.rvTranscripts.postDelayed(UNFREEZE_AUTO_SCROLL, UNFREEZE_AUTOSCROLL_DELAY_MS);
                }
            }
            return false;
        });
    }

    /**
     * Re-enables our auto scrolling logic of transcript list with respect to video's current
     * playback position.
     */
    final Runnable UNFREEZE_AUTO_SCROLL = () -> isTranscriptScrolling = false;

    public void updateBottomSectionVisibility(int visibility) {
        if (transcriptAdapter != null) {
            if (visibility == View.VISIBLE) {
                binding.rvTranscripts.setVisibility(visibility);
                binding.messageContainer.setVisibility(View.GONE);
            } else if (getActivity() != null) {
                updateUI(getActivity().getRequestedOrientation());
            }
        }
    }

    @Subscribe(sticky = true)
    public void onEvent(VideoPlaybackEvent event) {
        playPauseVideoPlayback(event.getPauseVideo());
        EventBus.getDefault().removeStickyEvent(event);
    }

    public boolean showCastMiniController() {
        return false;
    }
}
