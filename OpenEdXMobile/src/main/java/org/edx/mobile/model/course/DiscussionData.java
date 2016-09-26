package org.edx.mobile.model.course;

import com.google.gson.annotations.SerializedName;

public class DiscussionData extends BlockData {

    @SerializedName("topic_id")
    public String topicId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiscussionData that = (DiscussionData) o;

        return topicId != null ? topicId.equals(that.topicId) : that.topicId == null;

    }
}
