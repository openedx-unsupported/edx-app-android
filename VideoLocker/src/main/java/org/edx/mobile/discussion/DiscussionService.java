/**
 Copyright (c) 2015 Qualcomm Education, Inc.
 All rights reserved.


 Redistribution and use in source and binary forms, with or without modification, are permitted (subject to the limitations in the disclaimer below) provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

 * Neither the name of Qualcomm Education, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package org.edx.mobile.discussion;

import com.google.inject.Inject;

import org.edx.mobile.model.Page;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static org.edx.mobile.http.ApiConstants.PARAM_PAGE_SIZE;

public interface DiscussionService {
    /**
     * A RoboGuice Provider implementation for DiscussionService.
     */
    class Provider implements com.google.inject.Provider<DiscussionService> {
        @Inject
        private Retrofit retrofit;

        @Override
        public DiscussionService get() {
            return retrofit.create(DiscussionService.class);
        }
    }

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/courses/{course_id}/")
    Call<CourseDiscussionInfo> getCourseDiscussionInfo(@Path("course_id") String courseId);


    @GET("/api/discussion/v1/courses/{course_id}/")
    Call<CourseDiscussionInfo> getCourseDiscussionInfoWithCacheEnabled(@Path("course_id") String courseId);


    @GET("/api/discussion/v1/course_topics/{course_id}")
    Call<CourseTopics> getCourseTopics(@Path("course_id") String courseId);


    @GET("/api/discussion/v1/course_topics/{course_id}")
    Call<CourseTopics> getSpecificCourseTopics(@Path("course_id") String courseId,
                                               @Query("topic_id") List<String> topicIds);


    @GET("/api/discussion/v1/threads?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionThread>> getThreadList(@Query("course_id") String courseId,
                                               @Query("topic_id") List<String> topicIds,
                                               @Query("view") String view,
                                               @Query("order_by") String orderBy,
                                               @Query("page") int page,
                                               @Query("requested_fields") List<String> requestedFields);


    @GET("/api/discussion/v1/threads?following=true&" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionThread>> getFollowingThreadList(@Query("course_id") String courseId,
                                                        @Query("view") String view,
                                                        @Query("order_by") String orderBy,
                                                        @Query("page") int page,
                                                        @Query("requested_fields")
                                                        List<String> requestedFields);

    @GET("/api/discussion/v1/threads?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionThread>> searchThreadList(@Query("course_id") String courseId,
                                                  @Query("text_search") String text,
                                                  @Query("page") int page,
                                                  @Query("requested_fields") List<String> requestedFields);


    @GET("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> getThread(@Path("thread_id") String threadId);


    @GET("/api/discussion/v1/comments?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionComment>> getResponsesList(@Query("thread_id") String threadId,
                                                   @Query("page") int page,
                                                   @Query("requested_fields") List<String> requestedFields);


    @GET("/api/discussion/v1/comments?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionComment>> getResponsesListForQuestion(@Query("thread_id") String threadId,
                                                              @Query("page") int page,
                                                              @Query("endorsed") boolean endorsed,
                                                              @Query("requested_fields")
                                                              List<String> requestedFields);


    @GET("/api/discussion/v1/comments/{comment_id}?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionComment>> getCommentsList(@Path("comment_id") String responseId,
                                                  @Query("page") int page,
                                                  @Query("requested_fields") List<String> requestedFields);


    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> setThreadFlagged(@Path("thread_id") String threadId,
                                            @Body FlagBody flagBody);


    @PATCH("/api/discussion/v1/comments/{comment_id}/")
    Call<DiscussionComment> setCommentFlagged(@Path("comment_id") String commentId,
                                              @Body FlagBody flagBody);


    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> setThreadVoted(@Path("thread_id") String threadId,
                                          @Body VoteBody voteBody);


    @PATCH("/api/discussion/v1/comments/{comment_id}/")
    Call<DiscussionComment> setCommentVoted(@Path("comment_id") String commentId,
                                            @Body VoteBody voteBody);


    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> setThreadFollowed(@Path("thread_id") String threadId,
                                             @Body FollowBody followBody);


    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> setThreadRead(@Path("thread_id") String threadId,
                                         @Body ReadBody readBody);


    @POST("/api/discussion/v1/threads/")
    Call<DiscussionThread> createThread(@Body ThreadBody threadBody);


    @FormUrlEncoded
    @POST("/api/discussion/v1/comments/")
    Call<DiscussionComment> createComment(@Field("thread_id") String threadId,
                                          @Field("raw_body") String rawBody,
                                          @Field("parent_id") String parentId);

    final class FlagBody {
        private boolean abuseFlagged;

        public FlagBody(boolean abuseFlagged) {
            this.abuseFlagged = abuseFlagged;
        }
    }

    final class VoteBody {
        private boolean voted;

        public VoteBody(boolean voted) {
            this.voted = voted;
        }
    }

    final class FollowBody {
        private boolean following;

        public FollowBody(boolean following) {
            this.following = following;
        }
    }

    final class ReadBody {
        private boolean read;

        public ReadBody(boolean read) {
            this.read = read;
        }
    }
}
