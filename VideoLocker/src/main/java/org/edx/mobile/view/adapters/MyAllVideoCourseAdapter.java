package org.edx.mobile.view.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.edx.mobile.R;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.images.ImageCacheManager;

public abstract class MyAllVideoCourseAdapter extends BaseListAdapter<EnrolledCoursesResponse> {
    private long lastClickTime;

    public MyAllVideoCourseAdapter(Context context) {
        super(context, R.layout.row_myvideo_course_list);
        lastClickTime = 0;
    }

    @Override
    public void render(BaseViewHolder tag, EnrolledCoursesResponse enrollment) {
        ViewHolder holder = (ViewHolder) tag;

        CourseEntry courseData = enrollment.getCourse();
        holder.txtCourseTitle.setText(courseData.getName());

        String code = courseData.getOrg()+ " | " + courseData.getNumber();
        holder.txtSchoolCode.setText(code);
        String videos=enrollment.getVideoCountReadable() + ",";
        holder.txtNoOfVideos.setText(videos);
        holder.txtSizeOfVideos.setText(MemoryUtil.format(getContext(), enrollment.size));

        holder.imgCourse.setDefaultImageResId(R.drawable.edx_map);
        holder.imgCourse.setImageUrl(courseData.getCourse_image(getContext()),
                ImageCacheManager.getInstance().getImageLoader());
        holder.imgCourse.setTag(courseData);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.txtCourseTitle = (TextView) convertView
                .findViewById(R.id.course_name);
        holder.txtSchoolCode = (TextView) convertView
                .findViewById(R.id.school_code);
        holder.imgCourse = (NetworkImageView) convertView
                .findViewById(R.id.course_image);
        holder.txtNoOfVideos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.txtSizeOfVideos = (TextView) convertView
                .findViewById(R.id.size_of_videos);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        NetworkImageView imgCourse;
        TextView txtCourseTitle;
        TextView txtSchoolCode;
        TextView txtNoOfVideos;
        TextView txtSizeOfVideos;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        //This has been used so that if user clicks continuously on the screen, 
        //two activities should not be opened
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            EnrolledCoursesResponse model = getItem(position);
            if(model!=null) onItemClicked(model);
        }
    }

    public abstract void onItemClicked(EnrolledCoursesResponse model);
}
