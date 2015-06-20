package com.qualcomm.qlearn.sdk.discussion;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by jakelim on 6/16/15.
 */
public interface DiscussionService {

    @GET("/api/discussion/v1/course_topics/{courseId}")
    void getCourseTopics(@Path("courseId") String courseId, Callback<CourseTopics> callback);

    @GET("/api/discussion/v1/threads/")
    void getThreadList(@Query("course_id") String courseId, @Query("topic_id") String topicId, Callback<List<DiscussionThread>> callback);

    @GET("/api/discussion/v1/comments/")
    void getCommentList(@Query("thread_id") String threadId, Callback<List<DiscussionComment>> callback);
}
