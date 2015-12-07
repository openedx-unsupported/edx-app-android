package org.edx.mobile.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseDetail;


public abstract class FindCoursesListAdapter extends BaseListAdapter<CourseDetail> {
    private long lastClickTime;

    public FindCoursesListAdapter(Context context, IEdxEnvironment environment) {
        super(context, CourseCardViewHolder.LAYOUT, environment);
        lastClickTime = 0;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void render(BaseViewHolder tag, final CourseDetail courseData) {
        final CourseCardViewHolder holder = (CourseCardViewHolder) tag;
        holder.setCourseTitle(courseData.name);
        holder.setCourseImage(environment.getConfig().getApiHostURL() + courseData.media.course_image.uri);
        holder.setDescription(courseData.description, courseData.start_display);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new CourseCardViewHolder(convertView);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        //This time is checked to avoid taps in quick succession
        final long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            CourseDetail model = getItem(position);
            if (model != null) onItemClicked(model);
        }
    }

    public abstract void onItemClicked(CourseDetail model);
}
