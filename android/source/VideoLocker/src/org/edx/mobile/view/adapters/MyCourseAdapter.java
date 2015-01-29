
package org.edx.mobile.view.adapters;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.edx.mobile.R;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.images.ImageCacheManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

public abstract class MyCourseAdapter extends
BaseListAdapter<EnrolledCoursesResponse> {

    private long lastClickTime;

    public MyCourseAdapter(Context context) {
        super(context);
        lastClickTime = 0;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void render(BaseViewHolder tag, final EnrolledCoursesResponse enrollment) {

        ViewHolder holder = (ViewHolder) tag;

        CourseEntry courseData = enrollment.getCourse();
        holder.courseTitle.setText(courseData.getName());

        if (courseData.getOrg() != null) {
            holder.orgCodeTv.setVisibility(View.VISIBLE);
            holder.orgCodeTv.setText(courseData.getOrg()+" ");
        }else{
            holder.orSymbolTv.setVisibility(View.GONE);
            holder.orgCodeTv.setVisibility(View.GONE);
        }

        if (courseData.getNumber() != null) {
            holder.orSymbolTv.setVisibility(View.VISIBLE);
            holder.schoolCodeTv.setText(" "+courseData.getNumber());
        }else{
            holder.orSymbolTv.setVisibility(View.GONE);
            holder.schoolCodeTv.setVisibility(View.GONE);
        }

        if (enrollment.getCourse().hasUpdates()) {
            holder.new_course_content.setVisibility(View.VISIBLE);
            holder.starting_from_layout.setVisibility(View.GONE);
            holder.new_course_content.setTag(courseData);
            holder.new_course_content
            .setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    onAnnouncementClicked(enrollment);
                }
            });
        } else {
            try{
                holder.new_course_content.setVisibility(View.GONE);
                holder.starting_from_layout.setVisibility(View.VISIBLE);

                SimpleDateFormat dateformat = new SimpleDateFormat("MMMM dd");

                Date startDate = DateUtil.convertToDate(courseData.getStart());
                String startDt; 
                Date endDate = DateUtil.convertToDate(courseData.getEnd());
                String endDt; 

                if (!courseData.isStarted()) {
                    if(startDate!=null){
                        startDt = context.getString(R.string.label_starting_from)
                                + " - " + dateformat.format(startDate);                 
                        holder.starting_from.setText(startDt);
                        holder.starting_from_layout.setVisibility(View.VISIBLE);
                    }else{
                        holder.starting_from_layout.setVisibility(View.GONE);
                    }
                }
                else if (courseData.isStarted() && courseData.isEnded()) {
                    if(endDate!=null){
                        endDt = context.getString(R.string.label_ended)
                                + " - " + dateformat.format(endDate);
                        holder.starting_from.setText(endDt);
                        holder.starting_from_layout.setVisibility(View.VISIBLE);
                    }else{
                        holder.starting_from_layout.setVisibility(View.GONE);
                    }
                } else if (courseData.isStarted() && !courseData.isEnded()) {
                    if(endDate!=null){
                        endDt = context.getString(R.string.label_ending_on)
                                + " - " + dateformat.format(endDate);
                        holder.starting_from.setText(endDt);

                    }else{
                        holder.starting_from_layout.setVisibility(View.GONE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        holder.courseImage.setDefaultImageResId(R.drawable.edx_map);
        holder.courseImage.setImageUrl(courseData.getCourse_image(context),
                ImageCacheManager.getInstance().getImageLoader());
        holder.courseImage.setTag(courseData);

    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.courseTitle = (TextView) convertView
                .findViewById(R.id.course_name);
        holder.orgCodeTv = (TextView) convertView.
                findViewById(R.id.org_code_tv);
        holder.orSymbolTv = (TextView) convertView.
                findViewById(R.id.or_tv);
        holder.schoolCodeTv = (TextView) convertView
                .findViewById(R.id.school_code_tv);
        holder.starting_from = (TextView) convertView
                .findViewById(R.id.starting_from);
        holder.courseImage = (NetworkImageView) convertView
                .findViewById(R.id.course_image);
        holder.new_course_content = (LinearLayout) convertView
                .findViewById(R.id.new_course_content_layout);
        holder.starting_from_layout = (LinearLayout) convertView
                .findViewById(R.id.starting_from_layout);
        return holder;
    }

    @Override
    public int getListItemLayoutResId() {
        return R.layout.row_course_list;
    }

    private static class ViewHolder extends BaseViewHolder {
        NetworkImageView courseImage;
        TextView courseTitle;
        TextView schoolCodeTv;
        TextView orgCodeTv;
        TextView orSymbolTv;
        TextView starting_from;
        LinearLayout new_course_content;
        LinearLayout starting_from_layout;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
        //This time is checked to avoid taps in quick succession
        long currentTime = SystemClock.elapsedRealtime();   
        if (currentTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentTime;
            EnrolledCoursesResponse model = getItem(position);
            if(model!=null) onItemClicked(model);
        }
    }

    public abstract void onItemClicked(EnrolledCoursesResponse model);
    public abstract void onAnnouncementClicked(EnrolledCoursesResponse model);
}
