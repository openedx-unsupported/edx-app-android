package org.edx.mobile.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.images.CourseCardUtils;


public abstract class MyCoursesAdapter extends BaseListAdapter<EnrolledCoursesResponse> {
    private long lastClickTime;
    private boolean isValuePropEnabled = false;

    public MyCoursesAdapter(Context context, IEdxEnvironment environment) {
        super(context, CourseCardViewHolder.LAYOUT, environment);
        lastClickTime = 0;
    }

    public void setValuePropEnabled(boolean isEnabled) {
        isValuePropEnabled = isEnabled;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void render(BaseViewHolder tag, final EnrolledCoursesResponse enrollment) {
        final CourseCardViewHolder holder = (CourseCardViewHolder) tag;

        final CourseEntry courseData = enrollment.getCourse();
        holder.setCourseTitle(courseData.getName());
        holder.setCourseImage(courseData.getCourse_image(environment.getConfig().getApiHostURL()));

        if (enrollment.getCourse().hasUpdates()) {
            holder.setHasUpdates(courseData, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAnnouncementClicked(enrollment);
                }
            });
        } else {
            holder.setDetails(CourseCardUtils.getFormattedDate(getContext(), enrollment));
        }
        if (isValuePropEnabled) {
            holder.setHasUpgradeOption(enrollment.getMode(), v -> onLearnMoreClicked(courseData.getId()));
        }
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

    public abstract void onLearnMoreClicked(String courseId);
}
