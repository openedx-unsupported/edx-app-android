package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionComment;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.view_holders.AuthorLayoutViewHolder;
import org.edx.mobile.view.view_holders.NumberResponsesViewHolder;

import java.util.ArrayList;
import java.util.List;

public class CourseDiscussionResponsesAdapter extends RecyclerView.Adapter {

    @Inject
    Context context;

    private static final int ROW_POSITION_THREAD = 0;

    private DiscussionThread discussionThread;
    private List<DiscussionComment> discussionResponses = new ArrayList<>();

    static class RowType {
        static final int THREAD = 0;
        static final int RESPONSE = 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RowType.THREAD) {
            View discussionThreadRow = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.discussion_responses_thread_row, parent, false);

            return new DiscussionThreadViewHolder(discussionThreadRow);
        }

        View discussionResponseRow = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.discussion_responses_response_row, parent, false);

        return new DiscussionResponseViewHolder(discussionResponseRow);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == ROW_POSITION_THREAD) {
            bindViewHolderToThreadRow((DiscussionThreadViewHolder) holder);
        } else {
            bindViewHolderToResponseRow((DiscussionResponseViewHolder) holder, position);
        }
    }

    private void bindViewHolderToThreadRow(DiscussionThreadViewHolder holder) {
        holder.threadTitleTextView.setText(discussionThread.getTitle());
        holder.threadBodyTextView.setText(discussionThread.getRawBody());

        if (discussionThread.isPinned()) {
            holder.threadPinnedIconView.setVisibility(View.VISIBLE);
        }

        bindAuthorView(holder.authorLayoutViewHolder);
        bindNumberResponsesView(holder.numberResponsesViewHolder);
    }

    private void bindAuthorView(AuthorLayoutViewHolder holder) {
        holder.discussionAuthorTextView.setText(discussionThread.getAuthor());
        holder.discussionAuthorCreatedAtTextView.setText(
                DateUtil.formatPastDateRelativeToCurrentDate(discussionThread.getCreatedAt()));

        String priviledgedAuthorText = "";
        if (discussionThread.getAuthorLabel() == DiscussionThread.PinnedAuthor.STAFF) {
            priviledgedAuthorText = context.getString(R.string.discussion_priviledged_author_label_staff);

        } else if (discussionThread.getAuthorLabel() == DiscussionThread.PinnedAuthor.COMMUNITY_TA) {
            priviledgedAuthorText = context.getString(R.string.discussion_priviledged_author_label_ta);
        }

        holder.discussionAuthorPrivilegedAuthorTextView.setText(priviledgedAuthorText);
    }

    private void bindNumberResponsesView(NumberResponsesViewHolder holder) {
        holder.numberResponsesOrCommentsCountTextView.setText(Integer.toString(discussionThread.getCommentCount()));
        holder.numberResponsesOrCommentsLabel.setText(
                context.getString(R.string.number_responses_or_comments_responses_label));
    }

    private void bindViewHolderToResponseRow(DiscussionResponseViewHolder holder, int position) {
        DiscussionComment response = discussionResponses.get(position - 1);
        DiscussionResponseViewHolder responseViewHolder = (DiscussionResponseViewHolder) holder;
        responseViewHolder.responseCommentTextView.setText(response.getRawBody());
    }

    @Override
    public int getItemCount() {
        return (discussionThread == null) ? 0 : 1 + discussionResponses.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return RowType.THREAD;
        }

        return RowType.RESPONSE;
    }


    public void setDiscussionThread(DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
        notifyDataSetChanged();
    }

    public void setDiscussionResponses(List<DiscussionComment> discussionResponses) {
        this.discussionResponses = discussionResponses;
        notifyDataSetChanged();
    }

    public static class DiscussionThreadViewHolder extends RecyclerView.ViewHolder {
        ETextView threadTitleTextView;
        ETextView threadBodyTextView;
        IconView threadPinnedIconView;

        AuthorLayoutViewHolder authorLayoutViewHolder;
        NumberResponsesViewHolder numberResponsesViewHolder;

        public DiscussionThreadViewHolder(View itemView) {
            super(itemView);

            threadTitleTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_title_text_view);
            threadBodyTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_body_text_view);
            threadPinnedIconView = (IconView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_pinned_icon_view);

            authorLayoutViewHolder = new AuthorLayoutViewHolder(itemView);
            numberResponsesViewHolder = new NumberResponsesViewHolder(itemView);
        }
    }

    public static class DiscussionResponseViewHolder extends RecyclerView.ViewHolder {
        ETextView responseCommentTextView;

        public DiscussionResponseViewHolder(View itemView) {
            super(itemView);
            responseCommentTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_comment_text_view);
        }
    }

}
