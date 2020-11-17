package org.edx.mobile.view.adapters;

import android.content.Context;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.view_holders.AuthorLayoutViewHolder;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

public class DiscussionCommentsAdapter extends RecyclerView.Adapter implements InfiniteScrollUtils.ListContentController<DiscussionComment> {

    @Inject
    private Config config;

    @Inject
    private LoginPrefs loginPrefs;

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
        static final int RESPONSE = 0;
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
        RoboGuice.getInjector(context).injectMembers(this);
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
                    inflate(R.layout.list_view_footer_progress, parent, false)) {
            };
        }

        @LayoutRes
        final int layout;
        if (viewType == RowType.RESPONSE) {
            layout = R.layout.row_discussion_comments_response;
        } else {
            layout = R.layout.row_discussion_comments_comment;
        }
        return new ResponseOrCommentViewHolder(LayoutInflater.
                from(parent.getContext()).
                inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == RowType.PROGRESS) return;
        final ResponseOrCommentViewHolder holder = (ResponseOrCommentViewHolder) viewHolder;
        final DiscussionComment discussionComment;
        final IconDrawable iconDrawable;

        if (position == 0) {
            discussionComment = response;

            DiscussionTextUtils.setEndorsedState(holder.authorLayoutViewHolder.answerTextView, thread, response);
            final int childCount = discussionComment.getChildCount();
            holder.discussionCommentCountReportTextView.setText(context.getResources().
                    getQuantityString(R.plurals.number_responses_or_comments_comments_label, childCount, childCount));
            iconDrawable = new IconDrawable(context, FontAwesomeIcons.fa_comment)
                    .sizeRes(context, R.dimen.edx_small)
                    .colorRes(context, R.color.primaryBaseColor);
            holder.discussionCommentCountReportTextView.setOnClickListener(null);
            holder.discussionCommentCountReportTextView.setClickable(false);
        } else {
            holder.authorLayoutViewHolder.answerTextView.setVisibility(View.GONE);
            discussionComment = discussionComments.get(position - 1);

            iconDrawable = new IconDrawable(context, FontAwesomeIcons.fa_flag)
                    .sizeRes(context, R.dimen.edx_small)
                    .colorRes(context, R.color.infoBase);

            if (TextUtils.equals(loginPrefs.getUsername(), discussionComment.getAuthor())) {
                holder.discussionCommentCountReportTextView.setVisibility(View.GONE);
            } else {
                holder.discussionCommentCountReportTextView.setVisibility(View.VISIBLE);
                holder.discussionCommentCountReportTextView.setText(discussionComment.isAbuseFlagged() ? context.getString(R.string.discussion_responses_reported_label) : context.getString(R.string.discussion_responses_report_label));
                holder.discussionCommentCountReportTextView.setTextColor(context.getResources().getColor(R.color.infoBase));

                holder.discussionCommentCountReportTextView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(final View v) {
                        listener.reportComment(discussionComment);
                    }
                });
            }
        }

        holder.authorLayoutViewHolder.populateViewHolder(config, discussionComment,
                discussionComment, initialTimeStampMs,
                new Runnable() {
                    @Override
                    public void run() {
                        listener.onClickAuthor(discussionComment.getAuthor());
                    }
                });

        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                holder.discussionCommentCountReportTextView, iconDrawable, null, null, null);

        DiscussionTextUtils.renderHtml(holder.discussionCommentBody, discussionComment.getRenderedBody());
    }

    @Override
    public int getItemCount() {
        int total = 1 + discussionComments.size();
        if (progressVisible) {
            total++;
        }
        return total;
    }

    @Override
    public int getItemViewType(int position) {
        if (progressVisible && position == getItemCount() - 1) {
            return RowType.PROGRESS;
        }

        if (position == 0) {
            return RowType.RESPONSE;
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

    private static class ResponseOrCommentViewHolder extends RecyclerView.ViewHolder {
        public final View discussionCommentRow;
        public final TextView discussionCommentBody;
        public final TextView discussionCommentCountReportTextView;
        public final AuthorLayoutViewHolder authorLayoutViewHolder;

        public ResponseOrCommentViewHolder(View itemView) {
            super(itemView);
            discussionCommentRow = itemView.findViewById(R.id.row_discussion_comment_layout);
            discussionCommentBody = (TextView) itemView.findViewById(R.id.discussion_comment_body);
            discussionCommentCountReportTextView = (TextView) itemView.findViewById(R.id.discussion_comment_count_report_text_view);
            authorLayoutViewHolder = new AuthorLayoutViewHolder(itemView.findViewById(R.id.discussion_user_profile_row));
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
