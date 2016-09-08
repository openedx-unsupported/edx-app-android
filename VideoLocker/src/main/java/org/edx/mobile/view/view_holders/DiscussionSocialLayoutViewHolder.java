package org.edx.mobile.view.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.util.ResourceUtil;

public class DiscussionSocialLayoutViewHolder extends RecyclerView.ViewHolder {

    public final IconImageView threadVoteIconImageView;
    public final TextView threadVoteTextView;
    public final View voteViewContainer;
    public final IconImageView threadFollowIconImageView;
    public final TextView threadFollowTextView;
    public final View threadFollowContainer;

    public DiscussionSocialLayoutViewHolder(View itemView) {
        super(itemView);

        voteViewContainer = itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_container);

        threadVoteTextView = (TextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_count_text_view);
        threadVoteIconImageView = (IconImageView) itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_icon_view);

        threadFollowTextView = (TextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_text_view);

        threadFollowIconImageView = (IconImageView) itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_icon_view);
        threadFollowContainer = itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_container);
    }

    public void setDiscussionThread(final DiscussionThread discussionThread) {
        threadVoteTextView.setText(ResourceUtil.getFormattedStringForQuantity(
                threadVoteTextView.getResources(), R.plurals.discussion_responses_action_bar_vote_text, discussionThread.getVoteCount()));
        threadVoteIconImageView.setIconColorResource(discussionThread.isVoted() ?
                R.color.edx_brand_primary_base : R.color.edx_brand_gray_base);

        threadFollowContainer.setVisibility(View.VISIBLE);

        if (discussionThread.isFollowing()) {
            threadFollowTextView.setText(R.string.forum_unfollow);
            threadFollowIconImageView.setIconColorResource(R.color.edx_brand_primary_base);
        } else {
            threadFollowTextView.setText(R.string.forum_follow);
            threadFollowIconImageView.setIconColorResource(R.color.edx_brand_gray_base);
        }
    }

    public void setDiscussionResponse(final DiscussionComment discussionResponse) {
        threadVoteTextView.setText(ResourceUtil.getFormattedStringForQuantity(
                threadVoteTextView.getResources(), R.plurals.discussion_responses_action_bar_vote_text, discussionResponse.getVoteCount()));
        threadVoteIconImageView.setIconColorResource(discussionResponse.isVoted() ?
                R.color.edx_brand_primary_base : R.color.edx_brand_gray_base);
    }
}
