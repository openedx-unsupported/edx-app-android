package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;
import org.edx.mobile.task.SetCommentFlaggedTask;
import org.edx.mobile.task.SetCommentVotedTask;
import org.edx.mobile.task.SetThreadFlaggedTask;
import org.edx.mobile.task.SetThreadFollowedTask;
import org.edx.mobile.task.SetThreadVotedTask;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.view_holders.AuthorLayoutViewHolder;
import org.edx.mobile.view.view_holders.DiscussionSocialLayoutViewHolder;
import org.edx.mobile.view.view_holders.NumberResponsesViewHolder;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CourseDiscussionResponsesAdapter extends RecyclerView.Adapter implements InfiniteScrollUtils.ListContentController<DiscussionComment> {

    public interface Listener {
        void onClickAuthor(@NonNull String username);

        void onClickAddComment(@NonNull DiscussionComment comment);

        void onClickViewComments(@NonNull DiscussionComment comment);
    }

    @NonNull
    private final Context context;

    @NonNull
    private final Listener listener;

    @NonNull
    private DiscussionThread discussionThread;

    private List<DiscussionComment> discussionResponses = new ArrayList<>();

    private boolean progressVisible = false;

    static class RowType {
        static final int THREAD = 0;
        static final int RESPONSE = 1;
        static final int MORE_BUTTON = 2;
    }

    public CourseDiscussionResponsesAdapter(@NonNull Context context, @NonNull Listener listener, @NonNull DiscussionThread discussionThread) {
        this.context = context;
        this.discussionThread = discussionThread;
        this.listener = listener;
    }

    @Override
    public void setProgressVisible(boolean visible) {
        progressVisible = visible;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RowType.THREAD) {
            View discussionThreadRow = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.discussion_responses_thread_row, parent, false);

            return new DiscussionThreadViewHolder(discussionThreadRow);
        }
        if (viewType == RowType.MORE_BUTTON) {
            View discussionThreadRow = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.list_view_footer_progress, parent, false);

            return new ShowMoreViewHolder(discussionThreadRow);
        }

        View discussionResponseRow = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.discussion_responses_response_row, parent, false);
        // CardView adds extra padding on pre-lollipop devices for shadows
        // Since, we've set cardUseCompatPadding to true in the layout file
        // so we need to deduct the extra padding from margins in any case to get the desired results
        UiUtil.adjustCardViewMargins(discussionResponseRow);

        return new DiscussionResponseViewHolder(discussionResponseRow);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int rowType = getItemViewType(position);
        switch (rowType) {
            case RowType.THREAD:
                bindViewHolderToThreadRow((DiscussionThreadViewHolder) holder);
                break;
            case RowType.RESPONSE:
                bindViewHolderToResponseRow((DiscussionResponseViewHolder) holder, position);
                break;
            case RowType.MORE_BUTTON:
                bindViewHolderToShowMoreRow((ShowMoreViewHolder) holder);
                break;
        }

    }

    private void bindViewHolderToThreadRow(DiscussionThreadViewHolder holder) {
        holder.threadTitleTextView.setText(discussionThread.getTitle());

        holder.threadBodyTextView.setText(DiscussionTextUtils.parseHtml(discussionThread.getRenderedBody()));

        String groupName = discussionThread.getGroupName();
        if (groupName == null) {
            holder.threadVisibilityTextView.setText(R.string.discussion_post_visibility_everyone);
        } else {
            holder.threadVisibilityTextView.setText(ResourceUtil.getFormattedString(
                    context.getResources(), R.string.discussion_post_visibility_cohort,
                    "cohort", groupName));
        }

        holder.threadClosedIconImageView.setVisibility(discussionThread.isClosed() ? View.VISIBLE : View.GONE);
        holder.threadPinnedIconImageView.setVisibility(discussionThread.isPinned() ? View.VISIBLE : View.GONE);

        bindSocialView(holder.socialLayoutViewHolder, discussionThread);
        DiscussionTextUtils.setAuthorAttributionText(
                holder.authorLayoutViewHolder.discussionAuthorTextView,
                R.string.post_attribution,
                discussionThread, new Runnable() {
                    @Override
                    public void run() {
                        listener.onClickAuthor(discussionThread.getAuthor());
                    }
                });
        bindNumberResponsesView(holder.numberResponsesViewHolder);

        holder.discussionReportViewHolder.reportLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                SetThreadFlaggedTask task = new SetThreadFlaggedTask(context, discussionThread, !discussionThread.isAbuseFlagged()) {
                    @Override
                    public void onSuccess(DiscussionThread topicThread) {
                        if (topicThread != null) {
                            CourseDiscussionResponsesAdapter.this.discussionThread = topicThread;
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        logger.error(ex);
                    }
                };
                task.execute();
            }
        });

        holder.discussionReportViewHolder.setReported(discussionThread.isAbuseFlagged());
    }

    private void bindSocialView(DiscussionSocialLayoutViewHolder holder, DiscussionThread thread) {
        holder.setDiscussionThread(thread);

        holder.voteViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetThreadVotedTask task = new SetThreadVotedTask(context, discussionThread, !discussionThread.isVoted()) {
                    @Override
                    public void onSuccess(DiscussionThread updatedDiscussionThread) {
                        if (updatedDiscussionThread != null) {
                            CourseDiscussionResponsesAdapter.this.discussionThread = updatedDiscussionThread;
                            EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(updatedDiscussionThread));
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        logger.error(ex);
                    }
                };
                task.execute();
            }
        });

        holder.threadFollowContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetThreadFollowedTask task = new SetThreadFollowedTask(context, discussionThread, !discussionThread.isFollowing()) {
                    @Override
                    public void onSuccess(DiscussionThread updatedDiscussionThread) {
                        if (updatedDiscussionThread != null) {
                            CourseDiscussionResponsesAdapter.this.discussionThread = updatedDiscussionThread;
                            EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(updatedDiscussionThread));
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        logger.error(ex);
                    }
                };
                task.execute();
            }
        });
    }

    private void bindNumberResponsesView(NumberResponsesViewHolder holder) {
        int responsesCount = discussionThread.getResponseCount();
        if (responsesCount < 0) {
            // The responses count is not available yet, so hide the view.
            holder.numberResponsesOrCommentsLabel.setVisibility(View.GONE);
        } else {
            holder.numberResponsesOrCommentsLabel.setVisibility(View.VISIBLE);
            holder.numberResponsesOrCommentsLabel.setText(holder.numberResponsesOrCommentsLabel.getResources().getQuantityString(
                    R.plurals.number_responses_or_comments_responses_label, responsesCount, responsesCount));
        }
    }

    private void bindViewHolderToShowMoreRow(ShowMoreViewHolder holder) {
    }


    private void bindViewHolderToResponseRow(DiscussionResponseViewHolder holder, final int position) {
        final DiscussionComment comment = discussionResponses.get(position - 1); // Subtract 1 for the discussion thread row at position 0

        holder.responseCommentBodyTextView.setText(DiscussionTextUtils.parseHtml(comment.getRenderedBody()));

        if (discussionThread.isClosed() && comment.getChildCount() == 0) {
            holder.addCommentLayout.setEnabled(false);
        } else {
            holder.addCommentLayout.setEnabled(true);
            holder.addCommentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.getChildCount() > 0) {
                        listener.onClickViewComments(comment);
                    } else {
                        listener.onClickAddComment(comment);
                    }
                }
            });
        }

        DiscussionTextUtils.setAuthorAttributionText(
                holder.authorLayoutViewHolder.discussionAuthorTextView,
                R.string.post_attribution,
                comment, new Runnable() {
                    @Override
                    public void run() {
                        listener.onClickAuthor(comment.getAuthor());
                    }
                });
        bindNumberCommentsView(holder.numberResponsesViewHolder, comment);
        final int positionInResponses = position - 1;
        bindSocialView(holder.socialLayoutViewHolder, positionInResponses, comment);

        holder.discussionReportViewHolder.reportLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                SetCommentFlaggedTask task = new SetCommentFlaggedTask(context, comment, !comment.isAbuseFlagged()) {
                    @Override
                    public void onSuccess(DiscussionComment comment) {
                        if (comment != null) {
                            discussionResponses.remove(positionInResponses);
                            discussionResponses.add(positionInResponses, comment);
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        logger.error(ex);
                    }
                };
                task.execute();
            }
        });

        holder.discussionReportViewHolder.setReported(comment.isAbuseFlagged());

        holder.socialLayoutViewHolder.threadFollowContainer.setVisibility(View.INVISIBLE);

        if (comment.isEndorsed()) {
            DiscussionThread.ThreadType threadType = discussionThread.getType();
            @StringRes int attributionStringRes;
            @StringRes int endorsementTypeStringRes;
            switch (threadType) {
                case QUESTION:
                    attributionStringRes = R.string.answer_author_attribution;
                    endorsementTypeStringRes = R.string.discussion_responses_answer;
                    break;
                case DISCUSSION:
                default:
                    endorsementTypeStringRes = R.string.discussion_responses_endorsed;
                    attributionStringRes = R.string.endorser_attribution;
                    break;
            }
            holder.responseAnswerTextView.setText(endorsementTypeStringRes);
            DiscussionTextUtils.setAuthorAttributionText(holder.responseAnswerAuthorTextView,
                    attributionStringRes, comment.getEndorserData(),
                    new Runnable() {
                        @Override
                        public void run() {
                            listener.onClickAuthor(comment.getAuthor());
                        }
                    });
            holder.responseAnswerTextView.setVisibility(View.VISIBLE);
            holder.responseAnswerAuthorTextView.setVisibility(View.VISIBLE);
        } else {
            holder.responseAnswerTextView.setVisibility(View.GONE);
            holder.responseAnswerAuthorTextView.setVisibility(View.GONE);
        }
    }

    private void bindSocialView(DiscussionSocialLayoutViewHolder holder, final int positionInResponses, final DiscussionComment response) {
        holder.setDiscussionResponse(response);

        holder.voteViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SetCommentVotedTask task = new SetCommentVotedTask(context, response, !response.isVoted()) {
                    @Override
                    public void onSuccess(DiscussionComment comment) {
                        if (comment != null) {
                            discussionResponses.remove(positionInResponses);
                            discussionResponses.add(positionInResponses, comment);
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        logger.error(ex);
                    }
                };
                task.execute();
            }
        });
    }

    private void bindNumberCommentsView(NumberResponsesViewHolder holder, DiscussionComment response) {
        String text;
        Icon icon;

        int numChildren = response == null ? 0 : response.getChildCount();

        if (response.getChildCount() == 0) {
            if (discussionThread.isClosed()) {
                text = context.getString(R.string.discussion_add_comment_disabled_title);
                icon = FontAwesomeIcons.fa_lock;
            } else {
                text = context.getString(R.string.number_responses_or_comments_add_comment_label);
                icon = FontAwesomeIcons.fa_comment;
            }
        } else {
            text = context.getResources().getQuantityString(
                    R.plurals.number_responses_or_comments_comments_label, numChildren, numChildren);
            icon = FontAwesomeIcons.fa_comment;
        }

        holder.numberResponsesOrCommentsLabel.setText(text);
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                holder.numberResponsesOrCommentsLabel,
                new IconDrawable(context, icon)
                        .colorRes(context, R.color.edx_grayscale_neutral_base)
                        .sizeRes(context, R.dimen.edx_xxx_small),
                null, null, null);
    }

    @Override
    public int getItemCount() {
        if (discussionThread == null)
            return 0;
        int total = 1 + discussionResponses.size();
        if (progressVisible)
            total++;
        return total;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return RowType.THREAD;
        }

        if (progressVisible && position == getItemCount() - 1) {
            return RowType.MORE_BUTTON;
        }

        return RowType.RESPONSE;
    }

    @Override
    public void clear() {
        discussionResponses.clear();
        notifyDataSetChanged();
    }

    @Override
    public void addAll(List<DiscussionComment> items) {
        discussionResponses.addAll(items);
        notifyDataSetChanged();
    }

    public void addNewResponse(@NonNull DiscussionComment response) {
        discussionResponses.add(response);
        discussionThread.incrementResponseCount();
        notifyDataSetChanged();
    }

    public void addNewComment(@NonNull DiscussionComment parent) {
        String parentId = parent.getIdentifier();
        for (DiscussionComment response : discussionResponses) {
            if (parentId.equals(response.getIdentifier())) {
                response.incrementChildCount();
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void setResponseCount(int responseCount) {
        discussionThread.setResponseCount(responseCount);
        notifyDataSetChanged();
    }

    public static class ShowMoreViewHolder extends RecyclerView.ViewHolder {
        public ShowMoreViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class DiscussionThreadViewHolder extends RecyclerView.ViewHolder {
        TextView threadTitleTextView;
        TextView threadBodyTextView;
        TextView threadVisibilityTextView;
        IconImageView threadClosedIconImageView;
        IconImageView threadPinnedIconImageView;

        AuthorLayoutViewHolder authorLayoutViewHolder;
        NumberResponsesViewHolder numberResponsesViewHolder;
        DiscussionSocialLayoutViewHolder socialLayoutViewHolder;
        DiscussionReportViewHolder discussionReportViewHolder;

        public DiscussionThreadViewHolder(View itemView) {
            super(itemView);

            threadTitleTextView = (TextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_title_text_view);
            threadBodyTextView = (TextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_body_text_view);
            threadVisibilityTextView = (TextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_visibility_text_view);
            threadClosedIconImageView = (IconImageView) itemView.
                    findViewById(R.id.discussion_responses_thread_closed_icon_view);
            threadPinnedIconImageView = (IconImageView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_pinned_icon_view);

            authorLayoutViewHolder = new AuthorLayoutViewHolder(itemView);
            numberResponsesViewHolder = new NumberResponsesViewHolder(itemView);
            socialLayoutViewHolder = new DiscussionSocialLayoutViewHolder(itemView);
            discussionReportViewHolder = new DiscussionReportViewHolder(itemView);
        }
    }

    public static class DiscussionResponseViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout addCommentLayout;
        TextView responseAnswerTextView;
        TextView responseCommentBodyTextView;
        TextView responseAnswerAuthorTextView;
        AuthorLayoutViewHolder authorLayoutViewHolder;
        NumberResponsesViewHolder numberResponsesViewHolder;
        DiscussionSocialLayoutViewHolder socialLayoutViewHolder;
        DiscussionReportViewHolder discussionReportViewHolder;

        public DiscussionResponseViewHolder(View itemView) {
            super(itemView);

            addCommentLayout = (RelativeLayout) itemView.findViewById(R.id.discussion_responses_comment_relative_layout);
            responseAnswerTextView = (TextView) itemView.findViewById(R.id.discussion_responses_answer_text_view);
            responseCommentBodyTextView = (TextView) itemView.findViewById(R.id.discussion_responses_comment_body_text_view);
            responseAnswerAuthorTextView = (TextView) itemView.findViewById(R.id.discussion_responses_answer_author_text_view);
            authorLayoutViewHolder = new AuthorLayoutViewHolder(itemView);
            numberResponsesViewHolder = new NumberResponsesViewHolder(itemView);
            socialLayoutViewHolder = new DiscussionSocialLayoutViewHolder(itemView);
            discussionReportViewHolder = new DiscussionReportViewHolder(itemView);

            final Context context = responseAnswerTextView.getContext();
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    responseAnswerTextView,
                    new IconDrawable(context, FontAwesomeIcons.fa_check_square_o)
                            .sizeRes(context, R.dimen.edx_xxx_small)
                            .colorRes(context, R.color.edx_utility_success),
                    null, null, null);
        }
    }
}
