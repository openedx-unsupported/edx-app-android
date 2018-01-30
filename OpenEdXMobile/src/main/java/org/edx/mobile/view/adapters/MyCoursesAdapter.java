package org.edx.mobile.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.util.images.CourseCardUtils;

import java.util.Date;
import java.util.List;


public abstract class MyCoursesAdapter extends BaseListAdapter<EnrolledCoursesResponse> {
    private long lastClickTime;
    private CourseManager courseManager;

    public MyCoursesAdapter(Context context, IEdxEnvironment environment, CourseManager courseManager) {
        super(context, CourseCardViewHolder.LAYOUT, environment);
        this.courseManager = courseManager;
        lastClickTime = 0;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void render(BaseViewHolder tag, final EnrolledCoursesResponse enrollment) {
        final CourseCardViewHolder holder = (CourseCardViewHolder) tag;

        final CourseEntry courseData = enrollment.getCourse();
        holder.setCourseTitle(courseData.getName());
        holder.setCourseImage(courseData.getCourse_image(environment.getConfig().getApiHostURL()));

        updateDownloadStatus(courseData, holder);

        if (enrollment.getCourse().hasUpdates()) {
            holder.setHasUpdates(courseData, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAnnouncementClicked(enrollment);
                }
            });
        } else {
            holder.setDescription(
                    CourseCardUtils.getFormattedDate(getContext(), courseData)
            );
        }
    }

    private void updateDownloadStatus(final CourseEntry courseData, final CourseCardViewHolder holder) {
        final CourseComponent component;
        try {
            component = getCourseComponent(courseData.getId());
            final int totalDownloadableVideos = component == null ? 0 : component.getDownloadableMediaCount();
            // support video download for video type excluding the ones only viewable on web
            if (totalDownloadableVideos == 0) {
                holder.showNoContentDownloadStatusContainer(getContext());
            } else {
                int downloadedCount = environment.getDatabase().getDownloadedMediaCountForCourse(courseData.getId());

                if (downloadedCount == totalDownloadableVideos) {
                    Long downloadTimeStamp = environment.getDatabase().getLastMediaDownloadTimeForCourse(courseData.getId());
                    String relativeTimeSpanString = getRelativeTimeStringFromNow(downloadTimeStamp);
                    setRowStateOnDownload(holder, DownloadEntry.DownloadedState.DOWNLOADED, relativeTimeSpanString, null);
                } else if (environment.getDatabase().isAnyVideoDownloadingInCourse(null, courseData.getId())) {
                    setRowStateOnDownload(holder, DownloadEntry.DownloadedState.DOWNLOADING,
                            null, new View.OnClickListener() {
                                @Override
                                public void onClick(View downloadView) {
                                    viewDownloadsStatus();
                                }
                            });
                } else {
                    setRowStateOnDownload(holder, DownloadEntry.DownloadedState.ONLINE,
                            null, new View.OnClickListener() {
                                @Override
                                public void onClick(View downloadView) {
                                    download(component.getDownloadableMedia());
                                    // Immediately change icon and text to downloading.
                                    setRowStateOnDownload(holder, DownloadEntry.DownloadedState.DOWNLOADING, null, null);
                                }
                            });
                }
            }
        } catch (Exception e) {
            setRowStateOnDownload(holder, null, null, null);
        }
    }

    @NonNull
    private String getRelativeTimeStringFromNow(Long downloadTimeStamp) {
        return DateUtils.getRelativeTimeSpanString(downloadTimeStamp, new Date().getTime(), 0).toString();
    }

    private void setRowStateOnDownload(CourseCardViewHolder row, DownloadEntry.DownloadedState state
            , String relativeTimeStamp, OnClickListener listener) {

        row.showDownloadStatusContainer();
        row.updateDownloadStatus(getContext(), state, listener, relativeTimeStamp);
    }

    protected CourseComponent getCourseComponent(String courseId) throws Exception {
        return courseManager.getCourseByCourseId(courseId);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new CourseCardViewHolder(convertView);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View arg1, int position,
                            long arg3) {
        //This time is checked to avoid taps in quick succession
        final long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            EnrolledCoursesResponse model = (EnrolledCoursesResponse)adapterView.getItemAtPosition(position);
            if (model != null) onItemClicked(model);
        }
    }

    public abstract void onItemClicked(EnrolledCoursesResponse model);

    public abstract void onAnnouncementClicked(EnrolledCoursesResponse model);

    public abstract void download(List<CourseComponent> models);

    public abstract void viewDownloadsStatus();
}