package org.edx.mobile.discussion;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * /api/discussion/v1/courses/{course_id}/
 * Retrieve the discussion information for a course (the Course resource above). No parameters.
 */
public class CourseDiscussionInfo implements Serializable{
    @SerializedName("id")
    private  String id;
    @SerializedName("discussions_enabled")
    private boolean discussionsEnabled;
    @SerializedName("blackouts")
    private List<TimePeriod> blackoutList;
    @SerializedName("thread_list_url")
    private String threadListUrl;
    @SerializedName("following_thread_list_url")
    private String followingThreadListUrl;
    @SerializedName("flagged_thread_list_url")
    private String flaggedThreadListUrl;
    @SerializedName("topics_url")
    private String topicsUrl;
    @SerializedName("cohorts")
    private List<CohortUnit> cohortList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDiscussionsEnabled() {
        return discussionsEnabled;
    }

    public void setDiscussionsEnabled(boolean discussionsEnabled) {
        this.discussionsEnabled = discussionsEnabled;
    }


    public String getThreadListUrl() {
        return threadListUrl;
    }

    public void setThreadListUrl(String threadListUrl) {
        this.threadListUrl = threadListUrl;
    }

    public String getFollowingThreadListUrl() {
        return followingThreadListUrl;
    }

    public void setFollowingThreadListUrl(String followingThreadListUrl) {
        this.followingThreadListUrl = followingThreadListUrl;
    }

    public String getFlaggedThreadListUrl() {
        return flaggedThreadListUrl;
    }

    public void setFlaggedThreadListUrl(String flaggedThreadListUrl) {
        this.flaggedThreadListUrl = flaggedThreadListUrl;
    }

    public String getTopicsUrl() {
        return topicsUrl;
    }

    public void setTopicsUrl(String topicsUrl) {
        this.topicsUrl = topicsUrl;
    }

    public List<TimePeriod> getBlackoutList() {
        return blackoutList;
    }

    public void setBlackoutList(List<TimePeriod> blackoutList) {
        this.blackoutList = blackoutList;
    }

    public List<CohortUnit> getCohortList() {
        return cohortList;
    }

    public void setCohortList(List<CohortUnit> cohortList) {
        this.cohortList = cohortList;
    }
}
