package org.edx.mobile.view.view_holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.custom.ETextView;

public class DiscussionSocialLayoutViewHolder extends RecyclerView.ViewHolder {

    public  final IconView threadVoteIconView;
    public  final  ETextView threadVoteTextView;
    public  final  View  voteViewContainer;
    public  final  IconView threadFollowIconView;
    public  final  ETextView threadFollowTextView;
    public  final  View threadFollowContainer;

    public DiscussionSocialLayoutViewHolder(View itemView) {
        super(itemView);

        voteViewContainer = itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_container);


        threadVoteTextView = (ETextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_count_text_view);
        threadVoteIconView = (IconView) itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_icon_view);

        threadFollowTextView = (ETextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_text_view);

        threadFollowIconView = (IconView) itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_icon_view);
        threadFollowContainer = itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_relative_layout);

    }

    public void setDiscussionThread(final Context context,  final DiscussionThread discussionThread) {
        threadVoteTextView.setText(ResourceUtil.getFormattedStringForQuantity(
                context.getResources(), R.plurals.discussion_responses_action_bar_vote_text, discussionThread.getVoteCount()));
        threadVoteIconView.setIconColor(discussionThread.isVoted() ?
                context.getResources().getColor(R.color.edx_brand_primary_base) :
                context.getResources().getColor(R.color.edx_grayscale_neutral_base));


        threadFollowContainer.setVisibility(View.VISIBLE);

        if ( discussionThread.isFollowing() ){
            threadFollowTextView.setText(context.getString(R.string.forum_unfollow));
            threadFollowIconView.setIconColor( context.getResources().getColor(R.color.edx_brand_primary_base));
        } else {
            threadFollowTextView.setText( context.getString(R.string.forum_follow));
            threadFollowIconView.setIconColor(context.getResources().getColor(R.color.edx_grayscale_neutral_base));
        }


    }

    public void setDiscussionResponse(final Context context,  final DiscussionComment discussionResponse) {
        threadVoteTextView.setText(ResourceUtil.getFormattedStringForQuantity(
                context.getResources(), R.plurals.discussion_responses_action_bar_vote_text, discussionResponse.getVoteCount()));
        threadVoteIconView.setIconColor(discussionResponse.isVoted() ?
                context.getResources().getColor(R.color.edx_brand_primary_base) :
                context.getResources().getColor(R.color.edx_grayscale_neutral_base));
    }
}
