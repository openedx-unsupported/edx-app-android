package org.humana.mobile.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.model.api.CourseEntry;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.util.images.CourseCardUtils;

import java.util.List;


public abstract class MyCoursesAdapter extends BaseListAdapter<EnrolledCoursesResponse> {
    private long lastClickTime;

    public MyCoursesAdapter(Context context, IEdxEnvironment environment) {
        super(context, CourseCardViewHolder.LAYOUT, environment);
        lastClickTime = 0;
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
                holder.setDetails(CourseCardUtils.getFormattedDate(getContext(), courseData));
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
}
