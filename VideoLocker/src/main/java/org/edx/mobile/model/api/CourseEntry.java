package org.edx.mobile.model.api;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.SocialUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class CourseEntry implements Serializable {
    private List<SocialMember> members_list;

    private LatestUpdateModel latest_updates;
    private String start; // start date
    private String course_image;
    private String end; // completion date
    private String start_display;
    private StartType start_type;
    private String name;
    private String org;
    private String video_outline;
    private String course_about;
    private String course_updates;
    private String course_handouts;
    private String subscription_id;
    private String course_url;
    private String id;
    private String number;
    private String discussion_url;
    private SocialURLModel social_urls;
    private CoursewareAccess courseware_access;

    @Inject
    Config config;

    public LatestUpdateModel getLatest_updates() {
        return latest_updates;
    }

    public void setLatest_updates(LatestUpdateModel latest_updates) {
        this.latest_updates = latest_updates;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getCourse_image(Config config) {
        return config.getApiHostURL() + course_image;
    }

    public void setCourse_image(String course_image) {
        this.course_image = course_image;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStartDisplay() {
        return start_display;
    }

    public void setStartDisplay(String start_display) {
        this.start_display = start_display;
    }

    public StartType getStartType() {
        if(start_type == null) start_type = StartType.EMPTY;
        return start_type;
    }

    public void setStartType(StartType start_type) {
        this.start_type = start_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getVideo_outline() {
        return video_outline;
    }

    public void setVideo_outline(String video_outline) {
        this.video_outline = video_outline;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDiscussionUrl(){
        return discussion_url;
    }

    public void setDiscussionUrl(String url){
        discussion_url = url;
    }

    public CoursewareAccess getCoursewareAccess() { return courseware_access; }

    public void setCoursewareAccess(CoursewareAccess access) { this.courseware_access = access; }

    public boolean isStarted() {
        // check if "start" date has passed
        if (start == null)
            return false;

        Date startDate = DateUtil.convertToDate(start);
        Date today = new Date();
        return today.after(startDate);
    }

    public boolean isEnded() {
        // check if "end" date has passed
        if (end == null)
            return false;

        Date endDate = DateUtil.convertToDate(end);
        Date today = new Date();
        return today.after(endDate);
    }

    public boolean hasUpdates() {
        // check if latest updates available, return true if available
        if (latest_updates == null)
            return false;
        return (latest_updates.getVideo() != null);
    }

    public List<SocialMember> getMembers_list() { return members_list; }

    public void setMembers_list(List<SocialMember> members_list) {
        this.members_list = members_list;
    }

    public String getCourse_about() {
        return course_about;
    }

    public void setCourse_about(String course_about) {
        this.course_about = course_about;
    }

    /**
     * Returns URL for announcements or updates.
     * @return
     */
    public String getCourse_updates() {
        return course_updates;
    }

    public void setCourse_updates(String course_updates) {
        this.course_updates = course_updates;
    }

    /**
     * Returns URL for handouts.
     * @return
     */
    public String getCourse_handouts() {
        return course_handouts;
    }

    public void setCourse_handouts(String course_handouts) {
        this.course_handouts = course_handouts;
    }

    /**
     * the unique channel id, for notification service
     * @return
     */
    public String getSubscription_id() {
        return subscription_id;
    }

    public void setSubscription_id(String subscription_id) {
        this.subscription_id = subscription_id;
    }

    public String getCourse_url() {
        return course_url;
    }

    public void setCourse_url(String course_url) {
        this.course_url = course_url;
    }

    public String getCourseGroup(SocialUtils.SocialType type) {

        if (this.social_urls != null) {

            switch (type) {
                case FACEBOOK:
                    return this.social_urls.facebook;
            }

        }

        return null;

    }

    public boolean isGroupAvailable(SocialUtils.SocialType type) {

        if (this.social_urls != null) {

            switch (type) {
                case FACEBOOK:
                    return (this.social_urls.facebook != null && this.social_urls.facebook.length() > 0);
            }

        }

        return false;

    }

    public String getDescription(Context context, boolean withStartDate) {

        List<CharSequence> sections = new ArrayList<>();

        if (!TextUtils.isEmpty(org)) {
            sections.add(org);
        }

        if (!TextUtils.isEmpty(number)) {
            sections.add(number);
        }

        if (withStartDate) {
            CharSequence formattedDate = getFormattedDate(context);
            if (formattedDate != null) sections.add(formattedDate);
        }

        return TextUtils.join(" | ", sections);
    }

    public String getFormattedDate(Context context) {
        CharSequence formattedDate;
        if (isStarted()) {
            Date endDate = DateUtil.convertToDate(end);
            if (endDate == null) {
                return null;
            } else if (isEnded()) {
                formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                        .label_ended, "date", DateUtils.formatDateTime(context, endDate.getTime()
                        , DateUtils.FORMAT_NO_YEAR));
            } else {
                formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                        .label_ending, "date", DateUtils.formatDateTime(context, endDate.getTime
                        (), DateUtils.FORMAT_NO_YEAR));
            }
        } else {
            if (start_type == StartType.TIMESTAMP && !TextUtils.isEmpty(start)) {
                Date startDate = DateUtil.convertToDate(start);
                formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                        .label_starting, "date", DateUtils.formatDateTime(context, startDate
                        .getTime(), DateUtils.FORMAT_NO_YEAR));
            } else if (start_type == StartType.STRING && !TextUtils.isEmpty(start_display)) {
                formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                        .label_starting, "date", start_display);

            } else {
                formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                        .label_starting, "date", context.getString(R.string.assessment_soon));
            }
        }

        return formattedDate.toString().toUpperCase();
    }
}
