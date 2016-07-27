/**
 * Copyright (c) 2015 Qualcomm Education, Inc.
 * All rights reserved.
 * <p/>
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are permitted (subject to the limitations in the disclaimer below) provided that the following conditions are met:
 * <p/>
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * <p/>
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * <p/>
 * Neither the name of Qualcomm Education, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * <p/>
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package org.edx.mobile.discussion;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.http.ApiConstants;
import org.edx.mobile.http.HttpException;
import org.edx.mobile.model.Page;
import org.edx.mobile.util.NetworkUtil;

import java.util.List;

import retrofit.RestAdapter;

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
    private final Context context;
    private final DiscussionService discussionService;

    @Inject
    public DiscussionAPI(@NonNull Context context, @NonNull RestAdapter restAdapter) {
        this.context = context;
        discussionService = restAdapter.create(DiscussionService.class);
    }

    /**
     * as this is the meta data for course discussion info, it wont change frequently.
     * we should cache it in most cases?
     *
     * @param courseId
     * @return
     * @throws HttpException
     */
    public CourseDiscussionInfo getCourseDiscussionInfo(String courseId, boolean preferCache)
            throws HttpException {
        if (!NetworkUtil.isConnected(context)) {
            return discussionService.getCourseDiscussionInfoWithCacheEnabled(courseId);
        } else if (preferCache) {
            return discussionService.getCourseDiscussionInfoWithCacheEnabled(courseId);
        } else {
            return discussionService.getCourseDiscussionInfo(courseId);
        }
    }

    public CourseTopics getTopicList(String courseId) throws HttpException {
        return discussionService.getCourseTopics(courseId);
    }

    public CourseTopics getSpecificCourseTopics(String courseId, List<String> topicIds)
            throws HttpException {
        return discussionService.getSpecificCourseTopics(courseId, topicIds);
    }

    public Page<DiscussionThread> getThreadList(String courseId, List<String> topicIds,
                                                String filter, String orderBy, int page,
                                                List<String> requestedFields)
            throws HttpException {
        return discussionService.getThreadList(courseId, topicIds, filter, orderBy,
                ApiConstants.STANDARD_PAGE_SIZE, page, requestedFields);
    }

    public Page<DiscussionThread> getFollowingThreadList(String courseId, String filter,
                                                         String orderBy, int page,
                                                         List<String> requestedFields)
            throws HttpException {
        return discussionService.getFollowingThreadList(courseId, "True", filter, orderBy,
                ApiConstants.STANDARD_PAGE_SIZE, page, requestedFields);
    }


    public Page<DiscussionThread> searchThreadList(String courseId, String text, int page,
                                                   List<String> requestedFields)
            throws HttpException {
        return discussionService.searchThreadList(courseId, text, ApiConstants.STANDARD_PAGE_SIZE,
                page, requestedFields);
    }

    public DiscussionThread getThread(String threadId) throws HttpException {
        return discussionService.getThread(threadId);
    }

    public Page<DiscussionComment> getResponsesList(String threadId, int page,
                                                    List<String> requestedFields)
            throws HttpException {
        return discussionService.getResponsesList(threadId, ApiConstants.STANDARD_PAGE_SIZE, page,
                requestedFields);
    }

    public Page<DiscussionComment> getResponsesListForQuestion(String threadId, int page,
                                                               boolean endorsed,
                                                               List<String> requestedFields)
            throws HttpException {
        return discussionService.getResponsesListForQuestion(threadId,
                ApiConstants.STANDARD_PAGE_SIZE, page, endorsed, requestedFields);
    }

    public Page<DiscussionComment> getCommentsList(String responseId, int pageSize, int page,
                                                   List<String> requestedFields)
            throws HttpException {
        return discussionService.getCommentsList(responseId, pageSize, page, requestedFields);
    }

    public DiscussionThread setThreadFlagged(DiscussionThread thread, boolean flagged)
            throws HttpException {
        FlagBody flagBody = new FlagBody(flagged);
        return discussionService.setThreadFlagged(thread.getIdentifier(), flagBody);
    }

    public DiscussionComment setCommentFlagged(DiscussionComment comment, boolean flagged)
            throws HttpException {
        FlagBody flagBody = new FlagBody(flagged);
        return discussionService.setCommentFlagged(comment.getIdentifier(), flagBody);
    }

    public DiscussionThread setThreadVoted(DiscussionThread thread, boolean voted)
            throws HttpException {
        VoteBody voteBody = new VoteBody(voted);
        return discussionService.setThreadVoted(thread.getIdentifier(), voteBody);
    }

    public DiscussionComment setCommentVoted(DiscussionComment comment, boolean voted)
            throws HttpException {
        VoteBody voteBody = new VoteBody(voted);
        return discussionService.setCommentVoted(comment.getIdentifier(), voteBody);
    }

    public DiscussionThread setThreadFollowed(DiscussionThread thread, boolean following)
            throws HttpException {
        FollowBody followBody = new FollowBody(following);
        return discussionService.setThreadFollowed(thread.getIdentifier(), followBody);
    }

    public DiscussionThread setThreadRead(DiscussionThread thread, boolean read)
            throws HttpException {
        ReadBody readBody = new ReadBody(read);
        return discussionService.setThreadRead(thread.getIdentifier(), readBody);
    }

    public DiscussionThread createThread(ThreadBody threadBody) throws HttpException {
        return discussionService.createThread(threadBody);
    }

    public DiscussionComment createComment(CommentBody commentBody) throws HttpException {
        return discussionService.createComment(commentBody.threadId, commentBody.rawBody,
                commentBody.parentId);
    }

}
