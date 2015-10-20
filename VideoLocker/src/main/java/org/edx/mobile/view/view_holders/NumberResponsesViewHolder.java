package org.edx.mobile.view.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.view.custom.ETextView;

public class NumberResponsesViewHolder extends RecyclerView.ViewHolder {
    public ETextView numberResponsesOrCommentsLabel;
    public IconView numberResponsesIconView;

    public NumberResponsesViewHolder(View itemView) {
        super(itemView);
        numberResponsesOrCommentsLabel = (ETextView) itemView.
                findViewById(R.id.number_responses_or_comments_label);
        numberResponsesIconView = (IconView) itemView.
                findViewById(R.id.number_responses_or_comments_icon_view);
    }

}
