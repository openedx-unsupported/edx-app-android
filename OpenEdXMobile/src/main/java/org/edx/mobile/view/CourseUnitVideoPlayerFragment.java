package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.player.PlayerFragment;
import org.edx.mobile.util.MediaConsentUtils;

import subtitleFile.Caption;
import subtitleFile.TimedTextObject;

/**
 * This class is responsible to display the video content through the native android player, also
 * maintain the view of the player controllers.
 */
public class CourseUnitVideoPlayerFragment extends BaseCourseUnitVideoFragment {

    protected final static String HAS_NEXT_UNIT_ID = "has_next_unit";
    protected final static String HAS_PREV_UNIT_ID = "has_prev_unit";

    private final Handler playHandler = new Handler();
    protected boolean hasNextUnit;
    protected boolean hasPreviousUnit;
    private PlayerFragment playerFragment;
    private String chapterName;
    private LectureModel lecture;
    private EnrolledCoursesResponse enrollment;
    private Runnable playPending;

    /**
     * Create a new instance of fragment
     */
    public static CourseUnitVideoPlayerFragment newInstance(VideoBlockModel unit, boolean hasNextUnit, boolean hasPreviousUnit) {
        final CourseUnitVideoPlayerFragment fragment = new CourseUnitVideoPlayerFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putBoolean(HAS_NEXT_UNIT_ID, hasNextUnit);
        args.putBoolean(HAS_PREV_UNIT_ID, hasPreviousUnit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasNextUnit = getArguments().getBoolean(HAS_NEXT_UNIT_ID);
        hasPreviousUnit = getArguments().getBoolean(HAS_PREV_UNIT_ID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Intent extraIntent = getActivity().getIntent();
        if (extraIntent != null) {
            // read incoming chapter name
            if (chapterName == null) {
                chapterName = extraIntent.getStringExtra("chapter");
            }

            // read incoming lecture model
            if (lecture == null) {
                lecture = (LectureModel) extraIntent
                        .getSerializableExtra("lecture");
            }
            // read incoming enrollment model
            if (enrollment == null) {
                enrollment = (EnrolledCoursesResponse) extraIntent
                        .getSerializableExtra(Router.EXTRA_COURSE_DATA);
            }
        }

        if (chapterName == null) {
            if (enrollment != null && lecture != null) {
                if (lecture.chapter != null) {
                    chapterName = lecture.chapter.chapter;
                }
            }
        }

        if (playerFragment == null) {

            playerFragment = new PlayerFragment();
            playerFragment.setCallback(this);
            if (environment.getConfig().isVideoTranscriptEnabled()) {
                playerFragment.setTranscriptCallback(this);
            }

            final BaseCourseUnitVideoFragment.HasComponent hasComponent = (BaseCourseUnitVideoFragment.HasComponent) getActivity();
            if (hasComponent != null) {
                View.OnClickListener next = null;
                View.OnClickListener prev = null;

                if (hasNextUnit) {
                    next = v -> hasComponent.navigateNextComponent();
                }

                if (hasPreviousUnit) {
                    prev = v -> hasComponent.navigatePreviousComponent();
                }

                playerFragment.setNextPreviousListeners(next, prev);
            }

            try {
                final FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.player_container, playerFragment, "player");
                fragmentTransaction.commit();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        if (getUserVisibleHint()) {
            checkVideoStatusAndPlay(unit);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (playerFragment == null) {
            return;
        }

        if (isVisibleToUser) {
            if (playerFragment.getPlayingVideo() == null) {
                checkVideoStatusAndPlay(unit);
            } else {
                checkVideoStatus(unit);
            }
        } else {
            ((BaseFragmentActivity) getActivity()).hideInfoMessage();
        }

        playerFragment.setUserVisibleHint(isVisibleToUser);
    }

    private boolean checkDownloadEntry(DownloadEntry entry) {
        if (entry == null || !entry.isDownload()) {
            return false;
        }

        if (entry.isVideoForWebOnly) {
            Toast.makeText(getContext(), getString(R.string.video_only_on_web_short),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkVideoStatus(VideoBlockModel unit) {
        final DownloadEntry entry = unit.getDownloadEntry(environment.getStorage());
        if (checkDownloadEntry(entry) && !entry.isDownloaded()) {
            if (!MediaConsentUtils.canStreamMedia(getContext())) {
                ((BaseFragmentActivity) getActivity()).
                        showInfoMessage(getString(R.string.wifi_off_message));
            }
        }
    }

    private void checkVideoStatusAndPlay(VideoBlockModel unit) {
        final DownloadEntry entry = unit.getDownloadEntry(environment.getStorage());
        if (!checkDownloadEntry(entry)) return;
        startOnlinePlay(entry);
    }

    private void startOnlinePlay(DownloadEntry model) {
        model.videoThumbnail = unit.getVideoThumbnail(environment.getConfig().getApiHostURL());
        if (!isPlayerVisible()) {
            // don't try to showPlayer() if already shown here
            // this will cause player to freeze
            showPlayer();
        }

        addVideoDatatoDb(model);

        playVideoModel(model);
    }

    public synchronized void playVideoModel(final DownloadEntry video) {
        try {
            if (playerFragment.isPlaying()) {
                if (video.getVideoId().equals(playerFragment.getPlayingVideo().getVideoId())) {
                    logger.debug("this video is already being played, skipping play event");
                    return;
                }
            }
        } catch (Exception ex) {
            logger.debug(ex.toString());
        }
        try {

            // reload this model
            environment.getStorage().reloadDownloadEntry(video);

            logger.debug("Resumed= " + playerFragment.isResumed());
            if (!playerFragment.isResumed()) {
                // playback can work only if fragment is resume
                if (playPending != null) {
                    playHandler.removeCallbacks(playPending);
                }
                playPending = () -> playVideoModel(video);
                playHandler.postDelayed(playPending, 200);
                return;
            } else {
                if (playPending != null) {
                    playHandler.removeCallbacks(playPending);
                }
            }

            final TranscriptModel transcript = getTranscriptModel();

            playerFragment.prepare(video, transcript);


            try {
                // capture chapter name
                if (chapterName == null) {
                    // capture the chapter name of this video
                    chapterName = video.chapter;
                }

                videoModel = video;
            } catch (Exception e) {
                logger.error(e);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void showPlayer() {
        try {
            if (getView() != null) {
                View container = getView().findViewById(R.id.player_container);
                if (container != null) {
                    container.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.warn("Error in showing player");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (playerFragment != null) {
                playerFragment.onStop();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private boolean isPlayerVisible() {
        return getActivity() != null;
    }

    @Override
    protected boolean canProcessSubtitles() {
        if (playerFragment != null) {
            return playerFragment.canProcessSubtitles();
        }
        return false;
    }

    @Override
    protected long getPlayerCurrentPosition() {
        if (playerFragment != null) {
            return playerFragment.getCurrentPosition();
        }
        return 0;
    }

    @Override
    protected void updateClosedCaptionData(Caption caption) {
        if (playerFragment != null) {
            playerFragment.updateClosedCaptionData(caption);
        }
    }

    @Override
    protected void showClosedCaptionData(TimedTextObject subtitles) {
        if (playerFragment != null) {
            playerFragment.showClosedCaptionData(subtitles);
        }
    }

    @Override
    public void seekToCaption(Caption caption) {
        playerFragment.seekToCaption(caption);
    }

    @Override
    public boolean hasCastSupportedVideoContent() {
        return true;
    }

    @Override
    public boolean showCastMiniController() {
        return true;
    }
}
