package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.internal.Animation;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.RowBulkDownloadBinding;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.prefs.VideoPrefs;
import org.edx.mobile.module.storage.DownloadInterruptedEvent;
import org.edx.mobile.module.storage.DownloadStartedEvent;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.adapters.NewCourseOutlineAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class BulkDownloadFragment extends BaseFragment {
    protected final Logger logger = new Logger(getClass().getName());

    public enum SwitchState {ON, OFF, DEFAULT}

    private static final int DELETE_DELAY_MS = 4000;
    private static final int DOWNLOAD_PROGRESS_DELAY_MS = 2000;

    private RowBulkDownloadBinding binding;
    private NewCourseOutlineAdapter.DownloadListener downloadListener;
    private IEdxEnvironment environment;
    private VideoPrefs prefManager;
    private SwitchState switchState = SwitchState.DEFAULT;
    private CourseComponent courseComponent;
    private Timer downloadProgressTimer;
    private Timer deleteVideosTimer;

    /**
     * Summarises the download status of all the videos within a course.
     */
    private CourseVideosStatus videosStatus = new CourseVideosStatus();
    /**
     * List of videos that can be downloaded.
     */
    private List<HasDownloadEntry> remainingVideos = new ArrayList<>();
    /**
     * List of videos that are currently being downloaded or haven been downloaded.
     */
    private List<VideoModel> removableVideos = new ArrayList<>();

    public BulkDownloadFragment() {
    }

    @SuppressLint("ValidFragment")
    public BulkDownloadFragment(@NonNull NewCourseOutlineAdapter.DownloadListener downloadListener,
                                @NonNull IEdxEnvironment environment) {
        this.downloadListener = downloadListener;
        this.environment = environment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.row_bulk_download, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefManager = new VideoPrefs(getContext());

        ViewCompat.setImportantForAccessibility(binding.getRoot(), ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.icon, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.title, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.description, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.downloadProgress, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.downloadButton, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        initDownloadSwitch();
        if (courseComponent != null) {
            populateViewHolder(courseComponent);
        }
    }

    public void populateViewHolder(@NonNull CourseComponent courseComponent) {
        this.courseComponent = courseComponent;
        if (binding == null) return;
        remainingVideos.clear();
        removableVideos.clear();
        videosStatus.reset();
        videosStatus.courseId = courseComponent.getCourseId();

        // Get all the videos of this course
        final List<CourseComponent> allVideosInCourse = courseComponent.getVideos(true);
        videosStatus.total = allVideosInCourse.size();

        // Get all the videos of this course from DB that we have downloaded or are downloading
        final List<VideoModel> videosInDB = environment.getDatabase().getAllVideosByCourse(courseComponent.getCourseId(), null);
        for (CourseComponent video : allVideosInCourse) {
            boolean foundInDb = false;
            for (VideoModel dbVideo : videosInDB) {
                if (video.getId().contentEquals(dbVideo.getVideoId())) {
                    final DownloadEntry.DownloadedState downloadedState = DownloadEntry.DownloadedState.values()[dbVideo.getDownloadedStateOrdinal()];
                    switch (downloadedState) {
                        case DOWNLOADED:
                            videosStatus.downloaded++;
                            videosStatus.totalVideosSize += dbVideo.getSize();
                            removableVideos.add(dbVideo);
                            break;
                        case DOWNLOADING:
                            videosStatus.downloading++;
                            videosStatus.remaining++;
                            videosStatus.remainingVideosSize += dbVideo.getSize();
                            videosStatus.totalVideosSize += dbVideo.getSize();
                            removableVideos.add(dbVideo);
                            break;
                        default:
                            videosStatus.remaining++;
                            videosStatus.remainingVideosSize += dbVideo.getSize();
                            videosStatus.totalVideosSize += dbVideo.getSize();
                            remainingVideos.add((VideoBlockModel) video);
                            break;
                    }
                    foundInDb = true;
                    break;
                }
            }
            if (!foundInDb) {
                videosStatus.remaining++;
                final VideoBlockModel videoBlockModel = (VideoBlockModel) video;
                final long videoSize = videoBlockModel.getPreferredVideoEncodingSize();
                if (videoSize != -1) {
                    videosStatus.remainingVideosSize += videoSize;
                    videosStatus.totalVideosSize += videoSize;
                }
                remainingVideos.add((VideoBlockModel) video);
            }
        }

        // Lets populate the view now that we have all the info
        updateUI();
    }

    private void updateUI() {
        switchState = prefManager.getBulkDownloadSwitchState(videosStatus.courseId);

        if (videosStatus.allVideosDownloaded()) {
            binding.downloadProgress.setVisibility(View.GONE);

            setViewState(FontAwesomeIcons.fa_film, Animation.NONE, R.string.download_complete,
                    binding.description.getResources().getQuantityString(R.plurals.download_total, videosStatus.total),
                    "total_videos_count", videosStatus.total + "", "total_videos_size",
                    MemoryUtil.format(binding.description.getContext(), videosStatus.totalVideosSize));

            binding.getRoot().setOnClickListener(null);
            setSwitchAccessibility(R.string.switch_on_all_downloaded);
        } else if (videosStatus.allVideosDownloading() || switchState == SwitchState.ON) {
            setViewState(FontAwesomeIcons.fa_spinner, Animation.PULSE, R.string.downloading_videos,
                    binding.description.getResources().getString(R.string.download_remaining),
                    "remaining_videos_count", videosStatus.remaining + "", "remaining_videos_size",
                    MemoryUtil.format(getContext(), videosStatus.remainingVideosSize));

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadListener.viewDownloadsStatus();
                }
            });
            setSwitchAccessibility(R.string.switch_on_all_downloading);
        } else {
            binding.downloadProgress.setVisibility(View.GONE);

            setViewState(FontAwesomeIcons.fa_film, Animation.NONE,
                    R.string.download_to_device,
                    binding.description.getResources().getQuantityString(R.plurals.download_total, videosStatus.remaining),
                    "total_videos_count", videosStatus.remaining + "", "total_videos_size",
                    MemoryUtil.format(getContext(), videosStatus.remainingVideosSize));

            binding.getRoot().setOnClickListener(null);
            setSwitchAccessibility(R.string.switch_off_no_downloading);
        }

        switch (switchState) {
            case ON:
                binding.downloadButton.setChecked(true);
                break;
            case OFF:
                binding.downloadButton.setChecked(false);
                break;
            case DEFAULT:
                binding.downloadButton.setChecked(videosStatus.allVideosDownloaded() || videosStatus.allVideosDownloading());
        }
    }

    private void setViewState(@NonNull Icon icon, @NonNull Animation animation,
                              @StringRes int titleRes, @NonNull String descPattern,
                              @NonNull String firstPlaceholder, @NonNull String firstPlaceholderVal,
                              @NonNull String secondPlaceholder, @NonNull String secondPlaceholderVal) {
        binding.icon.setIcon(icon);
        binding.icon.setIconAnimation(animation);
        binding.title.setText(titleRes);
        setSubtitle(descPattern, firstPlaceholder, firstPlaceholderVal, secondPlaceholder, secondPlaceholderVal);
    }

    private void setSubtitle(@NonNull String descPattern, @NonNull String firstPlaceholder,
                             @NonNull String firstPlaceholderVal, @NonNull String secondPlaceholder,
                             @NonNull String secondPlaceholderVal) {
        final Map<String, String> keyValMap = new HashMap<>();
        keyValMap.put(firstPlaceholder, firstPlaceholderVal);
        keyValMap.put(secondPlaceholder, secondPlaceholderVal);
        binding.description.setText(ResourceUtil.getFormattedString(descPattern, keyValMap));
    }

    private void setSwitchAccessibility(@StringRes int accessibilitySuffixStringRes) {
        binding.downloadButton.setContentDescription(binding.title.getText() + ". " +
                binding.description.getText() + ". " +
                getResources().getString(accessibilitySuffixStringRes));
    }

    private void initDownloadSwitch() {
        binding.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CompoundButton buttonView = (CompoundButton) v;
                if (buttonView.isChecked()) {
                    switchState = SwitchState.ON;
                    // Download all videos
                    downloadListener.download(remainingVideos);
                    // Video download process on bulk download view needs to be delayed when the
                    // confirmation dialog for videos' size above 1 GB is shown.
                    if (videosStatus.remainingVideosSize < MemoryUtil.GB) {
                        onEvent((DownloadStartedEvent) null);
                    }
                } else {
                    switchState = SwitchState.OFF;
                    // Delete all videos after a delay
                    startVideosDeletion();
                    downloadProgressTimer.cancel();
                }

                prefManager.setBulkDownloadSwitchState(switchState, videosStatus.courseId);
                updateUI();

                // TODO: Remove this log, its just for performance testing purpose
                logger.debug("PERFORMANCE: VIDEOS_STATUS" + videosStatus.toString());
            }
        });
    }

    private void startVideosDeletion() {
        // Safety check to avoid multiple timers running at the same time
        if (deleteVideosTimer != null) {
            deleteVideosTimer.cancel();
        }
        deleteVideosTimer = new Timer();
        deleteVideosTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int deleted = environment.getStorage().removeDownloads(removableVideos);
                // TODO: Remove this log, its just for performance testing purpose
                logger.debug("PERFORMANCE: TOTAL_VIDEOS: " + removableVideos.size() + " - DELETE_VIDEOS: " + deleted);
                environment.getAnalyticsRegistry().trackBulkDownloadSwitchOff(
                        videosStatus.courseId, videosStatus.total);
            }
        }, DELETE_DELAY_MS);
    }

    private void initDownloadProgressView() {
        // Safety check to avoid multiple timers running at the same time
        if (downloadProgressTimer != null) {
            downloadProgressTimer.cancel();
        }

        downloadProgressTimer = new Timer();
        downloadProgressTimer.schedule(new TimerTask() {
            public void run() {
                final Context context = getContext();
                if (context == null || environment == null || downloadListener == null) {
                    downloadProgressTimer.cancel();
                    return;
                }

                // TODO: Remove this log, its just for performance testing purpose
                logger.debug("PERFORMANCE: initDownloadProgressView - isUIThread: " + (Looper.myLooper() == Looper.getMainLooper()));
                if (!NetworkUtil.isConnected(context) ||
                        !environment.getDatabase().isAnyVideoDownloading(null)) {
                    downloadProgressTimer.cancel();
                    binding.downloadProgress.post(new Runnable() {
                        @Override
                        public void run() {
                            binding.downloadProgress.setVisibility(View.GONE);
                            updateUI();
                        }
                    });
                } else {
                    environment.getStorage().getDownloadProgressOfCourseVideos(videosStatus.courseId,
                            new DataCallback<NativeDownloadModel>() {
                                @Override
                                public void onResult(final NativeDownloadModel downloadModel) {
                                    if (downloadModel != null && getActivity() != null) {
                                        binding.downloadProgress.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (getActivity() == null || environment == null || downloadListener == null) {
                                                    return;
                                                }
                                                binding.downloadProgress.setVisibility(View.VISIBLE);
                                                binding.downloadProgress.setProgress(downloadModel.getPercentDownloaded());
                                                setSubtitle(binding.description.getResources().getString(R.string.download_remaining),
                                                        "remaining_videos_count", videosStatus.remaining + "", "remaining_videos_size",
                                                        MemoryUtil.format(context,
                                                                NativeDownloadModel.getRemainingSizeToDownload(
                                                                        videosStatus.remainingVideosSize, downloadModel.downloaded
                                                                )));
                                                setSwitchAccessibility(R.string.switch_on_all_downloading);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFail(Exception ex) {
                                    logger.error(ex);
                                }
                            });
                }
            }
            /*
            Initial delay has been added cuz the Download Manager takes some time to initiate the
            first download. Otherwise our timer task gets cancelled due to this function
            # environment.getDatabase().isAnyVideoDownloading(null) #
            returning false.
             */
        }, DOWNLOAD_PROGRESS_DELAY_MS, DOWNLOAD_PROGRESS_DELAY_MS);
    }

    @Override
    public void onStart() {
        super.onStart();
        initDownloadProgressView();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (downloadProgressTimer != null) {
            downloadProgressTimer.cancel();
        }
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(DownloadInterruptedEvent event) {
        binding.downloadButton.setChecked(false);
        switchState = SwitchState.DEFAULT;
        prefManager.setBulkDownloadSwitchState(switchState, videosStatus.courseId);
        updateUI();
    }

    public void onEvent(DownloadStartedEvent event) {
        // Stop videos from deletion (if the Runnable has been postDelayed)
        if (deleteVideosTimer != null) {
            deleteVideosTimer.cancel();
        }
        remainingVideos.clear();
        prefManager.setBulkDownloadSwitchState(switchState, videosStatus.courseId);
        updateUI();
        initDownloadProgressView();
        environment.getAnalyticsRegistry().trackBulkDownloadSwitchOn(
                videosStatus.courseId, videosStatus.total, videosStatus.remaining);
    }

    private class CourseVideosStatus {
        String courseId;
        int total;
        int downloaded;
        int downloading;
        int remaining;
        long totalVideosSize;
        long remainingVideosSize;

        boolean allVideosDownloaded() {
            return total == downloaded;
        }

        boolean allVideosDownloading() {
            return total == downloaded + downloading;
        }

        void reset() {
            total = downloaded = downloading = remaining = 0;
            totalVideosSize = remainingVideosSize = 0;
            courseId = null;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public String toString() {
            return String.format("Total: %d - Downloaded: %d - Downloading: %d - Remaining: %d - TotalSize: %d - RemainingSize: %d",
                    total, downloaded, downloading, remaining, totalVideosSize, remainingVideosSize);
        }
    }
}
