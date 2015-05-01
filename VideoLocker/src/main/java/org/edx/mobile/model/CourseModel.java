package org.edx.mobile.model;

import android.text.TextUtils;

import org.edx.mobile.model.api.LatestUpdateModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Course model maps to the natural course structure for navigation
 */
public class CourseModel extends CourseComponent implements ICourse{
    private List<IChapter> chapters;
    private LatestUpdateModel latestUpdateModel;
    private String start;
    private String courseImage;
    private String end;
    private String org;
    private String videoOutline;

    private String subscriptionId;
    private String courseHandout;
    private String courseUpdates;
    private String courseAbout;
    private boolean started;
    private boolean ended;
    private String number;
    private boolean hasUpdates;


    public CourseModel(){
        chapters = new ArrayList<>();
    }


    @Override
    public LatestUpdateModel getLatestUpdateModel() {
        return latestUpdateModel;
    }

    @Override
    public String getStart() {
        return start;
    }

    @Override
    public String getCourseImage() {
        return courseImage;
    }

    @Override
    public String getEnd() {
        return end;
    }



    @Override
    public String getOrg() {
        return org;
    }

    @Override
    public String getVideoOutline() {
        return videoOutline;
    }


    @Override
    public String getNumber() {
        return number;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isEnded() {
        return ended;
    }

    @Override
    public boolean hasUpdates() {
        return hasUpdates;
    }

    @Override
    public String getCourseAbout() {
        return courseAbout;
    }

    @Override
    public String getCourseUpdates() {
        return courseUpdates;
    }

    @Override
    public String getCourseHandout() {
        return courseHandout;
    }


    @Override
    public String getSubscriptionId() {
        return subscriptionId;
    }

    public List<IChapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<IChapter> chapters) {
        this.chapters = chapters;
    }

    public void setLatestUpdateModel(LatestUpdateModel latestUpdateModel) {
        this.latestUpdateModel = latestUpdateModel;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setCourseImage(String courseImage) {
        this.courseImage = courseImage;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public void setVideoOutline(String videoOutline) {
        this.videoOutline = videoOutline;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setCourseHandout(String courseHandout) {
        this.courseHandout = courseHandout;
    }

    public void setCourseUpdates(String courseUpdates) {
        this.courseUpdates = courseUpdates;
    }

    public void setCourseAbout(String courseAbout) {
        this.courseAbout = courseAbout;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setHasUpdates(boolean hasUpdates) {
        this.hasUpdates = hasUpdates;
    }

    public IChapter getChapterById(String cid){
        if (TextUtils.isEmpty(cid))
            return null;
        for (IChapter chapter : chapters){
            if ( cid.equalsIgnoreCase(chapter.getId()) )
                return chapter;
        }
        return null;
    }
}
