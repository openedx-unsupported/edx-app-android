package org.edx.mobile.view.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.view.custom.ETextView;

public class NumberResponsesViewHolder extends RecyclerView.ViewHolder {
    public ETextView numberResponsesOrCommentsLabel;
    public IconImageView numberResponsesIconImageView;

    public NumberResponsesViewHolder(View itemView) {
        super(itemView);
        numberResponsesOrCommentsLabel = (ETextView) itemView.
                findViewById(R.id.number_responses_or_comments_label);
        numberResponsesIconImageView = (IconImageView) itemView.
                findViewById(R.id.number_responses_or_comments_icon_view);
    }

}
