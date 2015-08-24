package org.edx.mobile.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.view.custom.ETextView;

public class ActionBarViewHolder extends RecyclerView.ViewHolder {

    RelativeLayout voteLayout;
    public IconView voteIconView;
    public ETextView voteCountTextView;

    RelativeLayout followLayout;
    public IconView followIconView;
    public ETextView followTextView;

    RelativeLayout reportLayout;
    public IconView reportIconView;
    public ETextView reportTextView;

    public ActionBarViewHolder(View itemView) {
        super(itemView);

        voteLayout = (RelativeLayout) itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_container);
        voteIconView = (IconView) itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_icon_view);
        voteCountTextView = (ETextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_vote_count_text_view);

        followLayout = (RelativeLayout) itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_relative_layout);
        followIconView = (IconView) itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_icon_view);
        followTextView = (ETextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_follow_text_view);

        reportLayout = (RelativeLayout) itemView.
                findViewById(R.id.discussion_responses_action_bar_report_layout);
        reportIconView = (IconView) itemView.
                findViewById(R.id.discussion_responses_action_bar_report_icon_view);
        reportTextView = (ETextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_report_text_view);

    }
}
