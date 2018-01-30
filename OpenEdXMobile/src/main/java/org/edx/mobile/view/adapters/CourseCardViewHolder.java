package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.internal.Animation;

import org.edx.mobile.R;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.view.custom.IconImageViewXml;

public class CourseCardViewHolder extends BaseListAdapter.BaseViewHolder {

    @LayoutRes
    public static int LAYOUT = R.layout.row_course_list;

    private final ImageView courseImage;
    private final TextView courseTitle;
    private final TextView startingFrom;
    private final LinearLayout courseStatusUnit;
    private final TextView courseDownloadStatus;
    private final IconImageViewXml courseDownloadStatusIcon;

    public CourseCardViewHolder(View convertView) {
        this.courseTitle = (TextView) convertView
                .findViewById(R.id.course_name);
        this.startingFrom = (TextView) convertView
                .findViewById(R.id.starting_from);
        this.courseImage = (ImageView) convertView
                .findViewById(R.id.course_image);
        this.courseStatusUnit = (LinearLayout) convertView
                .findViewById(R.id.status_layout);
        this.courseDownloadStatus = (TextView) convertView
                .findViewById(R.id.course_download_status);
        this.courseDownloadStatusIcon = (IconImageViewXml) convertView
                .findViewById(R.id.course_download_status_icon);
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

    public void showNoContentDownloadStatusContainer(Context context) {
        courseDownloadStatusIcon.setImageResource(R.drawable.ic_prohibition);
        courseDownloadStatusIcon.setPadding(5,5,5,5);
        courseDownloadStatus.setText(R.string.no_downloadable_content);
        courseDownloadStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
        courseStatusUnit.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_1));
    }

    public void showDownloadStatusContainer() {
        courseStatusUnit.setVisibility(View.VISIBLE);
    }

    public void updateDownloadStatus(Context context, DownloadEntry.DownloadedState state, View.OnClickListener listener, String relativeTimeStamp) {
        if(state == null){
            courseDownloadStatusIcon.setImageResource(R.drawable.ic_danger);
            courseDownloadStatusIcon.setPadding(5,5,5,5);
            courseDownloadStatus.setText(R.string.visit_course_to_download);
            courseDownloadStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
            courseStatusUnit.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_1));
            return;
        } else if (state == DownloadEntry.DownloadedState.DOWNLOADING) {
            courseDownloadStatusIcon.setIcon(FontAwesomeIcons.fa_spinner);
            courseDownloadStatusIcon.setIconAnimation(Animation.PULSE);
            courseDownloadStatusIcon.setIconColorResource(R.color.black);
            courseDownloadStatus.setText(R.string.downloading);
            courseDownloadStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
            courseStatusUnit.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_1));

        } else if (state == DownloadEntry.DownloadedState.DOWNLOADED) {
            courseDownloadStatusIcon.setImageResource(R.drawable.ic_done_black);
            courseDownloadStatusIcon.setIconAnimation(Animation.NONE);
            courseDownloadStatus.setText(String.format(context.getString(R.string.media_saved_time_ago), relativeTimeStamp));
            courseDownloadStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
            courseStatusUnit.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_1));

        } else if (state == DownloadEntry.DownloadedState.ONLINE) {
            courseDownloadStatusIcon.setImageResource(R.drawable.ic_download_media);
            courseDownloadStatusIcon.setIconAnimation(Animation.NONE);
            courseDownloadStatus.setText(R.string.label_download_media);
            courseDownloadStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
            courseStatusUnit.setBackgroundColor(ContextCompat.getColor(context, R.color.philu_bottom_bar_blue_bg));

        }

        courseStatusUnit.setOnClickListener(listener);
        if (listener == null) {
            courseStatusUnit.setClickable(false);
        }
    }
}
