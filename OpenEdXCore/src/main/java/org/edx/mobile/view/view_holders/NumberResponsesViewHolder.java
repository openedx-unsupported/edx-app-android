package org.edx.mobile.view.view_holders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;

public class NumberResponsesViewHolder extends RecyclerView.ViewHolder {
    public TextView numberResponsesOrCommentsLabel;

    public NumberResponsesViewHolder(View itemView) {
        super(itemView);
        numberResponsesOrCommentsLabel = (TextView) itemView.
                findViewById(R.id.number_responses_or_comments_label);
        Context context = numberResponsesOrCommentsLabel.getContext();
        Drawable iconDrawable = new IconDrawable(context, FontAwesomeIcons.fa_comment)
                .colorRes(context, R.color.edx_grayscale_neutral_dark)
                .sizeRes(context, R.dimen.edx_small);
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                numberResponsesOrCommentsLabel, iconDrawable, null, null, null);
    }

}
