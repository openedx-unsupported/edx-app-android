package org.edx.mobile.view.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.edx.mobile.R;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation;

public class CourseCardViewHolder extends BaseListAdapter.BaseViewHolder {

    @LayoutRes
    public static int LAYOUT = R.layout.row_course_list;

    private final ImageView courseImage;
    private final TextView courseTitle;
    private final TextView startingFrom;

    public CourseCardViewHolder(View convertView) {
        this.courseTitle = (TextView) convertView
                .findViewById(R.id.course_name);
        this.startingFrom = (TextView) convertView
                .findViewById(R.id.starting_from);
        this.courseImage = (ImageView) convertView
                .findViewById(R.id.course_image);
    }

    public void setCourseTitle(@NonNull String title) {
        courseTitle.setText(title);
    }

    public void setCourseImage(@NonNull String imageUrl) {
        Glide.with(courseImage.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_course_card_image)
                .centerCrop()
                .into(courseImage);
    }

    public void setHasUpdates(@NonNull CourseEntry courseData, @NonNull View.OnClickListener listener) {
        startingFrom.setVisibility(View.GONE);
    }

    public void setDescription(@NonNull String formattedDate) {
        startingFrom.setVisibility(View.VISIBLE);
        startingFrom.setText(formattedDate);
    }
}
