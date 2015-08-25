package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.discussion.DiscussionAPI;
import org.edx.mobile.discussion.DiscussionComment;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.adapters.DiscussionCommentsAdapter;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseDiscussionCommentsFragment extends RoboFragment {

    @InjectView(R.id.discussion_comments_listview)
    ListView discussionCommentsListView;

    @InjectView(R.id.create_new_item_text_view)
    TextView createNewCommentTextView;

    @InjectView(R.id.create_new_item_relative_layout)
    RelativeLayout createNewCommentRelativeLayout;

    @InjectExtra(Router.EXTRA_DISCUSSION_COMMENT)
    private DiscussionComment discussionComment;

    @Inject
    DiscussionAPI discussionAPI;

    @Inject
    DiscussionCommentsAdapter discussionCommentsAdapter;

    @Inject
    Router router;

    @Inject
    Context context;

    private static final Logger logger = new Logger(CourseDiscussionCommentsFragment.class.getName());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discussionCommentsListView.setAdapter(discussionCommentsAdapter);
        discussionCommentsAdapter.setItems(discussionComment.getChildren());

        createNewCommentTextView.setText(context.getString(R.string.discussion_post_create_new_comment));

        createNewCommentRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showCourseDiscussionAddResponseOrComment(context, null, discussionComment);
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

    public void onEventMainThread(DiscussionCommentPostedEvent event) {
        if (null != event.getParent() && event.getParent().getIdentifier().equalsIgnoreCase(discussionComment.getIdentifier())) {
            discussionCommentsAdapter.add(event.getComment());
            discussionCommentsAdapter.notifyDataSetChanged();
        }
    }
}