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

import org.edx.mobile.http.HttpException;
import org.edx.mobile.model.Page;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

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


public interface DiscussionService {
    @Headers("Cache-Control: no-cache")
    @GET("/api/discussion/v1/courses/{course_id}/")
    CourseDiscussionInfo getCourseDiscussionInfo(@Path("course_id") String courseId)
            throws HttpException;


    @GET("/api/discussion/v1/courses/{course_id}/")
    CourseDiscussionInfo getCourseDiscussionInfoWithCacheEnabled(@Path("course_id") String courseId)
            throws HttpException;


    @GET("/api/discussion/v1/course_topics/{course_id}")
    CourseTopics getCourseTopics(@Path("course_id") String courseId) throws HttpException;


    @GET("/api/discussion/v1/course_topics/{course_id}")
    CourseTopics getSpecificCourseTopics(@Path("course_id") String courseId,
                                         @Query("topic_id") List<String> topicIds)
            throws HttpException;


    @GET("/api/discussion/v1/threads/")
    Page<DiscussionThread> getThreadList(@Query("course_id") String courseId,
                                         @Query("topic_id") List<String> topicIds,
                                         @Query("view") String view,
                                         @Query("order_by") String orderBy,
                                         @Query("page_size") int pageSize,
                                         @Query("page") int page,
                                         @Query("requested_fields") List<String> requestedFields)
            throws HttpException;


    @GET("/api/discussion/v1/threads/")
    Page<DiscussionThread> getFollowingThreadList(@Query("course_id") String courseId,
                                                  @Query("following") String following,
                                                  @Query("view") String view,
                                                  @Query("order_by") String orderBy,
                                                  @Query("page_size") int pageSize,
                                                  @Query("page") int page,
                                                  @Query("requested_fields")
                                                  List<String> requestedFields)
            throws HttpException;

    @GET("/api/discussion/v1/threads/")
    Page<DiscussionThread> searchThreadList(@Query("course_id") String courseId,
                                            @Query("text_search") String text,
                                            @Query("page_size") int pageSize,
                                            @Query("page") int page,
                                            @Query("requested_fields") List<String> requestedFields)
            throws HttpException;


    @GET("/api/discussion/v1/threads/{thread_id}/")
    DiscussionThread getThread(@Path("thread_id") String threadId)
            throws HttpException;


    @GET("/api/discussion/v1/comments/")
    Page<DiscussionComment> getResponsesList(@Query("thread_id") String threadId,
                                             @Query("page_size") int pageSize,
                                             @Query("page") int page,
                                             @Query("requested_fields") List<String> requestedFields)
            throws HttpException;


    @GET("/api/discussion/v1/comments/")
    Page<DiscussionComment> getResponsesListForQuestion(@Query("thread_id") String threadId,
                                                        @Query("page_size") int pageSize,
                                                        @Query("page") int page,
                                                        @Query("endorsed") boolean endorsed,
                                                        @Query("requested_fields")
                                                        List<String> requestedFields)
            throws HttpException;


    @GET("/api/discussion/v1/comments/{comment_id}/")
    Page<DiscussionComment> getCommentsList(@Path("comment_id") String responseId,
                                            @Query("page_size") int pageSize,
                                            @Query("page") int page,
                                            @Query("requested_fields") List<String> requestedFields)
            throws HttpException;


    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    DiscussionThread setThreadFlagged(@Path("thread_id") String threadId,
                                      @Body FlagBody flagBody)
            throws HttpException;


    @PATCH("/api/discussion/v1/comments/{comment_id}/")
    DiscussionComment setCommentFlagged(@Path("comment_id") String commentId,
                                        @Body FlagBody flagBody)
            throws HttpException;


    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    DiscussionThread setThreadVoted(@Path("thread_id") String threadId,
                                    @Body VoteBody voteBody)
            throws HttpException;


    @PATCH("/api/discussion/v1/comments/{comment_id}/")
    DiscussionComment setCommentVoted(@Path("comment_id") String commentId,
                                      @Body VoteBody voteBody)
            throws HttpException;


    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    DiscussionThread setThreadFollowed(@Path("thread_id") String threadId,
                                       @Body FollowBody followBody)
            throws HttpException;


    @PATCH("/api/discussion/v1/threads/{thread_id}/")
    DiscussionThread setThreadRead(@Path("thread_id") String threadId,
                                   @Body ReadBody readBody)
            throws HttpException;


    @POST("/api/discussion/v1/threads/")
    DiscussionThread createThread(@Body ThreadBody threadBody)
            throws HttpException;


    @FormUrlEncoded
    @POST("/api/discussion/v1/comments/")
    DiscussionComment createComment(@Field("thread_id") String threadId,
                                    @Field("raw_body") String rawBody,
                                    @Field("parent_id") String parentId)
            throws HttpException;
}
