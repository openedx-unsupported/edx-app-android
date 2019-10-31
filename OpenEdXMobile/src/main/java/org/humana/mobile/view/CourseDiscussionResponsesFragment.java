package org.humana.mobile.view;

import android.app.Activity;
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

import org.humana.mobile.R;
import org.humana.mobile.base.BaseFragment;
import org.humana.mobile.base.BaseFragmentActivity;
import org.humana.mobile.discussion.DiscussionComment;
import org.humana.mobile.discussion.DiscussionCommentPostedEvent;
import org.humana.mobile.discussion.DiscussionRequestFields;
import org.humana.mobile.discussion.DiscussionService;
import org.humana.mobile.discussion.DiscussionService.ReadBody;
import org.humana.mobile.discussion.DiscussionThread;
import org.humana.mobile.discussion.DiscussionThreadUpdatedEvent;
import org.humana.mobile.discussion.DiscussionUtils;
import org.humana.mobile.http.callback.CallTrigger;
import org.humana.mobile.http.callback.ErrorHandlingCallback;
import org.humana.mobile.model.Page;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.module.analytics.Analytics;
import org.humana.mobile.module.analytics.AnalyticsRegistry;
import org.humana.mobile.view.adapters.CourseDiscussionResponsesAdapter;
import org.humana.mobile.view.adapters.InfiniteScrollUtils;
import org.humana.mobile.view.common.TaskMessageCallback;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import roboguice.RoboGuice;
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
    private DiscussionService discussionService;

    @Inject
    private Router router;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @Nullable
    private Call<DiscussionThread> getAndReadThreadCall;

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
        final Activity activity = getActivity();

        responsesLoader = new ResponsesLoader(activity,
                discussionThread.getIdentifier(),
                discussionThread.getType() == DiscussionThread.ThreadType.QUESTION);

        courseDiscussionResponsesAdapter = new CourseDiscussionResponsesAdapter(
                activity, this, this, discussionThread, courseData);
        controller = InfiniteScrollUtils.configureRecyclerViewWithInfiniteList(
                discussionResponsesRecyclerView, courseDiscussionResponsesAdapter, responsesLoader);
        discussionResponsesRecyclerView.setAdapter(courseDiscussionResponsesAdapter);

        responsesLoader.freeze();
        if (getAndReadThreadCall != null) {
            getAndReadThreadCall.cancel();
        }
        final TaskMessageCallback mCallback = activity instanceof TaskMessageCallback ? (TaskMessageCallback) activity : null;
        getAndReadThreadCall = discussionService.setThreadRead(
                discussionThread.getIdentifier(), new ReadBody(true));
        // Setting a thread's "read" state gives us back the updated Thread object.
        getAndReadThreadCall.enqueue(new ErrorHandlingCallback<DiscussionThread>(
                activity, null, mCallback, CallTrigger.LOADING_UNCACHED) {
            @Override
            protected void onResponse(@NonNull final DiscussionThread discussionThread) {
                courseDiscussionResponsesAdapter.updateDiscussionThread(discussionThread);
                responsesLoader.unfreeze();
                EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(discussionThread));
            }
        });

        DiscussionUtils.setStateOnTopicClosed(discussionThread.isClosed(),
                addResponseTextView, R.string.discussion_responses_add_response_button,
                R.string.discussion_add_response_disabled_title, addResponseLayout,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        router.showCourseDiscussionAddResponse(activity, discussionThread);
                    }
                });

        addResponseLayout.setEnabled(!courseData.isDiscussionBlackedOut());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.TOPIC_ID, discussionThread.getTopicId());
        values.put(Analytics.Keys.THREAD_ID, discussionThread.getIdentifier());
        if (!discussionThread.isAuthorAnonymous()) {
            values.put(Analytics.Keys.AUTHOR, discussionThread.getAuthor());
        }
        analyticsRegistry.trackScreenView(Analytics.Screens.FORUM_VIEW_THREAD,
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
                    // We only need to showLoading this message when the first comment is added
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
        router.showCourseDiscussionComments(getActivity(), response, discussionThread, courseData);
    }

    private static class ResponsesLoader implements
            InfiniteScrollUtils.PageLoader<DiscussionComment> {
        @NonNull
        private final Context context;
        @NonNull
        private final String threadId;
        private final boolean isQuestionTypeThread;
        private boolean hasMorePages = true;

        @Inject
        private DiscussionService discussionService;

        @Nullable
        private Call<Page<DiscussionComment>> getResponsesListCall;
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
            RoboGuice.injectMembers(context, this);
        }

        @Override
        public void loadNextPage(@NonNull final InfiniteScrollUtils.PageLoadCallback<DiscussionComment> callback) {
            if (getResponsesListCall != null) {
                getResponsesListCall.cancel();
            }
            final List<String> requestedFields = Collections.singletonList(
                    DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue());
            if (isQuestionTypeThread) {
                getResponsesListCall = discussionService.getResponsesListForQuestion(
                        threadId, nextPage, isFetchingEndorsed, requestedFields);
            } else {
                getResponsesListCall = discussionService.getResponsesList(
                        threadId, nextPage, requestedFields);
            }

            final TaskMessageCallback mCallback = context instanceof TaskMessageCallback ? (TaskMessageCallback) context : null;
            getResponsesListCall.enqueue(new ErrorHandlingCallback<Page<DiscussionComment>>(
                    context, null, mCallback, CallTrigger.LOADING_UNCACHED) {
                @Override
                protected void onResponse(
                        @NonNull final Page<DiscussionComment> threadResponsesPage) {
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

                @Override
                protected void onFailure(@NonNull final Throwable error) {
                    callback.onError();
                    nextPage = 1;
                    hasMorePages = false;
                }
            });
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
            if (getResponsesListCall != null) {
                getResponsesListCall.cancel();
                getResponsesListCall = null;
            }
            isFetchingEndorsed = isQuestionTypeThread;
            nextPage = 1;
        }

        public boolean hasMorePages() {
            return hasMorePages;
        }
    }

}
