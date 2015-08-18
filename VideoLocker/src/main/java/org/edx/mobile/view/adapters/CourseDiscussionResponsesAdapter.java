package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionComment;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.custom.ETextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CourseDiscussionResponsesAdapter extends RecyclerView.Adapter {

    @Inject
    Context context;

    @Inject
    Router router;

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

    private void bindViewHolderToThreadRow(final DiscussionThreadViewHolder holder) {
        holder.threadTitleTextView.setText(discussionThread.getTitle());
        holder.threadBodyTextView.setText(discussionThread.getRawBody());

        if (discussionThread.isPinned()) {
            holder.threadPinnedIconView.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> authorMap = new HashMap<>();
        authorMap.put("author", discussionThread.getAuthor());
        authorMap.put("created_at", DateUtil.formatPastDateRelativeToCurrentDate(discussionThread.getCreatedAt()));
        String authorLabel = discussionThread.getAuthorLabel() == null ? "" :
                discussionThread.getAuthorLabel().getReadableText(context);
        authorMap.put("author_label", authorLabel);

        CharSequence authorText = ResourceUtil.getFormattedString(R.string.discussion_responses_author, authorMap);

        holder.authorTextView.setText(authorText);
        holder.numberResponsesTextView.setText(ResourceUtil.getFormattedStringForQuantity(
                R.plurals.number_responses_or_comments_responses_label, discussionThread.getCommentCount()));

        holder.reportTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                final TextView reportTextView = (TextView) v;
                boolean isReport = reportTextView.getText().toString().equalsIgnoreCase("Report");
                new DiscussionAPI().flagThread(discussionThread, isReport ? true : false, new APICallback<DiscussionThread>() {
                    @Override
                    public void success(DiscussionThread thread) {
                        if (thread.isAbuseFlagged()) {
                            reportTextView.setText("Reported");
                            holder.reportIconView.setIconColor(context.getResources().getColor(R.color.edx_utility_error));
                        } else {
                            reportTextView.setText("Report");
                            holder.reportIconView.setIconColor(context.getResources().getColor(R.color.edx_brand_primary_base));
                        }
                    }

                    @Override
                    public void failure(Exception e) {
                    }
                });
            }
        });

        holder.voteCountLabelTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                final TextView voteTextView = (TextView) v;
                new DiscussionAPI().voteThread(discussionThread, !discussionThread.isVoted(), new APICallback<DiscussionThread>() {
                    @Override
                    public void success(DiscussionThread thread) {
                        // TODO: localization
                        if (thread.getVoteCount() == 1)
                            voteTextView.setText("Votes");
                        else
                            voteTextView.setText("Votes");
                        holder.voteCountTextView.setText(Integer.toString(thread.getVoteCount()));
                    }

                    @Override
                    public void failure(Exception e) {
                    }
                });
            }
        });

    }

    private void bindViewHolderToResponseRow(final DiscussionResponseViewHolder holder, int position) {
        final DiscussionComment comment = discussionResponses.get(position - 1); // Subtract 1 for the discussion thread row at position 0

        holder.responseCommentBodyTextView.setText(comment.getRawBody());

        holder.addCommentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showCourseDiscussionComments(context, comment);
            }
        });

        HashMap<String, String> authorMap = new HashMap<>();
        authorMap.put("author", comment.getAuthor());
        authorMap.put("created_at", DateUtil.formatPastDateRelativeToCurrentDate(discussionThread.getCreatedAt()));
        String authorLabel = comment.getAuthorLabel() == null ? "" :
                comment.getAuthorLabel().getReadableText(context);
        authorMap.put("author_label", authorLabel);

        CharSequence authorText = ResourceUtil.getFormattedString(R.string.discussion_responses_author, authorMap);

        holder.authorTextView.setText(authorText);

        if (comment.isEndorsed()) {
            holder.answerLayout.setVisibility(View.VISIBLE);
            holder.endorsedTextView.setVisibility(View.VISIBLE);

            String endorsedAt = DateUtil.formatPastDateRelativeToCurrentDate(comment.getEndorsedAt());
            String endorsedAuthor = comment.getEndorsedBy();
            String endorsedLabel = comment.getEndorsedByLabel() == null ? "" :
                    comment.getEndorsedByLabel().getReadableText(context);

            HashMap<String, String> endorsedAuthorMap = new HashMap<>();
            endorsedAuthorMap.put("endorsed_by_author", endorsedAuthor);
            endorsedAuthorMap.put("endorsed_at", endorsedAt);
            endorsedAuthorMap.put("endorsed_by_label", endorsedLabel);

            CharSequence endorsedByText = ResourceUtil.getFormattedString(
                    R.string.discussion_responses_endorsed_author, endorsedAuthorMap);

            holder.endorsedTextView.setText(endorsedByText);
        }

        int numChildren = comment == null ? 0 : comment.getChildren().size();

        if (numChildren == 0) {
            holder.numberCommentsTextView.setText(context.getString(
                    R.string.number_responses_or_comments_add_comment_label));
        } else {
            holder.numberCommentsTextView.setText(ResourceUtil.getFormattedStringForQuantity(
                    R.plurals.number_responses_or_comments_comments_label, numChildren));
        }

        holder.reportTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                final TextView reportTextView = (TextView) v;
                boolean isReport = reportTextView.getText().toString().equalsIgnoreCase("Report");
                new DiscussionAPI().flagComment(comment, isReport ? true : false, new APICallback<DiscussionComment>() {
                    @Override
                    public void success(DiscussionComment comment) {
                        if (comment.isAbuseFlagged()) {
                            reportTextView.setText("Reported");
                            holder.reportIconView.setIconColor(context.getResources().getColor(R.color.edx_utility_error));
                        } else {
                            reportTextView.setText("Report");
                            holder.reportIconView.setIconColor(context.getResources().getColor(R.color.edx_brand_primary_base));
                        }
                    }

                    @Override
                    public void failure(Exception e) {
                    }
                });
            }
        });

        holder.voteCountLabelTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                final TextView voteTextView = (TextView) v;
                new DiscussionAPI().voteComment(comment, !comment.isVoted(), new APICallback<DiscussionComment>() {
                    @Override
                    public void success(DiscussionComment comment) {
                        // TODO: localization
                        if (comment.getVoteCount() == 1)
                            voteTextView.setText("Vote");
                        else
                            voteTextView.setText("Votes");
                        holder.voteCountTextView.setText(Integer.toString(comment.getVoteCount()));
                    }

                    @Override
                    public void failure(Exception e) {
                    }
                });
            }
        });

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
        ETextView authorTextView;
        ETextView numberResponsesTextView;
        IconView reportIconView;
        ETextView reportTextView;

        IconView voteIconView;
        ETextView voteCountLabelTextView;
        ETextView voteCountTextView;

        public DiscussionThreadViewHolder(View itemView) {
            super(itemView);

            threadTitleTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_title_text_view);
            threadBodyTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_body_text_view);
            threadPinnedIconView = (IconView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_pinned_icon_view);
            authorTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_author_label);
            numberResponsesTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_number_responses_or_comments_text_view);
            reportIconView = (IconView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_report_icon_view);
            reportTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_report_text_view);
            voteIconView = (IconView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_vote_icon_view);
            voteCountTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_vote_count_text_view);
            voteCountLabelTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_vote_count_label);
        }
    }

    public static class DiscussionResponseViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout addCommentLayout;
        ETextView responseCommentBodyTextView;
        ETextView numberCommentsTextView;
        RelativeLayout answerLayout;
        ETextView endorsedTextView;
        ETextView authorTextView;

        IconView reportIconView;
        ETextView reportTextView;

        IconView voteIconView;
        ETextView voteCountLabelTextView;
        ETextView voteCountTextView;

        public DiscussionResponseViewHolder(View itemView) {
            super(itemView);

            answerLayout = (RelativeLayout) itemView.findViewById(R.id.discussion_responses_answer_layout);
            addCommentLayout = (RelativeLayout) itemView.findViewById(R.id.discussion_responses_comment_relative_layout);
            responseCommentBodyTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_comment_body_text_view);
            numberCommentsTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_number_responses_or_comments_text_view);
            endorsedTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_endorsed_text_view);
            authorTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_response_row_author_label);

            reportIconView = (IconView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_report_icon_view);
            reportTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_report_text_view);

            voteIconView = (IconView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_vote_icon_view);
            voteCountTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_vote_count_text_view);
            voteCountLabelTextView = (ETextView) itemView.
                    findViewById(R.id.discussion_responses_action_bar_vote_count_label);
        }
    }

}
