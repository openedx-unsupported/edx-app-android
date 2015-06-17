package com.qualcomm.qlearn.sdk.discussion;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jakelim on 6/16/15.
 */
public class DiscussionAPI {

    DiscussionService createService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("TODO")
                .build();
        DiscussionService discussionService = restAdapter.create(DiscussionService.class);
        return discussionService;
    }

    void getTopicList(String courseId, final APICallback<CourseTopics> callback) {
        DiscussionService discussionService = createService();
        discussionService.getCourseTopics(courseId, new Callback<CourseTopics>() {
            @Override
            public void success(CourseTopics courseTopics, Response response) {
                callback.success(courseTopics);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    void getThreadList(String topicId, final APICallback<List<DiscussionThread>> callback) {
        DiscussionService discussionService = createService();
        discussionService.getThreadList(new Callback<List<DiscussionThread>>() {
            @Override
            public void success(List<DiscussionThread> discussionThreads, Response response) {
                callback.success(discussionThreads);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    void getCommentList(String threadId, final APICallback<List<DiscussionComment>> callback) {
        DiscussionService discussionService = createService();
        discussionService.getCommentList(new Callback<List<DiscussionComment>>() {
            @Override
            public void success(List<DiscussionComment> discussionComments, Response response) {
                callback.success(discussionComments);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }
}
