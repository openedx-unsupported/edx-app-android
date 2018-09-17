package org.edx.mobile.view;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;

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
import org.edx.mobile.player.IPlayerEventCallback;
import org.edx.mobile.player.TranscriptListener;
import org.edx.mobile.view.adapters.TranscriptAdapter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import subtitleFile.Caption;
import subtitleFile.TimedTextObject;

public abstract class CourseUnitVideoFragment extends CourseUnitFragment
        implements IPlayerEventCallback, TranscriptListener {

    protected abstract void seekToCaption(Caption caption);
    protected abstract void updateUIForOrientation();

    protected final static Logger logger = new Logger(CourseUnitVideoFragment.class.getName());
    protected final static String HAS_NEXT_UNIT_ID = "has_next_unit";
    protected final static String HAS_PREV_UNIT_ID = "has_prev_unit";
    private final static int UNFREEZE_AUTOSCROLL_DELAY_MS = 3500;
    protected DownloadEntry videoModel;

    VideoBlockModel unit;
    protected ListView transcriptListView;
    protected TranscriptAdapter transcriptAdapter;

    private View messageContainer;

    protected boolean hasNextUnit;

    protected boolean hasPreviousUnit;

    // Defines if the user is scrolling the transcript listview
    protected boolean isTranscriptScrolling = false;
    // Top offset to centralize the currently active transcript item in the listview
    private float topOffset = 0;

    @Inject
    private CourseAPI courseApi;
    private ViewTreeObserver.OnGlobalLayoutListener transcriptListLayoutListener;

    public static Bundle getCourseUnitBundle(VideoBlockModel unit, boolean hasNextUnit, boolean hasPreviousUnit) {
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putBoolean(HAS_NEXT_UNIT_ID, hasNextUnit);
        args.putBoolean(HAS_PREV_UNIT_ID, hasPreviousUnit);
        return args;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        unit = getArguments() == null ? null :
                (VideoBlockModel) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
        hasNextUnit = getArguments().getBoolean(HAS_NEXT_UNIT_ID);
        hasPreviousUnit = getArguments().getBoolean(HAS_PREV_UNIT_ID);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_video, container, false);
        messageContainer = v.findViewById(R.id.message_container);
        transcriptListView = (ListView) v.findViewById(R.id.transcript_listview);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restore(savedInstanceState);
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
    public void onResume() {
        super.onResume();
        updateUIForOrientation();
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

    protected void initTranscriptListView() {
        transcriptAdapter = new TranscriptAdapter(getContext(), environment);
        transcriptListView.setAdapter(transcriptAdapter);

        transcriptListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
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
    final Runnable UNFREEZE_AUTO_SCROLL = new Runnable() {
        @Override
        public void run() {
            isTranscriptScrolling = false;
        }
    };

    public void updateBottomSectionVisibility(int visibility) {
        if (transcriptListView != null && transcriptAdapter != null) {
            if (visibility == View.VISIBLE) {
                transcriptListView.setVisibility(visibility);
                messageContainer.setVisibility(visibility);
            } else if (getActivity() != null) {
                updateUI(getActivity().getRequestedOrientation());
            }
        }
    }
}
