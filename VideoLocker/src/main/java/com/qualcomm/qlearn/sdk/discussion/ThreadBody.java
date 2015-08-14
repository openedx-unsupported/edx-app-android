package com.qualcomm.qlearn.sdk.discussion;


public class ThreadBody {
    String type;
    String courseId;
    String topicId;
    String title;
    String rawBody;


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
}
