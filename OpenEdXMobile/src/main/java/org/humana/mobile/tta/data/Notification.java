
package org.humana.mobile.tta.data;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Notification {

    @SerializedName("action_id")
    private String mActionId;
    @SerializedName("action_parent_id")
    private String mActionParentId;
    @SerializedName("action_type")
    private String mActionType;
    @SerializedName("desc")
    private String mDesc;
    @SerializedName("id")
    private Long mId;
    @SerializedName("note")
    private String mNote;
    @SerializedName("respondents")
    private String mRespondents;
    @SerializedName("schedule_date")
    private String mScheduleDate;
    @SerializedName("status")
    private String mStatus;
    @SerializedName("title")
    private String mTitle;

    public String getActionId() {
        return mActionId;
    }

    public void setActionId(String actionId) {
        mActionId = actionId;
    }

    public String getActionParentId() {
        return mActionParentId;
    }

    public void setActionParentId(String actionParentId) {
        mActionParentId = actionParentId;
    }

    public String getActionType() {
        return mActionType;
    }

    public void setActionType(String actionType) {
        mActionType = actionType;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String desc) {
        mDesc = desc;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public String getRespondents() {
        return mRespondents;
    }

    public void setRespondents(String respondents) {
        mRespondents = respondents;
    }

    public String getScheduleDate() {
        return mScheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        mScheduleDate = scheduleDate;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

}
