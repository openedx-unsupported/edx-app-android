package org.edx.mobile.view.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;

public class CourseCardViewHolder extends BaseListAdapter.BaseViewHolder {

    @LayoutRes
    public static int LAYOUT = R.layout.row_course_list;

    private final View rootView;
    private final ImageView courseImage;
    private final TextView courseTitle;
    private final TextView courseRun;
    private final TextView startingFrom;
    private final View newCourseContent;

    public CourseCardViewHolder(View convertView) {
        this.rootView = convertView;
        this.courseTitle = (TextView) convertView
                .findViewById(R.id.course_name);
        this.courseRun = (TextView) convertView
                .findViewById(R.id.course_run);
        this.startingFrom = (TextView) convertView
                .findViewById(R.id.starting_from);
        this.courseImage = (ImageView) convertView
                .findViewById(R.id.course_image);
        this.newCourseContent = convertView
                .findViewById(R.id.new_course_content_layout);
    }

    public void setPadding(boolean isFirstItem) {
        rootView.setPadding(
                rootView.getPaddingLeft(),
                isFirstItem ? rootView.getResources().getDimensionPixelOffset(R.dimen.widget_margin) : 0,
                rootView.getPaddingRight(),
                rootView.getPaddingBottom());
    }

    public void setCourseTitle(@NonNull String title) {
        courseTitle.setText(title);
    }

    public void setCourseImage(@NonNull String imageUrl) {
        Glide.with(courseImage.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.edx_map)
                .transform(new TopAnchorFillWidthTransformation(courseImage.getContext()))
                .into(courseImage);
    }

    public void setHasUpdates(@NonNull CourseEntry courseData, @NonNull View.OnClickListener listener) {
        startingFrom.setVisibility(View.GONE);
        newCourseContent.setVisibility(View.VISIBLE);
        newCourseContent.setTag(courseData);
        newCourseContent.setOnClickListener(listener);
    }

    public void setDescription(@NonNull String description, @NonNull String formattedDate) {
        newCourseContent.setVisibility(View.GONE);
        startingFrom.setVisibility(View.VISIBLE);
        courseRun.setText(description);
        startingFrom.setText(formattedDate);
    }
}
