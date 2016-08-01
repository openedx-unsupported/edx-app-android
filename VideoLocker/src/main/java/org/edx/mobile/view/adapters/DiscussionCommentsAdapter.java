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
import org.edx.mobile.discussion.DiscussionThread;

import java.util.ArrayList;
import java.util.List;

public class DiscussionCommentsAdapter extends RecyclerView.Adapter implements InfiniteScrollUtils.ListContentController<DiscussionComment> {

    @NonNull
    private final Context context;

    @NonNull
    private final Listener listener;

    @NonNull
    private DiscussionComment response;

    @NonNull
    private DiscussionThread thread;
    // Record the current time at initialization to keep the display of the elapsed time durations stable.
    private long initialTimeStampMs = System.currentTimeMillis();

    private final List<DiscussionComment> discussionComments = new ArrayList<>();

    private boolean progressVisible = false;

    static class RowType {
        static final int COMMENT = 1;
        static final int PROGRESS = 2;
    }

    public interface Listener {
        void reportComment(@NonNull DiscussionComment comment);

        void onClickAuthor(@NonNull String username);
    }

    public DiscussionCommentsAdapter(@NonNull Context context, @NonNull Listener listener,
                                     @NonNull DiscussionThread thread,
                                     @NonNull DiscussionComment response) {
        this.context = context;
        this.listener = listener;
        this.thread = thread;
        this.response = response;
    }

    @Override
    public void setProgressVisible(boolean visible) {
        if (progressVisible != visible) {
            progressVisible = visible;
            int progressRowIndex = 1 + discussionComments.size();
            if (visible) {
                notifyItemInserted(progressRowIndex);
            } else {
                notifyItemRemoved(progressRowIndex);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RowType.PROGRESS) {
            return new RecyclerView.ViewHolder(LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.list_view_footer_progress, parent, false)) {};
        }

        return new ViewHolder(LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.row_discussion_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (!(viewHolder instanceof ViewHolder)) return;
        final ViewHolder holder = (ViewHolder) viewHolder;
        final DiscussionComment discussionComment;
        final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.discussionCommentRow.getLayoutParams();
        layoutParams.topMargin = layoutParams.bottomMargin = 0;
        @DrawableRes
        final int backgroundRes;
        final IconDrawable iconDrawable;
        final int standardMargin = context.getResources().getDimensionPixelOffset(R.dimen.discussion_responses_standard_margin);
        if (position == 0) {
            holder.discussionCommentRow.setPadding(0, standardMargin, 0, 0);
            discussionComment = response;
            DiscussionTextUtils.setEndorsedState(holder.responseAnswerTextView, thread, response);
            backgroundRes = R.drawable.row_discussion_first_comment_background;
            layoutParams.topMargin = context.getResources().getDimensionPixelOffset(R.dimen.discussion_responses_standard_margin);
            final int childCount = discussionComment.getChildCount();
            holder.discussionCommentCountReportTextView.setText(context.getResources().
                    getQuantityString(R.plurals.number_responses_or_comments_comments_label, childCount, childCount));
            iconDrawable = new IconDrawable(context, FontAwesomeIcons.fa_comment)
                    .sizeRes(context, R.dimen.edx_xxx_small)
                    .colorRes(context, R.color.edx_grayscale_neutral_base);
            holder.discussionCommentCountReportTextView.setOnClickListener(null);
            holder.discussionCommentCountReportTextView.setClickable(false);

        } else {
            holder.responseAnswerTextView.setVisibility(View.GONE);
            holder.discussionCommentRow.setPadding(standardMargin, standardMargin, standardMargin, 0);

            discussionComment = discussionComments.get(position - 1);
            if (!progressVisible && position == getItemCount() - 1) {
                backgroundRes = R.drawable.row_discussion_last_comment_background;
                layoutParams.bottomMargin = standardMargin;
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

        DiscussionTextUtils.setAuthorAttributionText(holder.discussionCommentAuthorTextView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                discussionComment, initialTimeStampMs,
                new Runnable() {
                    @Override
                    public void run() {
                        listener.onClickAuthor(discussionComment.getAuthor());
                    }
                });
    }

    @Override
    public int getItemCount() {
        int total = 1 + discussionComments.size();
        if (progressVisible)
            total++;
        return total;
    }

    @Override
    public int getItemViewType(int position) {
        if (progressVisible && position == getItemCount() - 1) {
            return RowType.PROGRESS;
        }

        return RowType.COMMENT;
    }

    @Override
    public void clear() {
        int commentsCount = discussionComments.size();
        discussionComments.clear();
        notifyItemRangeRemoved(1, commentsCount);
    }

    @Override
    public void addAll(List<DiscussionComment> items) {
        int lastCommentIndex = discussionComments.size();
        discussionComments.addAll(items);
        notifyItemRangeInserted(lastCommentIndex + 1, items.size());
        notifyItemChanged(lastCommentIndex); // Last item's background is different, so must be refreshed as well
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        View discussionCommentRow;
        TextView discussionCommentBody;
        TextView discussionCommentAuthorTextView;
        TextView discussionCommentCountReportTextView;
        TextView responseAnswerTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            discussionCommentRow = itemView.findViewById(R.id.row_discussion_comment_layout);
            discussionCommentBody = (TextView) itemView.findViewById(R.id.discussion_comment_body);
            discussionCommentAuthorTextView = (TextView) itemView.findViewById(R.id.discussion_comment_author_text_view);
            discussionCommentCountReportTextView = (TextView) itemView.findViewById(R.id.discussion_comment_count_report_text_view);
            responseAnswerTextView = (TextView) itemView.findViewById(R.id.discussion_responses_answer_text_view);
        }
    }

    public void insertCommentAtEnd(DiscussionComment comment) {
        // Since, we have a added a new comment we need to update timestamps of all comments
        initialTimeStampMs = System.currentTimeMillis();
        discussionComments.add(comment);
        incrementCommentCount();
        notifyDataSetChanged();
    }

    public void incrementCommentCount() {
        response.incrementChildCount();
        notifyItemChanged(0);
    }

    public void updateComment(DiscussionComment comment) {
        for (int i = 0; i < discussionComments.size(); ++i) {
            if (discussionComments.get(i).getIdentifier().equals(comment.getIdentifier())) {
                discussionComments.set(i, comment);
                notifyItemChanged(1 + i);
                break;
            }
        }
    }
}