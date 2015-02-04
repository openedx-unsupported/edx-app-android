package org.edx.mobile.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.edx.mobile.R;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.images.ImageCacheManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.view.custom.SocialFacePileView;

import java.util.List;


public abstract class MyCourseAdapter extends
BaseListAdapter<EnrolledCoursesResponse> {

    private long lastClickTime;

    private boolean showSocial;

    private CourseFriendsListener courseFriendsListener;

    public interface CourseFriendsListener {
        public void fetchCourseFriends(EnrolledCoursesResponse course);
    }

    public int getPositionForCourseId(String courseId){
        for(int i = 0; i < getCount(); i++){
            if(getItem(i).getCourse() != null && TextUtils.equals(getItem(i).getCourse().getId(), courseId))
                return i;
        }

        return -1;
    }

    public MyCourseAdapter(Context context, boolean showSocial, CourseFriendsListener courseFriendsListener ) {
        super(context, R.layout.row_course_list);
        this.courseFriendsListener = courseFriendsListener;
        this.showSocial = showSocial;
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
                        startDt = getContext().getString(R.string.label_starting_from)
                                + " - " + dateformat.format(startDate);                 
                        holder.starting_from.setText(startDt);
                        holder.starting_from_layout.setVisibility(View.VISIBLE);
                    }else{
                        holder.starting_from_layout.setVisibility(View.GONE);
                    }
                }
                else if (courseData.isStarted() && courseData.isEnded()) {
                    if(endDate!=null){
                        endDt = getContext().getString(R.string.label_ended)
                                + " - " + dateformat.format(endDate);
                        holder.starting_from.setText(endDt);
                        holder.starting_from_layout.setVisibility(View.VISIBLE);
                    }else{
                        holder.starting_from_layout.setVisibility(View.GONE);
                    }
                } else if (courseData.isStarted() && !courseData.isEnded()) {
                    if(endDate!=null){
                        endDt = getContext().getString(R.string.label_ending_on)
                                + " - " + dateformat.format(endDate);
                        holder.starting_from.setText(endDt);

                    }else{
                        holder.starting_from_layout.setVisibility(View.GONE);
                    }
                }
            } catch (Exception ex) {
                logger.error(ex);
            }

        }

        if(enrollment.isCertificateEarned()){
            holder.certificateBanner.setVisibility(View.VISIBLE);
        } else {
            holder.certificateBanner.setVisibility(View.GONE);
        }

        holder.courseImage.setDefaultImageResId(R.drawable.edx_map);
        holder.courseImage.setImageUrl(courseData.getCourse_image(getContext()),
                ImageCacheManager.getInstance().getImageLoader());
        holder.courseImage.setTag(courseData);

        if (showSocial) {

            holder.facePileContainer.setVisibility(View.INVISIBLE);
            List<SocialMember> membersList = enrollment.getCourse().getMembers_list();
            if (membersList == null) {
                courseFriendsListener.fetchCourseFriends(enrollment);
            } else if (membersList.size() > 0){
                holder.facePileContainer.setVisibility(View.VISIBLE);
                holder.facePileView.setMemberList(membersList);
            }

        } else {

            holder.facePileContainer.setVisibility(View.GONE);

        }

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
        holder.certificateBanner = (RelativeLayout) convertView
                .findViewById(R.id.course_certified_banner);
        holder.facePileView = (SocialFacePileView) convertView
                .findViewById(R.id.course_item_facepileview);
        holder.facePileContainer = (LinearLayout) convertView
                .findViewById(R.id.course_item_facepile_container);
        return holder;
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
        RelativeLayout certificateBanner;
        LinearLayout facePileContainer;
        SocialFacePileView facePileView;
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

    public boolean isShowSocial() {
        return showSocial;
    }

    public void setShowSocial(boolean showSocial) {

        if (showSocial != this.showSocial){
            this.showSocial = showSocial;
            this.notifyDataSetChanged();
        }

    }

}
