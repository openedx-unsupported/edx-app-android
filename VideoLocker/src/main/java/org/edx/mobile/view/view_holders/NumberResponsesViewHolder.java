package org.edx.mobile.view.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.view.custom.ETextView;

public class NumberResponsesViewHolder extends RecyclerView.ViewHolder {
    public ETextView numberResponsesOrCommentsCountTextView;
    public ETextView numberResponsesOrCommentsLabel;

    public NumberResponsesViewHolder(View itemView) {
        super(itemView);

        numberResponsesOrCommentsCountTextView = (ETextView) itemView.
                findViewById(R.id.number_responses_or_comments_count_text_view);
        numberResponsesOrCommentsLabel = (ETextView) itemView.
                findViewById(R.id.number_responses_or_comments_label);
    }

}
