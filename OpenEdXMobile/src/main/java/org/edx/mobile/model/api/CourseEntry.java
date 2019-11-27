package org.edx.mobile.model.api;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.social.SocialMember;
import org.edx.mobile.util.UnicodeCharacters;
import org.edx.mobile.util.UrlUtil;
import org.edx.mobile.util.images.CourseCardUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class CourseEntry implements Serializable {
    private List<SocialMember> members_list;

    private LatestUpdateModel latest_updates;
    private String start; // start date
    private String course_image;
    private String end; // completion date
    private String dynamic_upgrade_deadline; // Upgrade deadline to get unlimited access to the course as long as it exists.
    private String start_display;
    private StartType start_type;
    @NonNull private String name;
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
    @Nullable private Map<String, String> course_sharing_utm_parameters;

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

    @Nullable
    public String getCourse_image(@Nullable String baseURL) {
        return UrlUtil.makeAbsolute(course_image, baseURL);
    }

    public String getCourse_image() {
        return course_image;
    }

    public void setCourse_image(String course_image) {
        this.course_image = course_image;
    }

    public String getDynamicUpgradeDeadline() {
        return dynamic_upgrade_deadline;
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

    @NonNull
    public String getName() {
        return name.replaceAll("-", String.valueOf(UnicodeCharacters.NON_BREAKING_HYPHEN));
    }

    public void setName(@NonNull String name) {
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
        return CourseCardUtils.isDatePassed(new Date(), start);
    }

    public boolean isEnded() {
        return CourseCardUtils.isDatePassed(new Date(), end);
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

    public String getDescriptionWithStartDate(Context context) {
        return CourseCardUtils.getDescription(org, number, CourseCardUtils.getFormattedDate(context, this));
    }

    @Nullable
    public String getCourseSharingUtmParams(@NonNull String sharingPlatformKey) {
        return course_sharing_utm_parameters == null ? null : course_sharing_utm_parameters.get(sharingPlatformKey);
    }
}
