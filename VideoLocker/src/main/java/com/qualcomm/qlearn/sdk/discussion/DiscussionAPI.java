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

import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

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

    public void getThreadList(String courseId, String topicId, final APICallback<TopicThreads> callback) {
        DiscussionService discussionService = createService();
        discussionService.getThreadList(courseId, topicId, new Callback<TopicThreads>() {
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

    public void getCommentList(String threadId, final APICallback<List<DiscussionComment>> callback) {
        DiscussionService discussionService = createService();
        discussionService.getCommentList(threadId, new Callback<List<DiscussionComment>>() {
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
