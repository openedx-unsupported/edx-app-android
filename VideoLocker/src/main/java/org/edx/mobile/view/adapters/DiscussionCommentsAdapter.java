package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionTextUtils;

public class DiscussionCommentsAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final Listener listener;
    private DiscussionComment response;

    public interface Listener {
        void reportComment(@NonNull DiscussionComment comment);

        void onClickAuthor(@NonNull String username);
    }

    public DiscussionCommentsAdapter(Context context, Listener listener, DiscussionComment response) {
        this.context = context;
        this.listener = listener;
        this.response = response;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.row_discussion_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final ViewHolder holder = (ViewHolder) viewHolder;
        final DiscussionComment discussionComment;
        final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.discussionCommentRow.getLayoutParams();
        layoutParams.topMargin = layoutParams.bottomMargin = 0;
        @DrawableRes
        final int backgroundRes;
        final IconDrawable iconDrawable;
        if (position == 0) {
            discussionComment = response;
            backgroundRes = R.drawable.row_discussion_first_comment_background;
            layoutParams.topMargin = context.getResources().getDimensionPixelOffset(R.dimen.discussion_responses_standard_margin);
            final int childrenSize = discussionComment.getChildren().size();
            holder.discussionCommentCountReportTextView.setText(context.getResources().
                    getQuantityString(R.plurals.number_responses_or_comments_comments_label, childrenSize, childrenSize));
            iconDrawable = new IconDrawable(context, FontAwesomeIcons.fa_comment)
                    .sizeRes(context, R.dimen.edx_xxx_small)
                    .colorRes(context, R.color.edx_grayscale_neutral_base);
            holder.discussionCommentCountReportTextView.setOnClickListener(null);
            holder.discussionCommentCountReportTextView.setClickable(false);

        } else {
            final int extraSidePadding = context.getResources().getDimensionPixelOffset(R.dimen.discussion_responses_standard_margin);
            holder.discussionCommentRow.setPadding(extraSidePadding, 0, extraSidePadding, 0);

            discussionComment = response.getChildren().get(position - 1);
            if (position == getItemCount() - 1) {
                backgroundRes = R.drawable.row_discussion_last_comment_background;
                layoutParams.bottomMargin = context.getResources().getDimensionPixelOffset(R.dimen.discussion_responses_standard_margin);
            } else {
                backgroundRes = R.drawable.row_discussion_comment_background;
            }

            iconDrawable = new IconDrawable(context, FontAwesomeIcons.fa_flag)
                    .sizeRes(context, R.dimen.edx_xxx_small)
                    .colorRes(context, discussionComment.isAbuseFlagged() ? R.color.edx_brand_primary_base : R.color.edx_grayscale_neutral_dark);
            holder.discussionCommentCountReportTextView.setText(discussionComment.isAbuseFlagged() ? context.getString(R.string.discussion_responses_reported_label) : context.getString(R.string.discussion_responses_report_label));
            holder.discussionCommentCountReportTextView.setTextColor(context.getResources().getColor(R.color.edx_grayscale_neutral_dark));

            holder.discussionCommentCountReportTextView.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    listener.reportComment(discussionComment);
                }
            });
        }
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.discussionCommentCountReportTextView, iconDrawable, null, null, null);
        holder.discussionCommentRow.setLayoutParams(layoutParams);
        holder.discussionCommentRow.setBackgroundResource(backgroundRes);

        String commentBody = discussionComment.getRawBody();
        holder.discussionCommentBody.setText(commentBody);

        DiscussionTextUtils.setAuthorAttributionText(holder.discussionCommentAuthorTextView, discussionComment, new Runnable() {
            @Override
            public void run() {
                listener.onClickAuthor(discussionComment.getAuthor());
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1 + response.getChildren().size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        View discussionCommentRow;
        TextView discussionCommentBody;
        TextView discussionCommentAuthorTextView;
        TextView discussionCommentCountReportTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            discussionCommentRow = itemView.findViewById(R.id.row_discussion_comment_layout);
            discussionCommentBody = (TextView) itemView.findViewById(R.id.discussion_comment_body);
            discussionCommentAuthorTextView = (TextView) itemView.findViewById(R.id.discussion_comment_author_text_view);
            discussionCommentCountReportTextView = (TextView) itemView.findViewById(R.id.discussion_comment_count_report_text_view);
        }
    }

    public void insertCommentAtEnd(DiscussionComment comment) {
        response.getChildren().add(comment);
        notifyItemInserted(response.getChildren().size());
        notifyItemChanged(response.getChildren().size() - 1); // Last item's background is different, so must be refreshed as well
    }

    public void updateComment(DiscussionComment comment) {
        for (int i = 0; i < response.getChildren().size(); ++i) {
            if (response.getChildren().get(i).getIdentifier().equals(comment.getIdentifier())) {
                response.getChildren().set(i, comment);
                notifyItemChanged(i + 1);
                break;
            }
        }
    }
}