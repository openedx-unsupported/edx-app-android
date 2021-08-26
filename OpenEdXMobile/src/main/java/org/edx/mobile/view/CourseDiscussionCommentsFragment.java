package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.databinding.FragmentDiscussionResponsesOrCommentsBinding;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionService.FlagBody;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionUtils;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.DialogErrorNotification;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.view.adapters.DiscussionCommentsAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;
import org.edx.mobile.view.common.TaskMessageCallback;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit2.Call;

public class CourseDiscussionCommentsFragment extends BaseFragment implements DiscussionCommentsAdapter.Listener {

    @Inject
    private Router router;

    @Inject
    private Context context;

    @Inject
    private DiscussionService discussionService;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    private DiscussionThread discussionThread;
    private DiscussionComment discussionResponse;
    private DiscussionCommentsAdapter discussionCommentsAdapter;

    @Nullable
    private Call<Page<DiscussionComment>> getCommentsListCall;

    private int nextPage = 1;
    private boolean hasMorePages = true;

    private FragmentDiscussionResponsesOrCommentsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        parseExtras();

        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.TOPIC_ID, discussionThread.getTopicId());
        values.put(Analytics.Keys.THREAD_ID, discussionThread.getIdentifier());
        values.put(Analytics.Keys.RESPONSE_ID, discussionResponse.getIdentifier());
        if (!discussionResponse.isAuthorAnonymous()) {
            values.put(Analytics.Keys.AUTHOR, discussionResponse.getAuthor());
        }
        analyticsRegistry.trackScreenView(Analytics.Screens.FORUM_VIEW_RESPONSE_COMMENTS,
                discussionThread.getCourseId(), discussionThread.getTitle(), values);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiscussionResponsesOrCommentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionCommentsAdapter = new DiscussionCommentsAdapter(requireActivity(), this,
                discussionThread, discussionResponse);
        InfiniteScrollUtils.configureRecyclerViewWithInfiniteList(binding.discussionRecyclerView,
                discussionCommentsAdapter, new InfiniteScrollUtils.PageLoader<DiscussionComment>() {
                    @Override
                    public void loadNextPage(@NonNull InfiniteScrollUtils.PageLoadCallback<DiscussionComment> callback) {
                        getCommentsList(callback);
                    }
                });

        final int overlap = getResources().getDimensionPixelSize(R.dimen.edx_hairline);
        binding.discussionRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, -overlap, 0, 0);
            }
        });
        binding.discussionRecyclerView.setAdapter(discussionCommentsAdapter);

        DiscussionUtils.setStateOnTopicClosed(discussionThread.isClosed(),
                binding.createNewItem.createNewItemTextView, R.string.discussion_post_create_new_comment,
                R.string.discussion_add_comment_disabled_title, binding.createNewItem.createNewItemLayout,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        router.showCourseDiscussionAddComment(context, discussionResponse, discussionThread);
                    }
                });

        final EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getArguments().
                getSerializable(Router.EXTRA_COURSE_DATA);
        binding.createNewItem.createNewItemLayout.setEnabled(!courseData.isDiscussionBlackedOut());
    }

    private void parseExtras() {
        final Bundle bundle = getArguments();
        discussionThread = (DiscussionThread) bundle.getSerializable(Router.EXTRA_DISCUSSION_THREAD);
        discussionResponse = (DiscussionComment) bundle.getSerializable(Router.EXTRA_DISCUSSION_COMMENT);
    }

    protected void getCommentsList(@NonNull final InfiniteScrollUtils.PageLoadCallback<DiscussionComment> callback) {
        if (getCommentsListCall != null) {
            getCommentsListCall.cancel();
        }
        final List<String> requestedFields = Collections.singletonList(
                DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue());
        getCommentsListCall = discussionService.getCommentsList(
                discussionResponse.getIdentifier(), nextPage, requestedFields);
        final Activity activity = requireActivity();
        final TaskMessageCallback mCallback = activity instanceof TaskMessageCallback ? (TaskMessageCallback) activity : null;
        getCommentsListCall.enqueue(new ErrorHandlingCallback<Page<DiscussionComment>>(activity,
                null, mCallback, CallTrigger.LOADING_UNCACHED) {
            @Override
            protected void onResponse(@NonNull final Page<DiscussionComment> threadCommentsPage) {
                ++nextPage;
                callback.onPageLoaded(threadCommentsPage);
                discussionCommentsAdapter.notifyDataSetChanged();
                hasMorePages = threadCommentsPage.hasNext();
            }

            @Override
            protected void onFailure(@NonNull final Throwable error) {
                callback.onError();
                nextPage = 1;
                hasMorePages = false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getCommentsListCall != null) {
            getCommentsListCall.cancel();
        }
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DiscussionCommentPostedEvent event) {
        if (null != event.getParent() && event.getParent().getIdentifier().equals(discussionResponse.getIdentifier())) {
            ((BaseFragmentActivity) requireActivity()).showInfoMessage(getString(R.string.discussion_comment_posted));
            if (!hasMorePages) {
                discussionCommentsAdapter.insertCommentAtEnd(event.getComment());
                binding.discussionRecyclerView.smoothScrollToPosition(discussionCommentsAdapter.getItemCount() - 1);
            } else {
                // We still need to update the comment count locally
                discussionCommentsAdapter.incrementCommentCount();
            }
        }
    }

    @Override
    public void reportComment(@NonNull DiscussionComment comment) {
        final Call<DiscussionComment> setCommentFlaggedCall = discussionService.setCommentFlagged(
                comment.getIdentifier(), new FlagBody(!comment.isAbuseFlagged()));
        setCommentFlaggedCall.enqueue(new ErrorHandlingCallback<DiscussionComment>(
                context, null, new DialogErrorNotification(this)) {
            @Override
            protected void onResponse(@NonNull final DiscussionComment comment) {
                discussionCommentsAdapter.updateComment(comment);
            }
        });
    }

    @Override
    public void onClickAuthor(@NonNull String username) {
        router.showUserProfile(requireActivity(), username);
    }
}
