package org.edx.mobile.view.view_holders;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UiUtils;

public class DiscussionSocialLayoutViewHolder extends RecyclerView.ViewHolder {

    public final AppCompatImageView threadVoteIconImageView;
    public final TextView threadVoteTextView;
    public final View voteViewContainer;
    public final AppCompatImageView threadFollowIconImageView;
    public final TextView threadFollowTextView;
    public final View threadFollowContainer;

    public DiscussionSocialLayoutViewHolder(View itemView) {
        super(itemView);

        voteViewContainer = itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_container);

        threadVoteTextView = (TextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_count_text_view);
        threadVoteIconImageView = (AppCompatImageView) itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_icon_view);

        threadFollowTextView = (TextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_text_view);

        threadFollowIconImageView = (AppCompatImageView) itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_icon_view);
        threadFollowContainer = itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_container);
    }

    public void setDiscussionThread(final DiscussionThread discussionThread) {
        setVote(discussionThread.isVoted(), discussionThread.isVoted() ? discussionThread.getVoteCount() - 1 : discussionThread.getVoteCount());
        setFollowing(discussionThread.isFollowing());
        threadFollowContainer.setVisibility(View.VISIBLE);
    }

    public void setDiscussionResponse(final DiscussionComment discussionResponse) {
        setVote(discussionResponse.isVoted(), discussionResponse.isVoted() ? discussionResponse.getVoteCount() - 1 : discussionResponse.getVoteCount());
    }

    public boolean toggleFollow() {
        setFollowing(!threadFollowContainer.isSelected());
        return threadFollowContainer.isSelected();
    }

    public boolean toggleVote(int otherUserVotes) {
        setVote(!voteViewContainer.isSelected(), otherUserVotes);
        return voteViewContainer.isSelected();
    }

    private void setFollowing(boolean follow) {
        if (threadFollowContainer.isSelected() != follow) {
            threadFollowContainer.setSelected(follow);
            threadFollowTextView.setText(follow ? R.string.forum_unfollow : R.string.forum_follow);
            UiUtils.INSTANCE.setImageViewColor(threadFollowIconImageView.getContext(), threadFollowIconImageView, R.color.infoBase);
        }
    }

    private void setVote(boolean vote, int otherUserVotes) {
        voteViewContainer.setSelected(vote);
        threadVoteTextView.setText(ResourceUtil.getFormattedStringForQuantity(
                threadVoteTextView.getResources(), R.plurals.discussion_responses_action_bar_vote_text, vote ? otherUserVotes + 1 : otherUserVotes));
        UiUtils.INSTANCE.setImageViewColor(threadVoteIconImageView.getContext(), threadVoteIconImageView, R.color.infoBase);
    }
}
