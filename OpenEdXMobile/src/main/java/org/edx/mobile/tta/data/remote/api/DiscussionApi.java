package org.edx.mobile.tta.data.remote.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.discussion.CommentBody;
import org.edx.mobile.discussion.CourseTopics;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.ThreadBody;
import org.edx.mobile.model.Page;

import java.util.List;

import retrofit2.Call;

@Singleton
public class DiscussionApi {

    @Inject
    private DiscussionService discussionService;

    @Inject
    public DiscussionApi() {
    }

    public Call<CourseTopics> getCourseTopics(String courseId){
        return discussionService.getCourseTopics(courseId);
    }

    public Call<Page<DiscussionThread>> getThreadList(String courseId, List<String> topicIds,
                                               String view, String orderBy, int take, int page,
                                               List<String> requestedFields) {
        return discussionService.getThreadList(courseId, topicIds, view, orderBy, take, page, requestedFields);
    }

    public Call<Page<DiscussionComment>> getResponsesList(String threadId, int take, int page,
                                                          List<String> requestedFields, boolean isQuestionType){
        if (!isQuestionType) {
            return discussionService.getResponsesList(threadId, take, page, requestedFields);
        } else {
            return discussionService.getResponsesListForQuestion(threadId, take, page, false, requestedFields);
        }
    }

    public Call<Page<DiscussionComment>> getCommentsList(String responseId, int take, int page,
                                                         List<String> requestedFields){
        return discussionService.getCommentsList(responseId, take, page, requestedFields);
    }

    public Call<DiscussionThread> createThread(String courseId, String title, String body,
                                               String topicId, DiscussionThread.ThreadType type){
        ThreadBody threadBody = new ThreadBody();
        threadBody.setCourseId(courseId);
        threadBody.setTitle(title);
        threadBody.setRawBody(body);
        threadBody.setTopicId(topicId);
        threadBody.setType(type);
        return discussionService.createThread(threadBody);
    }

    public Call<DiscussionComment> createComment(String threadId, String comment, String parentCommentId){
        CommentBody commentBody = new CommentBody(threadId, comment, parentCommentId);
        return discussionService.createComment(commentBody);
    }

    public Call<DiscussionThread> likeThread(String threadId, boolean liked){
        return discussionService.setThreadVoted(threadId, new DiscussionService.VoteBody(liked));
    }

    public Call<DiscussionComment> likeComment(String commentId, boolean liked){
        return discussionService.setCommentVoted(commentId, new DiscussionService.VoteBody(liked));
    }
}
