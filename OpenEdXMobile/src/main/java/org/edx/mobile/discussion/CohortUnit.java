package org.edx.mobile.discussion;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * "cohorts": [{"group_id": 1, "group_name": "Cohort One"}, {"group_id": 2, "group_name": "Cohort Two"}]
 */
public class CohortUnit implements Serializable {
    @SerializedName("group_id")
    private int groupId;
    @SerializedName("group_name")
    private String groupName;


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}
