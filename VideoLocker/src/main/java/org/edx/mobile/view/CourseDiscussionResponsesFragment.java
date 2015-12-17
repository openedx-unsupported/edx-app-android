package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionUtils;
import org.edx.mobile.discussion.ThreadComments;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.GetResponsesListTask;
import org.edx.mobile.view.adapters.CourseDiscussionResponsesAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionResponsesFragment extends RoboFragment implements CourseDiscussionResponsesAdapter.Listener {

    @InjectView(R.id.discussion_responses_recycler_view)
    private RecyclerView discussionResponsesRecyclerView;

    @InjectView(R.id.create_new_item_text_view)
    private TextView addResponseTextView;

    @InjectView(R.id.create_new_item_layout)
    private ViewGroup addResponseLayout;

    @InjectExtra(Router.EXTRA_DISCUSSION_THREAD)
    private DiscussionThread discussionThread;

    @InjectExtra(value = Router.EXTRA_COURSE_DATA, optional = true)
    private EnrolledCoursesResponse courseData;

    private CourseDiscussionResponsesAdapter courseDiscussionResponsesAdapter;

    @Inject
    private Router router;

    private InfiniteScrollUtils.InfiniteListController controller;

    private ResponsesLoader responsesLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_responses, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        responsesLoader = new ResponsesLoader(getActivity(),
                discussionThread.getIdentifier(),
                discussionThread.getType() == DiscussionThread.ThreadType.QUESTION);

        // Using application context to prevent activity leak since adapter is retained across config changes
        courseDiscussionResponsesAdapter = new CourseDiscussionResponsesAdapter(
                getActivity().getApplicationContext(), this, discussionThread);
        controller = InfiniteScrollUtils.configureRecyclerViewWithInfiniteList(
                discussionResponsesRecyclerView, courseDiscussionResponsesAdapter, responsesLoader);
        discussionResponsesRecyclerView.setAdapter(courseDiscussionResponsesAdapter);

        DiscussionUtils.setStateOnTopicClosed(discussionThread.isClosed(),
                addResponseTextView, R.string.discussion_responses_add_response_button,
                R.string.discussion_add_response_disabled_title, addResponseLayout,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        router.showCourseDiscussionAddResponse(getActivity(), discussionThread);
                    }
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DiscussionCommentPostedEvent event) {
        if (discussionThread.containsComment(event.getComment())) {
            discussionThread.incrementCommentCount();
            courseDiscussionResponsesAdapter.setDiscussionThread(discussionThread);
            responsesLoader.reset();
            controller.reset();
        }
    }

    @Override
    public void onClickAuthor(@NonNull String username) {
        router.showUserProfile(getActivity(), username);
    }

    @Override
    public void onClickAddComment(@NonNull DiscussionComment response) {
        router.showCourseDiscussionAddComment(getActivity(), response);
    }

    @Override
    public void onClickViewComments(@NonNull DiscussionComment response) {
        router.showCourseDiscussionComments(getActivity(), response, discussionThread.isClosed());
    }

    private static class ResponsesLoader implements
            InfiniteScrollUtils.PageLoader<DiscussionComment> {
        private InfiniteScrollUtils.PageLoadCallback<DiscussionComment> callback;

        @NonNull
        private final Context context;
        @NonNull
        private final String threadId;
        private final boolean isQuestionTypeThread;

        @Nullable
        private GetResponsesListTask getResponsesListTask;
        @Nullable
        private GetResponsesListTask getEndorsedListTask;

        private int nextPage = 1;

        @Nullable
        private ThreadComments unendorsedResponses;
        private boolean isEndorsedFetched = false;

        public ResponsesLoader(@NonNull Context context, @NonNull String threadId,
                               boolean isQuestionTypeThread) {
            this.context = context;
            this.threadId = threadId;
            this.isQuestionTypeThread = isQuestionTypeThread;
        }

        @Override
        public void loadNextPage(@NonNull InfiniteScrollUtils.PageLoadCallback<DiscussionComment> callback) {
            this.callback = callback;
            if (isQuestionTypeThread && !isEndorsedFetched) {
                getEndorsedList();
            }
            getResponsesList();

        }

        /**
         * Gets the list of endorsed answers for a {@link DiscussionThread.ThreadType#QUESTION}
         * type discussion thread
         */
        protected void getEndorsedList() {
            if (getEndorsedListTask != null) {
                getEndorsedListTask.cancel(true);
            }
            getEndorsedListTask = new GetResponsesListTask(context, threadId, 1, isQuestionTypeThread,
                    true) {
                @Override
                public void onSuccess(ThreadComments threadResponses) {
                    if (callback != null) {
                        isEndorsedFetched = true;
                        callback.onPartialPageLoaded(threadResponses.getResults());
                        // If the unendorsed call returned earlier than this one
                        if (unendorsedResponses != null) deliverResult();
                    }
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                }
            };
            getEndorsedListTask.setProgressCallback(null);
            getEndorsedListTask.execute();
        }

        protected void getResponsesList() {
            if (getResponsesListTask != null) {
                getResponsesListTask.cancel(true);
            }
            getResponsesListTask = new GetResponsesListTask(context, threadId, nextPage,
                    isQuestionTypeThread, false) {
                @Override
                public void onSuccess(final ThreadComments threadResponses) {
                    if (callback != null) {
                        unendorsedResponses = threadResponses;
                        if (!isQuestionTypeThread || isEndorsedFetched) {
                            deliverResult();
                        }
                    }
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                }
            };
            getResponsesListTask.setProgressCallback(null);
            getResponsesListTask.execute();
        }

        private void deliverResult() {
            ++nextPage;
            boolean hasMore = unendorsedResponses.next != null &&
                    unendorsedResponses.next.length() > 0;
            callback.onPageLoaded(unendorsedResponses.getResults(), hasMore);
        }

        public void reset() {
            if (getResponsesListTask != null) {
                getResponsesListTask.cancel(true);
                getResponsesListTask = null;
            }
            if (getEndorsedListTask != null) {
                getEndorsedListTask.cancel(true);
                getEndorsedListTask = null;
            }
            unendorsedResponses = null;
            isEndorsedFetched = false;
            callback = null;
            nextPage = 1;
        }
    }

}
