package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;
import org.edx.mobile.discussion.DiscussionUtils;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.view.adapters.CourseDiscussionResponsesAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;
import org.edx.mobile.view.common.TaskMessageCallback;
import org.edx.mobile.view.common.TaskProgressCallback;

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

    @InjectExtra(value = Router.EXTRA_DISCUSSION_THREAD, optional = true)
    private DiscussionThread discussionThread;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_THREAD_ID, optional = true)
    private String threadId;

    @InjectExtra(value = Router.EXTRA_COURSE_DATA, optional = true)
    private EnrolledCoursesResponse courseData;

    private CourseDiscussionResponsesAdapter courseDiscussionResponsesAdapter;

    @Inject
    private DiscussionService discussionService;

    @Inject
    private Router router;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @InjectView(R.id.loading_indicator)
    private ProgressBar loadingIndicator;

    private FullScreenErrorNotification errorNotification;

    @Nullable
    private Call<DiscussionThread> getAndReadThreadCall;

    private InfiniteScrollUtils.InfiniteListController controller;

    private ResponsesLoader responsesLoader;
    private Call<DiscussionThread> getThreadCall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_responses_or_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errorNotification = new FullScreenErrorNotification(view.findViewById(R.id.ll_content));
        if (discussionThread == null) {
            if (getThreadCall != null) {
                getThreadCall.cancel();
            }
            getThreadCall = discussionService.getThread(threadId);
            getThreadCall.enqueue(new ErrorHandlingCallback<DiscussionThread>(getActivity(),
                    new TaskProgressCallback.ProgressViewController(loadingIndicator), errorNotification) {
                @Override
                protected void onResponse(@NonNull DiscussionThread responseBody) {
                    discussionThread = responseBody;
                    setScreenTitle();
                    loadThreadResponses();
                }
            });
        } else {
            setScreenTitle();
            loadThreadResponses();
        }
    }

    private void loadThreadResponses() {
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
                discussionThread.getIdentifier(), new DiscussionService.ReadBody(true));
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
        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.TOPIC_ID, discussionThread.getTopicId());
        values.put(Analytics.Keys.THREAD_ID, discussionThread.getIdentifier());
        if (!discussionThread.isAuthorAnonymous()) {
            values.put(Analytics.Keys.AUTHOR, discussionThread.getAuthor());
        }
        analyticsRegistry.trackScreenView(Analytics.Screens.FORUM_VIEW_THREAD,
                courseData.getCourse().getId(), discussionThread.getTitle(), values);
    }

    private void setScreenTitle() {
        switch (discussionThread.getType()) {
            case DISCUSSION:
                getActivity().setTitle(R.string.discussion_title);
                break;
            case QUESTION:
                getActivity().setTitle(discussionThread.isHasEndorsed() ?
                        R.string.course_discussion_answered_title :
                        R.string.course_discussion_unanswered_title);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getThreadCall != null) {
            getThreadCall.cancel();
        }
        if (responsesLoader != null) {
            responsesLoader.reset();
        }
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
