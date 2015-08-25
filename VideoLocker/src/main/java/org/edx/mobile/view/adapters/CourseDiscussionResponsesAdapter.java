package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.IAuthorData;
import org.edx.mobile.discussion.PinnedAuthor;
import org.edx.mobile.discussion.DiscussionThreadFollowedEvent;
import org.edx.mobile.task.FlagCommentTask;
import org.edx.mobile.task.FlagThreadTask;
import org.edx.mobile.task.FollowThreadTask;
import org.edx.mobile.task.VoteCommentTask;
import org.edx.mobile.task.VoteThreadTask;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.view_holders.AuthorLayoutViewHolder;
import org.edx.mobile.view.view_holders.DiscussionSocialLayoutViewHolder;
import org.edx.mobile.view.view_holders.NumberResponsesViewHolder;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CourseDiscussionResponsesAdapter extends RecyclerView.Adapter {

    public interface PaginationHandler {
        void loadMoreRecord(IPagination pagination);
    }

    @Inject
    Context context;

    @Inject
    Router router;

    private DiscussionThread discussionThread;
    private List<DiscussionComment> discussionResponses = new ArrayList<>();

    private PaginationHandler paginationHandler;

    private BasePagination pagination = new BasePagination(IPagination.DEFAULT_PAGE_SIZE);

    static class RowType {
        static final int THREAD = 0;
        static final int RESPONSE = 1;
        static final int MORE_BUTTON = 2;
    }

    public void setPaginationHandler(PaginationHandler paginationHandler) {
        this.paginationHandler = paginationHandler;
    }

    public BasePagination getPagination() {
        return pagination;
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
                    inflate(R.layout.show_more_button_row, parent, false);

            return new ShowMoreViewHolder(discussionThreadRow);
        }

        View discussionResponseRow = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.discussion_responses_response_row, parent, false);

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
                bindViewHolderToShowMoreRow((ShowMoreViewHolder) holder, position);
                break;
        }

    }

    private void bindViewHolderToThreadRow(DiscussionThreadViewHolder holder) {
        holder.threadTitleTextView.setText(discussionThread.getTitle());

        CharSequence charSequence = Html.fromHtml(discussionThread.getRenderedBody());
        holder.threadBodyTextView.setText(charSequence);

        if (discussionThread.isPinned()) {
            holder.threadPinnedIconView.setVisibility(View.VISIBLE);
        }

        bindSocialView(holder.socialLayoutViewHolder, discussionThread);
        bindAuthorView(holder.authorLayoutViewHolder, discussionThread);
        bindNumberResponsesView(holder.numberResponsesViewHolder);

        holder.actionBarViewHolder.reportLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                FlagThreadTask task = new FlagThreadTask(context, discussionThread, !discussionThread.isAbuseFlagged()) {
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

        updateActionBarVoteCount(holder.actionBarViewHolder,
                discussionThread.isVoted(), discussionThread.getVoteCount());
        updateActionBarFollow(holder.actionBarViewHolder, discussionThread.isFollowing());
        updateActionBarReportFlag(holder.actionBarViewHolder, discussionThread.isAbuseFlagged());
    }

    private void bindSocialView(DiscussionSocialLayoutViewHolder holder, DiscussionThread thread) {
        holder.setDiscussionThread(this.context, thread);

        holder.voteViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VoteThreadTask task = new VoteThreadTask(context, discussionThread, !discussionThread.isVoted()) {
                    @Override
                    public void onSuccess(DiscussionThread topicThreads) {
                        if (topicThreads != null) {
                            CourseDiscussionResponsesAdapter.this.discussionThread = topicThreads;
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

                FollowThreadTask task = new FollowThreadTask(context, discussionThread, !discussionThread.isFollowing()) {
                    @Override
                    public void onSuccess(DiscussionThread topicThreads) {
                        if (topicThreads != null) {
                            CourseDiscussionResponsesAdapter.this.discussionThread = topicThreads;
                            EventBus.getDefault().post(new DiscussionThreadFollowedEvent(topicThreads));
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
        holder.numberResponsesOrCommentsCountTextView.setText(Integer.toString(discussionThread.getCommentCount()));
        holder.numberResponsesOrCommentsLabel.setText(context.getResources().getQuantityString(
                R.plurals.number_responses_or_comments_responses_label, discussionThread.getCommentCount()));
    }

    private void bindViewHolderToShowMoreRow(ShowMoreViewHolder holder, int position) {

        holder.showMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paginationHandler != null) {
                    paginationHandler.loadMoreRecord(pagination);
                }
            }
        });

    }


    private void bindViewHolderToResponseRow(DiscussionResponseViewHolder holder, final int position) {
        final DiscussionComment comment = discussionResponses.get(position - 1); // Subtract 1 for the discussion thread row at position 0

        CharSequence charSequence = Html.fromHtml(comment.getRenderedBody());
        holder.responseCommentBodyTextView.setText(charSequence);

        holder.addCommentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showCourseDiscussionComments(context, comment);
            }
        });

        bindAuthorView(holder.authorLayoutViewHolder, comment);
        bindNumberCommentsView(holder.numberResponsesViewHolder, comment);
        final int positionInResponses = position - 1;
        bindSocialView(holder.socialLayoutViewHolder, positionInResponses, comment);

        holder.actionBarViewHolder.reportLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                FlagCommentTask task = new FlagCommentTask(context, comment, !comment.isAbuseFlagged()) {
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

        updateActionBarVoteCount(holder.actionBarViewHolder, comment.isVoted(), comment.getVoteCount());
        updateActionBarReportFlag(holder.actionBarViewHolder, comment.isAbuseFlagged());
        holder.actionBarViewHolder.followLayout.setVisibility(View.INVISIBLE);
    }

    private void bindSocialView(DiscussionSocialLayoutViewHolder holder, final int positionInResponses, final DiscussionComment response) {
        holder.setDiscussionResponse(this.context, response);

        holder.voteViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                VoteCommentTask task = new VoteCommentTask(context, response, !response.isVoted()) {
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
        int numChildren = response == null ? 0 : response.getChildren().size();

        if (numChildren == 0) {
            holder.numberResponsesOrCommentsCountTextView.setVisibility(View.GONE);
            holder.numberResponsesOrCommentsLabel.setText(context.getString(
                    R.string.number_responses_or_comments_add_comment_label));
        } else {
            holder.numberResponsesOrCommentsCountTextView.setText(Integer.toString(response.getChildren().size()));
            holder.numberResponsesOrCommentsLabel.setText(context.getResources().
                    getQuantityString(R.plurals.number_responses_or_comments_comments_label, numChildren));
        }
    }

    private void bindAuthorView(AuthorLayoutViewHolder holder, IAuthorData authorData) {
        holder.discussionAuthorTextView.setText(authorData.getAuthor());
        holder.discussionAuthorCreatedAtTextView.setText(
                DateUtil.formatPastDateRelativeToCurrentDate(authorData.getCreatedAt()));

        String priviledgedAuthorText = "";
        if (authorData.getAuthorLabel() == PinnedAuthor.STAFF) {
            priviledgedAuthorText = context.getString(R.string.discussion_priviledged_author_label_staff);

        } else if (authorData.getAuthorLabel() == PinnedAuthor.COMMUNITY_TA) {
            priviledgedAuthorText = context.getString(R.string.discussion_priviledged_author_label_ta);
        }

        holder.discussionAuthorPrivilegedAuthorTextView.setText(priviledgedAuthorText);
    }

    @Override
    public int getItemCount() {
        if (discussionThread == null)
            return 0;
        int total = 1 + discussionResponses.size();
        if (pagination.mayHasMorePages())
            total++;
        return total;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return RowType.THREAD;
        }

        if (pagination.mayHasMorePages() && position == getItemCount() - 1) {
            return RowType.MORE_BUTTON;
        }

        return RowType.RESPONSE;
    }


    public void setDiscussionThread(DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
        this.discussionResponses.clear();
        pagination.clear();
        notifyDataSetChanged();
    }

    public void addPage(List<DiscussionComment> discussionResponses, boolean hasMore) {
        this.discussionResponses.addAll(discussionResponses);
        pagination.setHasMorePages(hasMore);
        notifyDataSetChanged();
    }

    public static class ShowMoreViewHolder extends RecyclerView.ViewHolder {
        ETextView showMoreView;

        public ShowMoreViewHolder(View itemView) {
            super(itemView);

            showMoreView = (ETextView) itemView.
                    findViewById(R.id.show_more_button);
        }
    }

    public static class DiscussionThreadViewHolder extends RecyclerView.ViewHolder {
        ETextView threadTitleTextView;
        ETextView threadBodyTextView;
        IconView threadPinnedIconView;

        AuthorLayoutViewHolder authorLayoutViewHolder;
        NumberResponsesViewHolder numberResponsesViewHolder;
        DiscussionSocialLayoutViewHolder socialLayoutViewHolder;
        ActionBarViewHolder actionBarViewHolder;

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
            socialLayoutViewHolder = new DiscussionSocialLayoutViewHolder(itemView);
            actionBarViewHolder = new ActionBarViewHolder(itemView);
        }
    }

    public static class DiscussionResponseViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout addCommentLayout;
        ETextView responseCommentBodyTextView;
        AuthorLayoutViewHolder authorLayoutViewHolder;
        NumberResponsesViewHolder numberResponsesViewHolder;
        DiscussionSocialLayoutViewHolder socialLayoutViewHolder;
        ActionBarViewHolder actionBarViewHolder;

        public DiscussionResponseViewHolder(View itemView) {
            super(itemView);

            addCommentLayout = (RelativeLayout) itemView.findViewById(R.id.discussion_responses_comment_relative_layout);
            responseCommentBodyTextView = (ETextView) itemView.findViewById(R.id.discussion_responses_comment_body_text_view);
            authorLayoutViewHolder = new AuthorLayoutViewHolder(itemView);
            numberResponsesViewHolder = new NumberResponsesViewHolder(itemView);
            socialLayoutViewHolder = new DiscussionSocialLayoutViewHolder(itemView);
            actionBarViewHolder = new ActionBarViewHolder(itemView);
        }
    }

    void updateActionBarVoteCount(ActionBarViewHolder holder, boolean isVoted, int voteCount) {
        CharSequence voteText = ResourceUtil.getFormattedStringForQuantity(
                R.plurals.discussion_responses_action_bar_vote_text, voteCount);
        holder.voteCountTextView.setText(voteText);

        int iconColor = isVoted ? R.color.edx_brand_primary_base : R.color.edx_grayscale_neutral_base;
        holder.voteIconView.setIconColor(context.getResources().getColor(iconColor));
    }

    void updateActionBarFollow(ActionBarViewHolder holder, boolean isFollowing) {
        int followStringResId = isFollowing ? R.string.discussion_responses_action_bar_unfollow_text :
                R.string.discussion_responses_action_bar_follow_text;
        holder.followTextView.setText(context.getString(followStringResId));

        int iconColor = isFollowing ? R.color.edx_brand_primary_base : R.color.edx_grayscale_neutral_base;
        holder.followIconView.setIconColor(context.getResources().getColor(iconColor));
    }

    void updateActionBarReportFlag(ActionBarViewHolder holder, boolean isReported) {
        int reportStringResId = isReported ? R.string.discussion_responses_reported_label :
                R.string.discussion_responses_report_label;
        holder.reportTextView.setText(context.getString(reportStringResId));

        int iconColor = isReported ? R.color.edx_brand_primary_base : R.color.edx_grayscale_neutral_base;
        holder.reportIconView.setIconColor(context.getResources().getColor(iconColor));
    }
}
