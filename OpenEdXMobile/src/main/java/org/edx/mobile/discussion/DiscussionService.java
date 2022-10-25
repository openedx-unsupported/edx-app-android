/**
 * Copyright (c) 2015 Qualcomm Education, Inc.
 * All rights reserved.
 * <p>
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted (subject to the limitations in the disclaimer below) provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * <p>
 * Neither the name of Qualcomm Education, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * <p>
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package org.edx.mobile.discussion;

import static org.edx.mobile.http.constants.ApiConstants.PARAM_PAGE_SIZE;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.http.provider.RetrofitProvider;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.discussion.CommentBody;
import org.edx.mobile.model.discussion.CourseDiscussionInfo;
import org.edx.mobile.model.discussion.CourseTopics;
import org.edx.mobile.model.discussion.DiscussionComment;
import org.edx.mobile.model.discussion.DiscussionThread;
import org.edx.mobile.model.discussion.ThreadBody;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DiscussionService {
    /**
     * A Provider implementation for DiscussionService.
     */
    @Module
    @InstallIn(SingletonComponent.class)
    class Provider {

        @Singleton
        @Provides
        public DiscussionService get(@NonNull RetrofitProvider retrofitProvider) {
            return retrofitProvider.getWithOfflineCache().create(DiscussionService.class);
        }
    }

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/courses/{course_id}/")
    Call<CourseDiscussionInfo> getCourseDiscussionInfo(@Path("course_id") String courseId);

    @GET("/api/discussion/v1/courses/{course_id}/")
    Call<CourseDiscussionInfo> getCourseDiscussionInfoWithCacheEnabled(@Path("course_id") String courseId);

    @GET("/api/discussion/v1/course_topics/{course_id}")
    Call<CourseTopics> getCourseTopics(@Path("course_id") String courseId);

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/course_topics/{course_id}")
    Call<CourseTopics> getSpecificCourseTopics(@Path("course_id") String courseId,
                                               @Query("topic_id") List<String> topicIds);

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/threads?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionThread>> getThreadList(@Query("course_id") String courseId,
                                               @Query("topic_id") List<String> topicIds,
                                               @Query("view") String view,
                                               @Query("order_by") String orderBy,
                                               @Query("page") int page,
                                               @Query("requested_fields") List<String> requestedFields);

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/threads?following=true&" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionThread>> getFollowingThreadList(@Query("course_id") String courseId,
                                                        @Query("view") String view,
                                                        @Query("order_by") String orderBy,
                                                        @Query("page") int page,
                                                        @Query("requested_fields")
                                                                List<String> requestedFields);

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/threads?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionThread>> searchThreadList(@Query("course_id") String courseId,
                                                  @Query("text_search") String text,
                                                  @Query("page") int page,
                                                  @Query("requested_fields") List<String> requestedFields);

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> getThread(@Path("thread_id") String threadId);

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/comments?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionComment>> getResponsesList(@Query("thread_id") String threadId,
                                                   @Query("page") int page,
                                                   @Query("requested_fields") List<String> requestedFields);

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/comments?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionComment>> getResponsesListForQuestion(@Query("thread_id") String threadId,
                                                              @Query("page") int page,
                                                              @Query("endorsed") boolean endorsed,
                                                              @Query("requested_fields")
                                                                      List<String> requestedFields);

    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/comments/{comment_id}?" + PARAM_PAGE_SIZE)
    Call<Page<DiscussionComment>> getCommentsList(@Path("comment_id") String responseId,
                                                  @Query("page") int page,
                                                  @Query("requested_fields") List<String> requestedFields);

    @Headers({"Cache-Control: no-cache", "Content-type: application/merge-patch+json"})
    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> setThreadFlagged(@Path("thread_id") String threadId,
                                            @Body FlagBody flagBody);

    @Headers({"Cache-Control: no-cache", "Content-type: application/merge-patch+json"})
    @PATCH("/api/discussion/v1/comments/{comment_id}/")
    Call<DiscussionComment> setCommentFlagged(@Path("comment_id") String commentId,
                                              @Body FlagBody flagBody);

    @Headers({"Cache-Control: no-cache", "Content-type: application/merge-patch+json"})
    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> setThreadVoted(@Path("thread_id") String threadId,
                                          @Body VoteBody voteBody);

    @Headers({"Cache-Control: no-cache", "Content-type: application/merge-patch+json"})
    @PATCH("/api/discussion/v1/comments/{comment_id}/")
    Call<DiscussionComment> setCommentVoted(@Path("comment_id") String commentId,
                                            @Body VoteBody voteBody);

    @Headers({"Cache-Control: no-cache", "Content-type: application/merge-patch+json"})
    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> setThreadFollowed(@Path("thread_id") String threadId,
                                             @Body FollowBody followBody);

    @Headers({"Cache-Control: no-cache", "Content-type: application/merge-patch+json"})
    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    Call<DiscussionThread> setThreadRead(@Path("thread_id") String threadId,
                                         @Body ReadBody readBody);

    @Headers("Cache-Control: no-cache")
    @POST("/api/discussion/v1/threads/")
    Call<DiscussionThread> createThread(@Body ThreadBody threadBody);

    @Headers("Cache-Control: no-cache")
    @POST("/api/discussion/v1/comments/")
    Call<DiscussionComment> createComment(@Body CommentBody commentBody);

    @Keep
    final class FlagBody {
        @SerializedName("abuse_flagged")
        private boolean abuseFlagged;

        public FlagBody(boolean abuseFlagged) {
            this.abuseFlagged = abuseFlagged;
        }
    }

    @Keep
    final class VoteBody {
        @SerializedName("voted")
        private boolean voted;

        public VoteBody(boolean voted) {
            this.voted = voted;
        }
    }

    @Keep
    final class FollowBody {
        @SerializedName("following")
        private boolean following;

        public FollowBody(boolean following) {
            this.following = following;
        }
    }

    @Keep
    final class ReadBody {
        @SerializedName("read")
        private boolean read;

        public ReadBody(boolean read) {
            this.read = read;
        }
    }
}
