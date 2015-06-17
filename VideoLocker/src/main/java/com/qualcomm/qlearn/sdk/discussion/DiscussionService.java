package com.qualcomm.qlearn.sdk.discussion;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by jakelim on 6/16/15.
 */
public interface DiscussionService {

    @GET("/api/discussion/v1/course_topics/{courseId}")
    void getCourseTopics(@Path("courseId") String courseId, Callback<CourseTopics> callback);

    @GET("TODO")
    void getThreadList(Callback<List<DiscussionThread>> callback);

    @GET("TODO")
    void getCommentList(Callback<List<DiscussionComment>> callback);
}
