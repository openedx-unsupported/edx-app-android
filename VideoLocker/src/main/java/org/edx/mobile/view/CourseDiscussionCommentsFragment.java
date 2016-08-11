package org.edx.mobile.view;

import android.content.Context;
import android.graphics.Rect;
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
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionService.FlagBody;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionUtils;
import org.edx.mobile.http.CallTrigger;
import org.edx.mobile.http.ErrorHandlingCallback;
import org.edx.mobile.model.Page;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.view.adapters.DiscussionCommentsAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionCommentsFragment extends BaseFragment implements DiscussionCommentsAdapter.Listener {

    @InjectView(R.id.discussion_recycler_view)
    private RecyclerView discussionCommentsListView;

    @InjectView(R.id.create_new_item_text_view)
    private TextView createNewCommentTextView;

    @InjectView(R.id.create_new_item_layout)
    private ViewGroup createNewCommentLayout;

    @Inject
    private Router router;

    @Inject
    private Context context;

    @InjectExtra(Router.EXTRA_DISCUSSION_THREAD)
    private DiscussionThread discussionThread;

    @InjectExtra(Router.EXTRA_DISCUSSION_COMMENT)
    private DiscussionComment discussionResponse;

    @Inject
    private DiscussionService discussionService;

    @Inject
    ISegment segIO;

    private DiscussionCommentsAdapter discussionCommentsAdapter;

    @Nullable
    private Call<Page<DiscussionComment>> getCommentsListCall;

    private int nextPage = 1;
    private boolean hasMorePages = true;

    private InfiniteScrollUtils.InfiniteListController controller;

    @Nullable
    private Call<DiscussionComment> setCommentFlaggedCall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_responses_or_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionCommentsAdapter = new DiscussionCommentsAdapter(getActivity(), this,
                discussionThread, discussionResponse);
        controller = InfiniteScrollUtils.configureRecyclerViewWithInfiniteList(discussionCommentsListView,
                discussionCommentsAdapter, new InfiniteScrollUtils.PageLoader<DiscussionComment>() {
            @Override
            public void loadNextPage(@NonNull InfiniteScrollUtils.PageLoadCallback<DiscussionComment> callback) {
                getCommentsList(callback);
            }
        });

        final int overlap = getResources().getDimensionPixelSize(R.dimen.edx_hairline);
        discussionCommentsListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, -overlap, 0, 0);
            }
        });
        discussionCommentsListView.setAdapter(discussionCommentsAdapter);

        DiscussionUtils.setStateOnTopicClosed(discussionThread.isClosed(),
                createNewCommentTextView, R.string.discussion_post_create_new_comment,
                R.string.discussion_add_comment_disabled_title, createNewCommentLayout,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        router.showCourseDiscussionAddComment(context, discussionResponse, discussionThread);
                    }
                });
    }

    protected void getCommentsList(@NonNull final InfiniteScrollUtils.PageLoadCallback<DiscussionComment> callback) {
        if (getCommentsListCall != null) {
            getCommentsListCall.cancel();
        }
        final List<String> requestedFields = Collections.singletonList(
                DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue());
        getCommentsListCall = discussionService.getCommentsList(
                discussionResponse.getIdentifier(), nextPage, requestedFields);
        getCommentsListCall.enqueue(new ErrorHandlingCallback<Page<DiscussionComment>>(
                getActivity(), CallTrigger.LOADING_UNCACHED, (TaskProgressCallback) null) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        Map<String, String> values = new HashMap<>();
        values.put(ISegment.Keys.TOPIC_ID, discussionThread.getTopicId());
        values.put(ISegment.Keys.THREAD_ID, discussionThread.getIdentifier());
        values.put(ISegment.Keys.RESPONSE_ID, discussionResponse.getIdentifier());
        segIO.trackScreenView(ISegment.Screens.FORUM_VIEW_RESPONSE_COMMENTS,
                discussionThread.getCourseId(), discussionThread.getTitle(), values);
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
            ((BaseFragmentActivity) getActivity()).showInfoMessage(getString(R.string.discussion_comment_posted));
            if (!hasMorePages) {
                discussionCommentsAdapter.insertCommentAtEnd(event.getComment());
                discussionCommentsListView.smoothScrollToPosition(discussionCommentsAdapter.getItemCount() - 1);
            } else {
                // We still need to update the comment count locally
                discussionCommentsAdapter.incrementCommentCount();
            }
        }
    }

    @Override
    public void reportComment(@NonNull DiscussionComment comment) {
        if (setCommentFlaggedCall != null) {
            setCommentFlaggedCall.cancel();
        }
        setCommentFlaggedCall = discussionService.setCommentFlagged(
                comment.getIdentifier(), new FlagBody(!comment.isAbuseFlagged()));
        setCommentFlaggedCall.enqueue(new ErrorHandlingCallback<DiscussionComment>(
                context, CallTrigger.LOADING_UNCACHED, (TaskProgressCallback) null) {
            @Override
            protected void onResponse(@NonNull final DiscussionComment comment) {
                discussionCommentsAdapter.updateComment(comment);
            }
        });
    }

    @Override
    public void onClickAuthor(@NonNull String username) {
        router.showUserProfile(getActivity(), username);
    }
}
