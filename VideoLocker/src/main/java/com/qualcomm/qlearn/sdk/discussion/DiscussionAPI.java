/**
 Copyright (c) 2015 Qualcomm Education, Inc.
 All rights reserved.


 Redistribution and use in source and binary forms, with or without modification, are permitted (subject to the limitations in the disclaimer below) provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

 * Neither the name of Qualcomm Education, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package com.qualcomm.qlearn.sdk.discussion;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.Config;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

enum DiscussionPostsFilter {
    Unread,
    Unanswered,
    All
}

enum DiscussionPostsSort {
    LastActivityAt,
    VoteCount,
    None
}

/*
// TODO: fix the issue - try to simplify the callback implementation
class RetrofitAdaptor<T> extends Callback {
    final APICallback<T> apiCallback;
    public RetrofitAdaptor(APICallback<T> apiCallback) {
        this.apiCallback = apiCallback;
    }

    @Override
    void success(T t, Response response) {
        apiCallback.success(t);
    }

    @Override
    public void failure(RetrofitError error) {
        if (apiCallback != null)
            apiCallback.failure(error);
    }

}
*/

public class DiscussionAPI {

    @Inject
    Config config;

    DiscussionService createService() {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

        Builder restBuilder = new RestAdapter.Builder()
                .setEndpoint("https://mobile-demo.sandbox.edx.org") //config.getApiHostURL())
                .setLogLevel(RestAdapter.LogLevel.FULL) // TODO: comment this for release
                .setConverter(new GsonConverter(gson));
        restBuilder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                //request.addHeader("Accept", "application/json");
                PrefManager pref = new PrefManager(MainApplication.instance(), PrefManager.Pref.LOGIN);
                AuthResponse auth = pref.getCurrentAuth();
                String token;
                if (auth == null || !auth.isSuccess()) {
                    // this might be a login with Facebook or Google
                    token = pref.getString(PrefManager.Key.AUTH_TOKEN_SOCIAL);
                } else {
                    token = auth.access_token;
                }
                request.addHeader("Authorization", "Bearer " + token);
            }
        });

        RestAdapter restAdapter = restBuilder.build();
        DiscussionService discussionService = restAdapter.create(DiscussionService.class);
        return discussionService;
    }

    public void getTopicList(String courseId, final APICallback<CourseTopics> callback) {
        System.out.println("courseId=" + courseId);
        DiscussionService discussionService = createService();
        discussionService.getCourseTopics(courseId, new Callback<CourseTopics>() {
            @Override
            public void success(CourseTopics courseTopics, Response response) {
                // use this to see response body: new String(((TypedByteArray) response.getBody()).getBytes()));
                if (callback != null)
                    callback.success(courseTopics);
            }

            @Override
            public void failure(RetrofitError error) {
                if (callback != null)
                    callback.failure(error);
            }
        });
    }

    public void getThreadList(String courseId, String topicId, DiscussionPostsFilter filter, DiscussionPostsSort orderBy, final APICallback<TopicThreads> callback) {
        DiscussionService discussionService = createService();

        String view;
        if (filter == DiscussionPostsFilter.Unread) view = "unread";
        else if (filter == DiscussionPostsFilter.Unanswered) view = "unanswered";
        else view = "";

        String order;
        if (orderBy == DiscussionPostsSort.LastActivityAt) order = "last_activity_at";
        else if (orderBy == DiscussionPostsSort.VoteCount) order = "vote_count";
        else order = "";

        discussionService.getThreadList(courseId, topicId, view, order, new Callback<TopicThreads>() {
            @Override
            public void success(TopicThreads discussionThreads, Response response) {
                callback.success(discussionThreads);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    public void searchThreadList(String courseId, String text, final APICallback<TopicThreads> callback) {
        DiscussionService discussionService = createService();
        discussionService.searchThreadList(courseId, text, new Callback<TopicThreads>() {
            @Override
            public void success(TopicThreads discussionThreads, Response response) {
                callback.success(discussionThreads);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    // get the responses, and all comments for each of which, of a thread
    public void getCommentList(String threadId, final APICallback<ThreadComments> callback) {
        DiscussionService discussionService = createService();
        discussionService.getCommentList(threadId, new Callback<ThreadComments>() {
            @Override
            public void success(ThreadComments threadComments, Response response) {
                // each of threadComments's results has a children field which are comments for this response
                callback.success(threadComments);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    public void flagThread(DiscussionThread thread, Boolean flagged, final APICallback<DiscussionThread> callback) {
        DiscussionService discussionService = createService();
        FlagBody flagBody = new FlagBody(flagged);
        discussionService.flagThread(thread.getIdentifier(), flagBody, new Callback<DiscussionThread>() {
            @Override
            public void success(DiscussionThread thread, Response response) {
                callback.success(thread);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    public void flagComment(DiscussionComment comment, Boolean flagged, final APICallback<DiscussionComment> callback) {
        DiscussionService discussionService = createService();
        FlagBody flagBody = new FlagBody(flagged);
        discussionService.flagComment(comment.getIdentifier(), flagBody, new Callback<DiscussionComment>() {
            @Override
            public void success(DiscussionComment comment, Response response) {
                callback.success(comment);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    public void voteThread(DiscussionThread thread, Boolean voted, final APICallback<DiscussionThread> callback) {
        DiscussionService discussionService = createService();
        VoteBody voteBody = new VoteBody(voted);
        discussionService.voteThread(thread.getIdentifier(), voteBody, new Callback<DiscussionThread>() {
            @Override
            public void success(DiscussionThread thread, Response response) {
                callback.success(thread);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    public void voteComment(DiscussionComment comment, Boolean voted, final APICallback<DiscussionComment> callback) {
        DiscussionService discussionService = createService();
        VoteBody voteBody = new VoteBody(voted);
        discussionService.voteComment(comment.getIdentifier(), voteBody, new Callback<DiscussionComment>() {
            @Override
            public void success(DiscussionComment comment, Response response) {
                callback.success(comment);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    public void followThread(DiscussionThread thread, Boolean following, final APICallback<DiscussionThread> callback) {
        DiscussionService discussionService = createService();
        FollowBody followBody = new FollowBody(following);
        discussionService.followThread(thread.getIdentifier(), followBody, new Callback<DiscussionThread>() {
            @Override
            public void success(DiscussionThread thread, Response response) {
                callback.success(thread);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }


    public void createThread(ThreadBody threadBody, final APICallback<DiscussionThread> callback) {
        DiscussionService discussionService = createService();
        discussionService.createThread(threadBody, new Callback<DiscussionThread>() {
            @Override
            public void success(DiscussionThread thread, Response response) {
                callback.success(thread);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    public void createResponse(ResponseBody responseBody, final APICallback<DiscussionComment> callback) {
        DiscussionService discussionService = createService();
        discussionService.createResponse(responseBody, new Callback<DiscussionComment>() {
            @Override
            public void success(DiscussionComment comment, Response response) {
                callback.success(comment);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

    public void createComment(CommentBody commentBody, final APICallback<DiscussionComment> callback) {
        DiscussionService discussionService = createService();
        discussionService.createComment(commentBody, new Callback<DiscussionComment>() {
            @Override
            public void success(DiscussionComment comment, Response response) {
                callback.success(comment);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.failure(error);
            }
        });
    }

}
