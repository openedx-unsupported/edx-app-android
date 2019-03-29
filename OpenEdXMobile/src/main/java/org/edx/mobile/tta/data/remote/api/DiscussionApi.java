package org.edx.mobile.tta.data.remote.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.discussion.CourseTopics;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionThread;
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
                                                          List<String> requestedFields){
        return discussionService.getResponsesList(threadId, take, page, requestedFields);
    }

    public Call<Page<DiscussionComment>> getCommentsList(String responseId, int take, int page,
                                                         List<String> requestedFields){
        return discussionService.getCommentsList(responseId, take, page, requestedFields);
    }
}
