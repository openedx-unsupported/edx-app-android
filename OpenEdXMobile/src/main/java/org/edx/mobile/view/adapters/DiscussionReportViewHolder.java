package org.edx.mobile.view.adapters;

import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import org.edx.mobile.R;

public class DiscussionReportViewHolder {

    ViewGroup reportLayout;
    private AppCompatImageView reportIconImageView;
    private TextView reportTextView;

    public DiscussionReportViewHolder(View itemView) {
        reportLayout = (ViewGroup) itemView.
                findViewById(R.id.discussion_responses_action_bar_report_container);
        reportIconImageView = (AppCompatImageView) itemView.
                findViewById(R.id.discussion_responses_action_bar_report_icon_view);
        reportTextView = (TextView) itemView.
                findViewById(R.id.discussion_responses_action_bar_report_text_view);
    }


    public void setReported(boolean isReported) {
        reportLayout.setSelected(isReported);
        int reportStringResId = isReported ? R.string.discussion_responses_reported_label :
                R.string.discussion_responses_report_label;
        reportTextView.setText(reportTextView.getResources().getString(reportStringResId));
        reportIconImageView.setColorFilter(ContextCompat.getColor(reportIconImageView.getContext(), R.color.infoBase), PorterDuff.Mode.SRC_IN);
    }

    public boolean toggleReported() {
        setReported(!reportLayout.isSelected());
        return reportLayout.isSelected();
    }
}
