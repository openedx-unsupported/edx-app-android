package org.edx.mobile.model.discussion;

import com.google.gson.annotations.SerializedName;

public class ThreadBody {

    @SerializedName("type")
    String type;

    @SerializedName("course_id")
    String courseId;

    @SerializedName("topic_id")
    String topicId;

    @SerializedName("title")
    String title;

    @SerializedName("raw_body")
    String rawBody;

    // Automatically follow posts when creating a new post (default behavior on desktop) [MA-1380]
    @SerializedName("following")
    boolean following = true;

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public void setType(DiscussionThread.ThreadType type) {
        // post type passed to the create post API. case-sensitive!
        if (type == DiscussionThread.ThreadType.DISCUSSION)
            this.type = "discussion";
        else
            this.type = "question";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRawBody(String rawBody) {
        this.rawBody = rawBody;
    }

    public void setFollowing(boolean isFollowing) {
        following = isFollowing;
    }
}
