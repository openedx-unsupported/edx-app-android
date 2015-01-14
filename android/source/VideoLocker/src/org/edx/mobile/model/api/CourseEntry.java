package org.edx.mobile.model.api;

import java.io.Serializable;
import java.util.Date;

import org.edx.mobile.http.Api;
import org.edx.mobile.util.DateUtil;

import android.content.Context;

@SuppressWarnings("serial")
public class CourseEntry implements Serializable {

    private LatestUpdateModel latest_updates;
    private String start; // start date
    private String course_image;
    private String end; // completion date
    private String name;
    private String org;
    private String video_outline;
    private String course_about;
    private String course_updates;
    private String course_handouts;
    private String id;
    private String number;

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

    public String getCourse_image(Context context) {
        return new Api(context).getBaseUrl() + course_image;
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
}
