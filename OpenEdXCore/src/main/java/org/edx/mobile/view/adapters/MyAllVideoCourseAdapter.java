package org.edx.mobile.view.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;

public abstract class MyAllVideoCourseAdapter extends BaseListAdapter<EnrolledCoursesResponse> {
    private long lastClickTime;

    public MyAllVideoCourseAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_myvideo_course_list, environment);
        lastClickTime = 0;
    }

    @Override
    public void render(BaseViewHolder tag, EnrolledCoursesResponse enrollment) {
        ViewHolder holder = (ViewHolder) tag;

        CourseEntry courseData = enrollment.getCourse();
        holder.courseTitle.setText(courseData.getName());

        String code = courseData.getOrg()+ " | " + courseData.getNumber();
        holder.schoolCode.setText(code);
        String videos=enrollment.getVideoCountReadable() + ",";
        holder.no_of_videos.setText(videos);
        holder.size_of_videos.setText(MemoryUtil.format(getContext(), enrollment.size));

        Glide.with(getContext())
                .load(courseData.getCourse_image(environment.getConfig().getApiHostURL()))
                .placeholder(R.drawable.edx_map)
                .transform(new TopAnchorFillWidthTransformation(getContext()))
                .into(holder.courseImage);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.courseTitle = (TextView) convertView
                .findViewById(R.id.course_name);
        holder.schoolCode = (TextView) convertView
                .findViewById(R.id.school_code);
        holder.courseImage = (ImageView) convertView
                .findViewById(R.id.course_image);
        holder.no_of_videos = (TextView) convertView
                .findViewById(R.id.no_of_videos);
        holder.size_of_videos = (TextView) convertView
                .findViewById(R.id.size_of_videos);
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        ImageView courseImage;
        TextView courseTitle;
        TextView schoolCode;
        TextView no_of_videos;
        TextView size_of_videos;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
        //This has been used so that if user clicks continuously on the screen,
        //two activities should not be opened
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            EnrolledCoursesResponse model = (EnrolledCoursesResponse)adapterView.getItemAtPosition(position);
            if(model!=null) onItemClicked(model);
        }
    }

    public abstract void onItemClicked(EnrolledCoursesResponse model);
}
