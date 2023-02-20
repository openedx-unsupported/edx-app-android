package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.util.images.ImageUtils;
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;

public class CourseCardViewHolder extends BaseListAdapter.BaseViewHolder {

    @LayoutRes
    public static int LAYOUT = R.layout.row_course_list;

    private final AppCompatImageView courseImage;
    private final TextView courseTitle;
    private final TextView courseDetails;
    private final View newCourseContent;

    public CourseCardViewHolder(View convertView) {
        this.courseTitle = (TextView) convertView
                .findViewById(R.id.course_name);
        this.courseDetails = (TextView) convertView
                .findViewById(R.id.course_details);
        this.courseImage = (AppCompatImageView) convertView
                .findViewById(R.id.course_image);
        this.newCourseContent = convertView
                .findViewById(R.id.new_course_content_layout);
    }

    public void setCourseTitle(@NonNull String title) {
        courseTitle.setText(title);
    }

    public void setCourseImage(@NonNull String imageUrl) {
        final Context context = courseImage.getContext();
        if (ImageUtils.isValidContextForGlide(context)) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_course_card_image)
                    .transform(new TopAnchorFillWidthTransformation())
                    .into(courseImage);
        }
    }

    public void setDetails(@NonNull String date) {
        newCourseContent.setVisibility(View.GONE);
        courseDetails.setVisibility(View.VISIBLE);
        courseDetails.setText(date);
    }
}
