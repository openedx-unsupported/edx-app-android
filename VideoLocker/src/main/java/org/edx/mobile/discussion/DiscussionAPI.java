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

import org.edx.mobile.http.RetroHttpException;
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
    Context context;
    final DiscussionService discussionService;

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
     * @throws RetroHttpException
     */
    public CourseDiscussionInfo getCourseDiscussionInfo(String courseId, boolean preferCache)
            throws RetroHttpException {
        if (!NetworkUtil.isConnected(context)) {
            return discussionService.getCourseDiscussionInfoWithCacheEnabled(courseId);
        } else if (preferCache) {
            return discussionService.getCourseDiscussionInfoWithCacheEnabled(courseId);
        } else {
            return discussionService.getCourseDiscussionInfo(courseId);
        }
    }

    public CourseTopics getTopicList(String courseId) throws RetroHttpException {
        return discussionService.getCourseTopics(courseId);
    }

    public TopicThreads getThreadList(String courseId, List<String> topicIds, String filter,
                                      String orderBy, int pageSize, int page)
            throws RetroHttpException {
        return discussionService.getThreadList(courseId, topicIds, filter, orderBy, pageSize, page);
    }

    public TopicThreads getFollowingThreadList(String courseId, String filter, String orderBy,
                                               int pageSize, int page)
            throws RetroHttpException {
        return discussionService.getFollowingThreadList(courseId, "True", filter, orderBy,
                pageSize, page);
    }


    public TopicThreads searchThreadList(String courseId, String text, int pageSize, int page)
            throws RetroHttpException {
        return discussionService.searchThreadList(courseId, text, pageSize, page);
    }

    // get the responses, and all comments for each of which, of a thread
    public ThreadComments getResponsesList(String threadId, int pageSize, int page)
            throws RetroHttpException {
        return discussionService.getResponsesList(threadId, pageSize, page);
    }

    public ThreadComments getResponsesListForQuestion(String threadId, int pageSize, int page,
                                                      boolean endorsed)
            throws RetroHttpException {
        return discussionService.getResponsesListForQuestion(threadId, pageSize, page, endorsed);
    }

    public DiscussionThread flagThread(DiscussionThread thread, boolean flagged)
            throws RetroHttpException {
        FlagBody flagBody = new FlagBody(flagged);
        return discussionService.flagThread(thread.getIdentifier(), flagBody);
    }

    public DiscussionComment flagComment(DiscussionComment comment, boolean flagged)
            throws RetroHttpException {
        FlagBody flagBody = new FlagBody(flagged);
        return discussionService.flagComment(comment.getIdentifier(), flagBody);
    }

    public DiscussionThread voteThread(DiscussionThread thread, boolean voted)
            throws RetroHttpException {
        VoteBody voteBody = new VoteBody(voted);
        return discussionService.voteThread(thread.getIdentifier(), voteBody);
    }

    public DiscussionComment voteComment(DiscussionComment comment, boolean voted)
            throws RetroHttpException {
        VoteBody voteBody = new VoteBody(voted);
        return discussionService.voteComment(comment.getIdentifier(), voteBody);
    }

    public DiscussionThread followThread(DiscussionThread thread, boolean following)
            throws RetroHttpException {
        FollowBody followBody = new FollowBody(following);
        return discussionService.followThread(thread.getIdentifier(), followBody);
    }


    public DiscussionThread createThread(ThreadBody threadBody) throws RetroHttpException {
        return discussionService.createThread(threadBody);
    }

    public DiscussionComment createResponse(ResponseBody responseBody) throws RetroHttpException {
        return discussionService.createResponse(responseBody);
    }

    public DiscussionComment createComment(CommentBody commentBody) throws RetroHttpException {
        return discussionService.createComment(commentBody.threadId, commentBody.rawBody,
                commentBody.parentId);
    }

}
