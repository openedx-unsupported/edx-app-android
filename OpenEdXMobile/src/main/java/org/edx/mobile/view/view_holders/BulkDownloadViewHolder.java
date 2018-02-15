package org.edx.mobile.view.view_holders;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.internal.Animation;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry.DownloadedState;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.adapters.NewCourseOutlineAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkDownloadViewHolder {
    private static final int DELETE_DELAY_MS = 4000;

    public final View rootView;
    public final IconImageView image;
    public final TextView title;
    public final TextView description;
    public final SwitchCompat downloadSwitch;
    public final ProgressBar progressBar;
    private final NewCourseOutlineAdapter.DownloadListener downloadListener;
    private final IEdxEnvironment environment;

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
    /**
     * Callback for monitoring currently downloaded videos. Fetched from calling
     * {@link VideoDownloadHelper#registerForDownloadProgress(String, View, VideoDownloadHelper.DownloadProgressCallback) VideoDownloadHelper#registerForDownloadProgress}.
     */
    private Runnable downloadProgressRunnable;

    public BulkDownloadViewHolder(@NonNull View itemView,
                                  @NonNull NewCourseOutlineAdapter.DownloadListener downloadListener,
                                  @NonNull IEdxEnvironment environment) {
        rootView = itemView;
        image = (IconImageView) itemView.findViewById(R.id.icon);
        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.description);
        downloadSwitch = (SwitchCompat) itemView.findViewById(R.id.download_button);
        progressBar = (ProgressBar) itemView.findViewById(R.id.download_progress);

        ViewCompat.setImportantForAccessibility(rootView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(image, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(title, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(description, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(progressBar, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
        ViewCompat.setImportantForAccessibility(downloadSwitch, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

        this.downloadListener = downloadListener;
        this.environment = environment;
        initDownloadSwitch();
    }

    public void populateViewHolder(@NonNull CourseComponent courseComponent) {
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
                    final DownloadedState downloadedState = DownloadedState.values()[dbVideo.getDownloadedStateOrdinal()];
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
        if (videosStatus.allVideosDownloaded()) {
            progressBar.setVisibility(View.GONE);
            progressBar.removeCallbacks(downloadProgressRunnable);
            downloadProgressRunnable = null;

            setViewState(FontAwesomeIcons.fa_film, Animation.NONE, R.string.download_complete,
                    description.getResources().getQuantityString(R.plurals.download_total, videosStatus.total),
                    "total_videos_count", videosStatus.total + "", "total_videos_size",
                    MemoryUtil.format(description.getContext(), videosStatus.totalVideosSize));

            rootView.setOnClickListener(null);
            setSwitchAccessibility(R.string.switch_on_all_downloaded);
        } else if (videosStatus.allVideosDownloading()) {
            setViewState(FontAwesomeIcons.fa_spinner, Animation.PULSE, R.string.downloading_videos,
                    description.getResources().getString(R.string.download_remaining),
                    "remaining_videos_count", videosStatus.remaining + "", "remaining_videos_size",
                    MemoryUtil.format(description.getContext(), videosStatus.remainingVideosSize));

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadListener.viewDownloadsStatus();
                }
            });
            initDownloadProgressView(courseComponent);
            setSwitchAccessibility(R.string.switch_on_all_downloading);
        } else {
            progressBar.setVisibility(View.GONE);
            progressBar.removeCallbacks(downloadProgressRunnable);
            downloadProgressRunnable = null;

            setViewState(FontAwesomeIcons.fa_film, Animation.NONE,
                    R.string.download_to_device,
                    description.getResources().getQuantityString(R.plurals.download_total, videosStatus.remaining),
                    "total_videos_count", videosStatus.remaining + "", "total_videos_size",
                    MemoryUtil.format(description.getContext(), videosStatus.remainingVideosSize));

            rootView.setOnClickListener(null);
            setSwitchAccessibility(R.string.switch_off_no_downloading);
        }

        downloadSwitch.setChecked(videosStatus.allVideosDownloaded() || videosStatus.allVideosDownloading());
    }

    private void setViewState(@NonNull Icon icon, @NonNull Animation animation,
                              @StringRes int titleRes, @NonNull String descPattern,
                              @NonNull String firstPlaceholder, @NonNull String firstPlaceholderVal,
                              @NonNull String secondPlaceholder, @NonNull String secondPlaceholderVal) {
        image.setIcon(icon);
        image.setIconAnimation(animation);
        title.setText(titleRes);
        setSubtitle(descPattern, firstPlaceholder, firstPlaceholderVal, secondPlaceholder, secondPlaceholderVal);
    }

    private void setSubtitle(@NonNull String descPattern, @NonNull String firstPlaceholder,
                             @NonNull String firstPlaceholderVal, @NonNull String secondPlaceholder,
                             @NonNull String secondPlaceholderVal) {
        final Map<String, String> keyValMap = new HashMap<>();
        keyValMap.put(firstPlaceholder, firstPlaceholderVal);
        keyValMap.put(secondPlaceholder, secondPlaceholderVal);
        description.setText(ResourceUtil.getFormattedString(descPattern, keyValMap));
    }

    private void setSwitchAccessibility(@StringRes int accessibilitySuffixStringRes) {
        downloadSwitch.setContentDescription(title.getText() + ". " +
                description.getText() + ". " +
                downloadSwitch.getResources().getString(accessibilitySuffixStringRes));
    }

    private void initDownloadSwitch() {
        downloadSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CompoundButton buttonView = (CompoundButton) v;
                if (buttonView.isChecked()) {
                    // Stop videos from deletion (if the Runnable has been postDelayed)
                    buttonView.removeCallbacks(DELETE_VIDEOS);
                    // Download all videos
                    downloadListener.download(remainingVideos);
                    // Turn the switch off forcefully if there's no connectivity
                    if (!NetworkUtil.isConnected(buttonView.getContext())) {
                        buttonView.setChecked(false);
                    } else {
                        environment.getAnalyticsRegistry().trackBulkDownloadSwitchOn(
                                videosStatus.courseId, videosStatus.total, videosStatus.remaining);
                    }
                } else {
                    // Delete all videos after a delay
                    buttonView.postDelayed(DELETE_VIDEOS, DELETE_DELAY_MS);
                }
            }

            final Runnable DELETE_VIDEOS = new Runnable() {
                @Override
                public void run() {
                    for (VideoModel videoModel : removableVideos) {
                        environment.getStorage().removeDownload(videoModel);
                    }
                    environment.getAnalyticsRegistry().trackBulkDownloadSwitchOff(
                            videosStatus.courseId, videosStatus.total);
                }
            };
        });
    }

    private void initDownloadProgressView(CourseComponent courseComponent) {
        progressBar.setVisibility(View.VISIBLE);
        downloadProgressRunnable = VideoDownloadHelper.registerForDownloadProgress(courseComponent.getCourseId(),
                progressBar, new VideoDownloadHelper.DownloadProgressCallback() {
                    @Override
                    public void giveProgressStatus(final NativeDownloadModel downloadModel) {
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(downloadModel.getPercentDownloaded());
                                setSubtitle(description.getResources().getString(R.string.download_remaining),
                                        "remaining_videos_count", downloadModel.downloadCount + "", "remaining_videos_size",
                                        MemoryUtil.format(description.getContext(),
                                                NativeDownloadModel.getRemainingSizeToDownload(
                                                        videosStatus.remainingVideosSize, downloadModel.downloaded
                                                )));
                                setSwitchAccessibility(R.string.switch_on_all_downloading);
                            }
                        });
                    }

                    @Override
                    public void startProgress() {
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void stopProgress() {
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                });
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
    }
}
