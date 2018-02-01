package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
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
import org.edx.mobile.module.storage.BulkVideosDownloadCancelledEvent;
import org.edx.mobile.module.storage.BulkVideosDownloadStartedEvent;
import org.edx.mobile.util.DownloadUtil;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.adapters.NewCourseOutlineAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.util.DownloadUtil.isDownloadSizeWithinLimit;

public class BulkDownloadFragment extends BaseFragment {
    protected final Logger logger = new Logger(getClass().getName());

    public enum SwitchState {
        /**
         * Switch's state was set to ON by user.
         */
        USER_TURNED_ON,
        /**
         * Switch's state was set to OFF by user.
         */
        USER_TURNED_OFF,
        /**
         * Switch's state was never set by user so its currently in its default state.
         */
        DEFAULT,
        /**
         * Switch's state was set to ON by user and the task that initiated due to it hasn't finished yet.
         */
        IN_PROCESS
    }

    private static final int DELETE_DELAY_MS = 4000;
    private static final int DOWNLOAD_PROGRESS_DELAY_MS = 2000;

    private RowBulkDownloadBinding binding;
    private NewCourseOutlineAdapter.DownloadListener downloadListener;
    private IEdxEnvironment environment;
    private VideoPrefs prefManager;
    private SwitchState switchState = SwitchState.DEFAULT;
    private CourseComponent courseComponent;
    private boolean isDeleteScheduled = false;
    private Handler bgThreadHandler;

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

        final HandlerThread handlerThread = new HandlerThread("BulkDownloadBgThread");
        handlerThread.start();
        bgThreadHandler = new Handler(handlerThread.getLooper());

        ViewCompat.setImportantForAccessibility(binding.getRoot(), ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.ivIcon, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.tvTitle, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.tvSubtitle, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.pbDownload, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(binding.swDownload, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
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
        environment.getDatabase().getAllVideosByCourse(courseComponent.getCourseId(),
                new DataCallback<List<VideoModel>>() {
                    @Override
                    public void onResult(final List<VideoModel> result) {
                        for (CourseComponent video : allVideosInCourse) {
                            boolean foundInDb = false;
                            for (VideoModel dbVideo : result) {
                                if (video.getId().contentEquals(dbVideo.getVideoId())) {
                                    final DownloadEntry.DownloadedState downloadedState =
                                            DownloadEntry.DownloadedState.values()[dbVideo.getDownloadedStateOrdinal()];
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

                        binding.getRoot().post(new Runnable() {
                            @Override
                            public void run() {
                                binding.getRoot().setVisibility(View.VISIBLE);
                                // Lets populate the view now that we have all the info
                                updateUI();
                            }
                        });
                    }

                    @Override
                    public void onFail(Exception ex) {
                        binding.getRoot().post(new Runnable() {
                            @Override
                            public void run() {
                                binding.getRoot().setVisibility(View.GONE);
                            }
                        });
                    }
                });
    }

    private void updateUI() {
        switchState = prefManager.getBulkDownloadSwitchState(videosStatus.courseId);

        if (videosStatus.allVideosDownloaded()) {
            bgThreadHandler.removeCallbacks(PROGRESS_RUNNABLE);
            binding.pbDownload.setVisibility(View.GONE);

            setViewState(FontAwesomeIcons.fa_film, Animation.NONE, R.string.download_complete,
                    binding.tvSubtitle.getResources().getQuantityString(R.plurals.download_total, videosStatus.total),
                    "total_videos_count", videosStatus.total + "", "total_videos_size",
                    MemoryUtil.format(binding.tvSubtitle.getContext(), videosStatus.totalVideosSize));

            binding.getRoot().setOnClickListener(null);
            ViewCompat.setImportantForAccessibility(binding.getRoot(), ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
            setSwitchAccessibility(R.string.switch_on_all_downloaded);
        } else if (videosStatus.allVideosDownloading()) {
            // This means that all the videos have been put to downloading without the use of Bulk Download switch
            if (switchState == SwitchState.USER_TURNED_OFF && !isDeleteScheduled) {
                switchState = SwitchState.USER_TURNED_ON;
                prefManager.setBulkDownloadSwitchState(switchState, videosStatus.courseId);
            }

            initDownloadProgressView();

            setViewState(FontAwesomeIcons.fa_spinner, Animation.PULSE, R.string.downloading_videos,
                    binding.tvSubtitle.getResources().getString(R.string.download_remaining),
                    "remaining_videos_count", videosStatus.remaining + "", "remaining_videos_size",
                    MemoryUtil.format(getContext(), videosStatus.remainingVideosSize));

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadListener.viewDownloadsStatus();
                }
            });
            ViewCompat.setImportantForAccessibility(binding.getRoot(), ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
            setSwitchAccessibility(R.string.switch_on_all_downloading);
        } else if (switchState == SwitchState.IN_PROCESS) {
            setViewState(FontAwesomeIcons.fa_spinner, Animation.PULSE, R.string.download_starting,
                    binding.tvSubtitle.getResources().getString(R.string.download_remaining),
                    "remaining_videos_count", videosStatus.remaining + "", "remaining_videos_size",
                    MemoryUtil.format(getContext(), videosStatus.remainingVideosSize));

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadListener.viewDownloadsStatus();
                }
            });
            ViewCompat.setImportantForAccessibility(binding.getRoot(), ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
            setSwitchAccessibility(R.string.switch_on_all_downloading);
        } else {
            bgThreadHandler.removeCallbacks(PROGRESS_RUNNABLE);
            binding.pbDownload.setVisibility(View.GONE);

            setViewState(FontAwesomeIcons.fa_film, Animation.NONE,
                    R.string.download_to_device,
                    binding.tvSubtitle.getResources().getQuantityString(R.plurals.download_total, videosStatus.remaining),
                    "total_videos_count", videosStatus.remaining + "", "total_videos_size",
                    MemoryUtil.format(getContext(), videosStatus.remainingVideosSize));

            binding.getRoot().setOnClickListener(null);
            ViewCompat.setImportantForAccessibility(binding.getRoot(), ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
            setSwitchAccessibility(R.string.switch_off_no_downloading);
        }

        setSwitchState();
    }

    private void setViewState(@NonNull Icon icon, @NonNull Animation animation,
                              @StringRes int titleRes, @NonNull String descPattern,
                              @NonNull String firstPlaceholder, @NonNull String firstPlaceholderVal,
                              @NonNull String secondPlaceholder, @NonNull String secondPlaceholderVal) {
        binding.ivIcon.setIcon(icon);
        binding.ivIcon.setIconAnimation(animation);
        binding.tvTitle.setText(titleRes);
        setSubtitle(descPattern, firstPlaceholder, firstPlaceholderVal, secondPlaceholder, secondPlaceholderVal);
    }

    private void setSubtitle(@NonNull String descPattern, @NonNull String firstPlaceholder,
                             @NonNull String firstPlaceholderVal, @NonNull String secondPlaceholder,
                             @NonNull String secondPlaceholderVal) {
        final Map<String, String> keyValMap = new HashMap<>();
        keyValMap.put(firstPlaceholder, firstPlaceholderVal);
        keyValMap.put(secondPlaceholder, secondPlaceholderVal);
        binding.tvSubtitle.setText(ResourceUtil.getFormattedString(descPattern, keyValMap));
    }

    private void setSwitchState() {
        switch (switchState) {
            case USER_TURNED_ON:
                binding.swDownload.setChecked(true);
                binding.swDownload.setEnabled(true);
                break;
            case IN_PROCESS:
                binding.swDownload.setChecked(true);
                binding.swDownload.setEnabled(false);
                if (videosStatus.allVideosDownloading()) {
                    // Now that all videos have been enqueued to Download manager, enable the Switch and update its state
                    switchState = SwitchState.USER_TURNED_ON;
                    prefManager.setBulkDownloadSwitchState(switchState, videosStatus.courseId);
                    setSwitchState();
                }
                break;
            case USER_TURNED_OFF:
                binding.swDownload.setChecked(false);
                binding.swDownload.setEnabled(true);
                break;
            case DEFAULT:
                binding.swDownload.setChecked(videosStatus.allVideosDownloaded() || videosStatus.allVideosDownloading());
                binding.swDownload.setEnabled(true);
                break;
        }
    }

    private void setSwitchAccessibility(@StringRes int accessibilitySuffixStringRes) {
        @StringRes
        final int prefixStringRes = R.string.bulk_download_switch;
        final Resources resources = binding.swDownload.getResources();
        binding.swDownload.setContentDescription(
                resources.getString(prefixStringRes) + " " +
                        binding.tvTitle.getText() + ". " +
                        binding.tvSubtitle.getText() + ". " +
                        resources.getString(accessibilitySuffixStringRes));
    }

    private void initDownloadSwitch() {
        binding.swDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CompoundButton buttonView = (CompoundButton) v;
                if (buttonView.isChecked()) {
                    // Stop videos from deletion (if the Runnable has been postDelayed)
                    bgThreadHandler.removeCallbacks(DELETION_RUNNABLE);
                    isDeleteScheduled = false;

                    switchState = SwitchState.IN_PROCESS;
                    prefManager.setBulkDownloadSwitchState(switchState, videosStatus.courseId);
                    setSwitchState();

                    // Download all videos
                    downloadListener.download(remainingVideos);
                    // Video download process on bulk download view needs to be delayed when the
                    // confirmation dialog for videos' size above 1 GB is shown.
                    if (isDownloadSizeWithinLimit(videosStatus.remainingVideosSize, MemoryUtil.GB)) {
                        onEvent((BulkVideosDownloadStartedEvent) null);
                    }

                    environment.getAnalyticsRegistry().trackBulkDownloadSwitchOn(
                            videosStatus.courseId, videosStatus.total, videosStatus.remaining);
                } else {
                    switchState = SwitchState.USER_TURNED_OFF;
                    prefManager.setBulkDownloadSwitchState(switchState, videosStatus.courseId);
                    // Delete all videos after a delay
                    startVideosDeletion();
                    bgThreadHandler.removeCallbacks(PROGRESS_RUNNABLE);
                    updateUI();

                    environment.getAnalyticsRegistry().trackBulkDownloadSwitchOff(
                            videosStatus.courseId, videosStatus.total);
                }
            }
        });
    }

    private void startVideosDeletion() {
        isDeleteScheduled = true;
        // Safety check to avoid multiple timers running at the same time
        bgThreadHandler.removeCallbacks(DELETION_RUNNABLE);
        bgThreadHandler.postDelayed(DELETION_RUNNABLE, DELETE_DELAY_MS);
    }

    final Runnable DELETION_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            final int deleted = environment.getStorage().removeDownloads(removableVideos);
            isDeleteScheduled = false;
            logger.debug("TOTAL_VIDEOS: " + removableVideos.size() + " - DELETE_VIDEOS: " + deleted);
        }
    };

    private void initDownloadProgressView() {
        // Safety check to avoid multiple timers running at the same time
        bgThreadHandler.removeCallbacks(PROGRESS_RUNNABLE);
        bgThreadHandler.post(PROGRESS_RUNNABLE);
    }

    final Runnable PROGRESS_RUNNABLE = new Runnable() {

        public void run() {
            final Context context = getContext();
            if (!isValidState() || !NetworkUtil.isConnected(context)) {
                return;
            }

            if (!videosStatus.allVideosDownloading()) {
                binding.pbDownload.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.pbDownload.setVisibility(View.GONE);
                        updateUI();
                    }
                });
            } else {
                environment.getStorage().getDownloadProgressOfCourseVideos(videosStatus.courseId,
                        new DataCallback<NativeDownloadModel>() {
                            @Override
                            public void onResult(final NativeDownloadModel downloadModel) {
                                if (downloadModel != null && isValidState()) {
                                    /*
                                    Since this is an async DB call, we can't be sure
                                    when its response would come. So, before updating
                                    the UI at runtime we are first checking the pre-
                                    condition i.e. all the videos should be downloading.
                                     */
                                    final long remainingSizeToDownload = DownloadUtil.getRemainingSizeToDownload(
                                            videosStatus.remainingVideosSize, downloadModel.downloaded);

                                    final int percentageDownloaded = DownloadUtil.getPercentDownloaded(
                                            videosStatus.totalVideosSize, videosStatus.totalVideosSize - remainingSizeToDownload);

                                    if (videosStatus.allVideosDownloading()) {
                                        binding.pbDownload.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                binding.pbDownload.setVisibility(View.VISIBLE);
                                                binding.pbDownload.setProgress(percentageDownloaded);
                                                setSubtitle(binding.tvSubtitle.getResources().getString(R.string.download_remaining),
                                                        "remaining_videos_count", videosStatus.remaining + "", "remaining_videos_size",
                                                        MemoryUtil.format(context, remainingSizeToDownload));
                                                setSwitchAccessibility(R.string.switch_on_all_downloading);
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onFail(Exception ex) {
                                logger.error(ex);
                            }
                        });

                bgThreadHandler.postDelayed(this, DOWNLOAD_PROGRESS_DELAY_MS);
            }
        }
    };

    /**
     * @return
     */
    private boolean isValidState() {
        return getActivity() != null && environment != null && downloadListener != null;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        bgThreadHandler.removeCallbacks(PROGRESS_RUNNABLE);
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(BulkVideosDownloadCancelledEvent event) {
        binding.swDownload.setChecked(false);
        switchState = SwitchState.DEFAULT;
        prefManager.setBulkDownloadSwitchState(switchState, videosStatus.courseId);
        updateUI();
    }

    public void onEvent(BulkVideosDownloadStartedEvent event) {
        remainingVideos.clear();
        updateUI();
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
            return (total == downloaded + downloading) || (switchState != null && switchState == SwitchState.USER_TURNED_ON);
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
