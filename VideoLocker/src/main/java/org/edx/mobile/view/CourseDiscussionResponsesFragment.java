package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionUtils;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.GetResponsesListTask;
import org.edx.mobile.view.adapters.CourseDiscussionResponsesAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionResponsesFragment extends BaseFragment implements CourseDiscussionResponsesAdapter.Listener {

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_responses, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Using application context to prevent activity leak since adapter is retained across config changes
        final Context context = getContext();
        courseDiscussionResponsesAdapter = new CourseDiscussionResponsesAdapter(
                context.getApplicationContext(), this, discussionThread);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        discussionResponsesRecyclerView.setLayoutManager(linearLayoutManager);
        controller = new PaginationController();
        controller.loadMore();
        discussionResponsesRecyclerView.addOnScrollListener(new InfiniteScrollUtils
                .RecyclerViewOnScrollListener(linearLayoutManager, controller));
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
            if (event.getParent() == null) {
                // We got a response
                courseDiscussionResponsesAdapter.addNewResponse(event.getComment());
            } else {
                // We got a comment to a response
                courseDiscussionResponsesAdapter.addNewComment(event.getParent());
            }
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

    private class PaginationController implements InfiniteScrollUtils.InfiniteListController {
        @Nullable
        private GetResponsesListTask getResponsesListTask;
        @Nullable
        private GetResponsesListTask getEndorsedListTask;

        private boolean loading;
        private int nextPage = 1;

        @Nullable
        private Page<DiscussionComment> unendorsedResponsesPage;
        // The number of the endorsed responses, or -1 if they haven't been queried yet.
        private int endorsedResponsesCount = -1;

        @Override
        public void loadMore() {
            if (!loading && (unendorsedResponsesPage == null ||
                    unendorsedResponsesPage.hasNext())) {
                loading = true;
                courseDiscussionResponsesAdapter.setProgressVisible(true);
                if (endorsedResponsesCount < 0 &&
                        discussionThread.getType() == DiscussionThread.ThreadType.QUESTION) {
                    getEndorsedList();
                }
                getResponsesList();
            }

        }

        /**
         * Gets the list of endorsed answers for a {@link DiscussionThread.ThreadType#QUESTION}
         * type discussion thread
         */
        protected void getEndorsedList() {
            if (getEndorsedListTask != null) {
                getEndorsedListTask.cancel(true);
            }
            getEndorsedListTask = new GetResponsesListTask(getContext(),
                    discussionThread.getIdentifier(), 1,
                    discussionThread.getType() == DiscussionThread.ThreadType.QUESTION, true) {
                @Override
                public void onSuccess(Page<DiscussionComment> threadResponsesPage) {
                    if (getEndorsedListTask == this) {
                        endorsedResponsesCount = threadResponsesPage.getCount();
                        courseDiscussionResponsesAdapter.addAll(threadResponsesPage.getResults());
                        // If the unendorsed call returned earlier than this one
                        if (unendorsedResponsesPage != null) deliverResult();
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
            getResponsesListTask = new GetResponsesListTask(getContext(),
                    discussionThread.getIdentifier(), nextPage,
                    discussionThread.getType() == DiscussionThread.ThreadType.QUESTION, false) {
                @Override
                public void onSuccess(final Page<DiscussionComment> threadResponsesPage) {
                    if (getResponsesListTask == this) {
                        unendorsedResponsesPage = threadResponsesPage;
                        if (endorsedResponsesCount >= 0 || discussionThread.getType() !=
                                DiscussionThread.ThreadType.QUESTION) {
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
            int responseCount = unendorsedResponsesPage.getCount();
            if (endorsedResponsesCount > 0) {
                responseCount += endorsedResponsesCount;
            }
            discussionThread.setResponseCount(responseCount);
            courseDiscussionResponsesAdapter.setResponseCount(responseCount);
            courseDiscussionResponsesAdapter.addAll(unendorsedResponsesPage.getResults());
            ++nextPage;
            if (!unendorsedResponsesPage.hasNext()) {
                courseDiscussionResponsesAdapter.setProgressVisible(false);
            }
            loading = false;
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
            unendorsedResponsesPage = null;
            endorsedResponsesCount = -1;
            nextPage = 1;
            loading = false;
            courseDiscussionResponsesAdapter.clear();
            loadMore();
        }
    }

}
