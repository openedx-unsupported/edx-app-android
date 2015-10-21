package org.edx.mobile.view;

import android.content.Context;
import android.graphics.Rect;
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
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionUtils;
import org.edx.mobile.task.FlagCommentTask;
import org.edx.mobile.view.adapters.DiscussionCommentsAdapter;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionCommentsFragment extends RoboFragment implements DiscussionCommentsAdapter.Listener {

    @InjectView(R.id.discussion_comments_listview)
    private RecyclerView discussionCommentsListView;

    @InjectView(R.id.create_new_item_text_view)
    private TextView createNewCommentTextView;

    @InjectView(R.id.create_new_item_layout)
    private ViewGroup createNewCommentLayout;

    @Inject
    private Router router;

    @Inject
    private Context context;

    @InjectExtra(Router.EXTRA_DISCUSSION_TOPIC_CLOSED)
    private boolean isTopicClosed;

    @InjectExtra(Router.EXTRA_DISCUSSION_COMMENT)
    private DiscussionComment discussionComment;
    private DiscussionCommentsAdapter discussionCommentsAdapter;

    @Nullable
    private FlagCommentTask flagCommentTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionCommentsAdapter = new DiscussionCommentsAdapter(getActivity(), this, discussionComment);

        discussionCommentsListView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        final int overlap = getResources().getDimensionPixelSize(R.dimen.edx_hairline);
        discussionCommentsListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, -overlap, 0, 0);
            }
        });
        discussionCommentsListView.setAdapter(discussionCommentsAdapter);

        DiscussionUtils.setStateOnTopicClosed(isTopicClosed,
                createNewCommentTextView, R.string.discussion_post_create_new_comment,
                R.string.discussion_add_comment_disabled_title, createNewCommentLayout,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        router.showCourseDiscussionAddComment(context, discussionComment);
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
        if (null != event.getParent() && event.getParent().getIdentifier().equalsIgnoreCase(discussionComment.getIdentifier())) {
            discussionCommentsAdapter.insertCommentAtEnd(event.getComment());
        }
    }

    @Override
    public void reportComment(@NonNull DiscussionComment comment) {
        if (flagCommentTask != null) {
            flagCommentTask.cancel(true);
        }
        flagCommentTask = new FlagCommentTask(context, comment, !comment.isAbuseFlagged()) {
            @Override
            public void onSuccess(DiscussionComment comment) {
                discussionCommentsAdapter.updateComment(comment);
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
            }
        };
        flagCommentTask.execute();
    }

    @Override
    public void onClickAuthor(@NonNull String username) {
        router.showUserProfile(getActivity(), username);
    }
}