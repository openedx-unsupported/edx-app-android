package org.edx.mobile.view.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;

public class NumberResponsesViewHolder extends RecyclerView.ViewHolder {
    public TextView numberResponsesOrCommentsLabel;
    public IconImageView numberResponsesIconImageView;

    public NumberResponsesViewHolder(View itemView) {
        super(itemView);
        numberResponsesOrCommentsLabel = (TextView) itemView.
                findViewById(R.id.number_responses_or_comments_label);
        numberResponsesIconImageView = (IconImageView) itemView.
                findViewById(R.id.number_responses_or_comments_icon_view);
    }

}
