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
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionUtils;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.task.GetAndReadThreadTask;
import org.edx.mobile.task.GetResponsesListTask;
import org.edx.mobile.view.adapters.CourseDiscussionResponsesAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionResponsesFragment extends BaseFragment implements CourseDiscussionResponsesAdapter.Listener {

    @InjectView(R.id.discussion_recycler_view)
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

    @Inject
    ISegment segIO;

    @Nullable
    private GetAndReadThreadTask getAndReadThreadTask;

    private InfiniteScrollUtils.InfiniteListController controller;

    private ResponsesLoader responsesLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_responses_or_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        responsesLoader = new ResponsesLoader(getActivity(),
                discussionThread.getIdentifier(),
                discussionThread.getType() == DiscussionThread.ThreadType.QUESTION);

        courseDiscussionResponsesAdapter = new CourseDiscussionResponsesAdapter(
                getActivity(), this, discussionThread);
        controller = InfiniteScrollUtils.configureRecyclerViewWithInfiniteList(
                discussionResponsesRecyclerView, courseDiscussionResponsesAdapter, responsesLoader);
        discussionResponsesRecyclerView.setAdapter(courseDiscussionResponsesAdapter);

        responsesLoader.freeze();
        if (getAndReadThreadTask != null) {
            getAndReadThreadTask.cancel(true);
        }
        // Setting a thread's "read" state gives us back the updated Thread object.
        getAndReadThreadTask = new GetAndReadThreadTask(getContext(), discussionThread) {
            @Override
            protected void onSuccess(DiscussionThread discussionThread) {
                super.onSuccess(discussionThread);
                courseDiscussionResponsesAdapter.updateDiscussionThread(discussionThread);
                responsesLoader.unfreeze();
            }
        };
        getAndReadThreadTask.setProgressCallback(null);
        getAndReadThreadTask.execute();

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

        Map<String, String> values = new HashMap<>();
        values.put(ISegment.Keys.TOPIC_ID, discussionThread.getTopicId());
        values.put(ISegment.Keys.THREAD_ID, discussionThread.getIdentifier());
        segIO.trackScreenView(ISegment.Screens.FORUM_VIEW_THREAD,
                courseData.getCourse().getId(), discussionThread.getTitle(), values);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        responsesLoader.reset();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DiscussionCommentPostedEvent event) {
        if (discussionThread.containsComment(event.getComment())) {
            if (event.getParent() == null) {
                // We got a response
                ((BaseFragmentActivity) getActivity()).showInfoMessage(getString(R.string.discussion_response_posted));
                if (!responsesLoader.hasMorePages()) {
                    courseDiscussionResponsesAdapter.addNewResponse(event.getComment());
                    discussionResponsesRecyclerView.smoothScrollToPosition(
                            courseDiscussionResponsesAdapter.getItemCount() - 1);
                } else {
                    // We still need to update the response count locally
                    courseDiscussionResponsesAdapter.incrementResponseCount();
                }
            } else {
                // We got a comment to a response
                if (event.getParent().getChildCount() == 0) {
                    // We only need to show this message when the first comment is added
                    ((BaseFragmentActivity) getActivity()).showInfoMessage(getString(R.string.discussion_comment_posted));
                }
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
        router.showCourseDiscussionAddComment(getActivity(), response, discussionThread);
    }

    @Override
    public void onClickViewComments(@NonNull DiscussionComment response) {
        router.showCourseDiscussionComments(getActivity(), response, discussionThread);
    }

    private static class ResponsesLoader implements
            InfiniteScrollUtils.PageLoader<DiscussionComment> {
        @NonNull
        private final Context context;
        @NonNull
        private final String threadId;
        private final boolean isQuestionTypeThread;
        private boolean hasMorePages = true;

        @Nullable
        private GetResponsesListTask getResponsesListTask;
        private int nextPage = 1;
        private boolean isFetchingEndorsed;
        private boolean isFrozen;
        private Runnable deferredDeliveryRunnable;

        public ResponsesLoader(@NonNull Context context, @NonNull String threadId,
                               boolean isQuestionTypeThread) {
            this.context = context;
            this.threadId = threadId;
            this.isQuestionTypeThread = isQuestionTypeThread;
            this.isFetchingEndorsed = isQuestionTypeThread;
        }

        @Override
        public void loadNextPage(@NonNull final InfiniteScrollUtils.PageLoadCallback<DiscussionComment> callback) {
            if (getResponsesListTask != null) {
                getResponsesListTask.cancel(true);
            }
            getResponsesListTask = new GetResponsesListTask(context, threadId, nextPage,
                    isQuestionTypeThread, isFetchingEndorsed) {
                @Override
                public void onSuccess(final Page<DiscussionComment> threadResponsesPage) {
                    if (getResponsesListTask == this) {
                        final Runnable deliverResultRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (isFetchingEndorsed) {
                                    boolean hasMoreEndorsed = threadResponsesPage.hasNext();
                                    if (hasMoreEndorsed) {
                                        ++nextPage;
                                    } else {
                                        isFetchingEndorsed = false;
                                        nextPage = 1;
                                    }
                                    final List<DiscussionComment> endorsedResponses =
                                            threadResponsesPage.getResults();
                                    if (hasMoreEndorsed || !endorsedResponses.isEmpty()) {
                                        callback.onPageLoaded(endorsedResponses);
                                    } else {
                                        // If there are no endorsed responses, then just start
                                        // loading the unendorsed ones without triggering the
                                        // callback, since that would just cause the controller
                                        // to wait for the scroll listener to be invoked, which
                                        // would not happen automatically without any changes
                                        // in the adapter dataset.
                                        loadNextPage(callback);
                                    }
                                } else {
                                    ++nextPage;
                                    callback.onPageLoaded(threadResponsesPage);
                                    hasMorePages = threadResponsesPage.hasNext();
                                }
                            }
                        };
                        if (isFrozen) {
                            deferredDeliveryRunnable = deliverResultRunnable;
                        } else {
                            deliverResultRunnable.run();
                        }
                    }
                }

                @Override
                protected void onException(Exception ex) {
                    super.onException(ex);
                    callback.onError();
                    nextPage = 1;
                    hasMorePages = false;
                }
            };
            getResponsesListTask.setProgressCallback(null);
            getResponsesListTask.execute();
        }

        public void freeze() {
            isFrozen = true;
        }

        public void unfreeze() {
            if (isFrozen) {
                isFrozen = false;
                if (deferredDeliveryRunnable != null) {
                    deferredDeliveryRunnable.run();
                    deferredDeliveryRunnable = null;
                }
            }
        }

        public void reset() {
            if (getResponsesListTask != null) {
                getResponsesListTask.cancel(true);
                getResponsesListTask = null;
            }
            isFetchingEndorsed = isQuestionTypeThread;
            nextPage = 1;
        }

        public boolean hasMorePages() {
            return hasMorePages;
        }
    }

}
