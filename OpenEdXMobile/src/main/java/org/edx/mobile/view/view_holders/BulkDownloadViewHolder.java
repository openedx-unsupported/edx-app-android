package org.edx.mobile.view.view_holders;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.internal.Animation;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry.DownloadedState;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.adapters.NewCourseOutlineAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkDownloadViewHolder {
    public final IconImageView image;
    public final TextView title;
    public final TextView description;
    public final SwitchCompat downloadSwitch;
    public final ProgressBar progressBar;

    private List<HasDownloadEntry> remainingVideos = new ArrayList<>();

    public BulkDownloadViewHolder(View itemView) {
        image = (IconImageView) itemView.findViewById(R.id.icon);
        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.description);
        downloadSwitch = (SwitchCompat) itemView.findViewById(R.id.download_button);
        progressBar = (ProgressBar) itemView.findViewById(R.id.download_progress);
    }

    public void populateViewHolder(@NonNull CourseComponent courseComponent,
                                   @NonNull IDatabase database,
                                   @NonNull NewCourseOutlineAdapter.DownloadListener downloadListener,
                                   @NonNull Runnable listener) {
        final CourseVideosStatus videosStatus = new CourseVideosStatus();

        // Get all the videos of this course
        final List<CourseComponent> allVideosInCourse = courseComponent.getVideos(true);
        videosStatus.total = allVideosInCourse.size();

        // Get all the videos of this course from DB that we have downloaded or are downloading
        final List<VideoModel> videosInDB = database.getAllVideosByCourse(courseComponent.getCourseId(), null);
        for (CourseComponent video : allVideosInCourse) {
            boolean foundInDb = false;
            for (VideoModel dbVideo : videosInDB) {
                if (video.getId().contentEquals(dbVideo.getVideoId())) {
                    final DownloadedState downloadedState = DownloadedState.values()[dbVideo.getDownloadedStateOrdinal()];
                    switch (downloadedState) {
                        case DOWNLOADED:
                            videosStatus.downloaded++;
                            videosStatus.totalVideosSize += dbVideo.getSize();
                            break;
                        case DOWNLOADING:
                            videosStatus.downloading++;
                            videosStatus.remaining++;
                            videosStatus.remainingVideosSize += dbVideo.getSize();
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
            setViewState(FontAwesomeIcons.fa_film, Animation.NONE, R.string.download_complete,
                    description.getResources().getQuantityString(R.plurals.download_total, videosStatus.total),
                    "total_videos_count", videosStatus.total + "", "total_videos_size",
                    MemoryUtil.format(description.getContext(), videosStatus.totalVideosSize));
        } else if (videosStatus.allVideosDownloading()) {
            setViewState(FontAwesomeIcons.fa_spinner, Animation.PULSE, R.string.downloading_videos,
                    description.getResources().getString(R.string.download_remaining),
                    "remaining_videos_count", videosStatus.remaining + "", "remaining_videos_size",
                    MemoryUtil.format(description.getContext(), videosStatus.remainingVideosSize));
        } else {
            setViewState(FontAwesomeIcons.fa_film, Animation.NONE,
                    R.string.download_to_device,
                    description.getResources().getQuantityString(R.plurals.download_total, videosStatus.total),
                    "total_videos_count", videosStatus.remaining + "", "total_videos_size",
                    MemoryUtil.format(description.getContext(), videosStatus.remainingVideosSize));
        }
        downloadSwitch.setChecked(videosStatus.allVideosDownloaded());
    }

    private void setViewState(@NonNull Icon icon, @NonNull Animation animation,
                              @StringRes int titleRes, @NonNull String descPattern,
                              @NonNull String firstPlaceholder, @NonNull String firstPlaceholderVal,
                              @NonNull String secondPlaceholder, @NonNull String secondPlaceholderVal) {
        image.setIcon(icon);
        image.setIconAnimation(animation);
        title.setText(titleRes);

        final Map<String, String> keyValMap = new HashMap<>();
        keyValMap.put(firstPlaceholder, firstPlaceholderVal);
        keyValMap.put(secondPlaceholder, secondPlaceholderVal);
        description.setText(ResourceUtil.getFormattedString(descPattern, keyValMap));
    }

    private class CourseVideosStatus {
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
    }
}
