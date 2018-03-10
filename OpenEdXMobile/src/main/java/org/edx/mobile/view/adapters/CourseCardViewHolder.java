package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.util.images.ImageUtils;
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;

public class CourseCardViewHolder extends BaseListAdapter.BaseViewHolder {

    @LayoutRes
    public static int LAYOUT = R.layout.row_course_list;

    private final ImageView courseImage;
    private final TextView courseTitle;
    private final TextView courseDetails;
    private final View newCourseContent;

    public CourseCardViewHolder(View convertView) {
        this.courseTitle = (TextView) convertView
                .findViewById(R.id.course_name);
        this.courseDetails = (TextView) convertView
                .findViewById(R.id.course_details);
        this.courseImage = (ImageView) convertView
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
                    .transform(new TopAnchorFillWidthTransformation(context))
                    .into(courseImage);
        }
    }

    public void setHasUpdates(@NonNull CourseEntry courseData, @NonNull View.OnClickListener listener) {
        courseDetails.setVisibility(View.GONE);
        newCourseContent.setVisibility(View.VISIBLE);
        newCourseContent.setTag(courseData);
        newCourseContent.setOnClickListener(listener);
    }

    public void setDetails(@NonNull String date) {
        newCourseContent.setVisibility(View.GONE);
        courseDetails.setVisibility(View.VISIBLE);
        courseDetails.setText(date);
    }
}
