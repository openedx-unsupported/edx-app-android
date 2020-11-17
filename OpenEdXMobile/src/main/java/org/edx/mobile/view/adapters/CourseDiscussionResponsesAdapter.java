package org.edx.mobile.view.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionService.FlagBody;
import org.edx.mobile.discussion.DiscussionService.FollowBody;
import org.edx.mobile.discussion.DiscussionService.VoteBody;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.DiscussionThreadUpdatedEvent;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.DialogErrorNotification;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.view_holders.AuthorLayoutViewHolder;
import org.edx.mobile.view.view_holders.DiscussionSocialLayoutViewHolder;
import org.edx.mobile.view.view_holders.NumberResponsesViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;

public class CourseDiscussionResponsesAdapter extends RecyclerView.Adapter implements InfiniteScrollUtils.ListContentController<DiscussionComment> {

    public interface Listener {
        void onClickAuthor(@NonNull String username);

        void onClickAddComment(@NonNull DiscussionComment comment);

        void onClickViewComments(@NonNull DiscussionComment comment);
    }

    @Inject
    private Config config;

    @Inject
    private DiscussionService discussionService;

    @Inject
    private LoginPrefs loginPrefs;

    @NonNull
    private final Context context;

    @NonNull
    private final BaseFragment baseFragment;

    @NonNull
    private final Listener listener;

    @NonNull
    private DiscussionThread discussionThread;

    @NonNull
    private EnrolledCoursesResponse courseData;

    private final List<DiscussionComment> discussionResponses = new ArrayList<>();

    private boolean progressVisible = false;
    // Record the current time at initialization to keep the display of the elapsed time durations stable.
    private long initialTimeStampMs = System.currentTimeMillis();

    static class RowType {
        static final int THREAD = 0;
        static final int RESPONSE = 1;
        static final int PROGRESS = 2;
    }

    public CourseDiscussionResponsesAdapter(@NonNull Context context,
                                            @NonNull BaseFragment baseFragment,
                                            @NonNull Listener listener,
                                            @NonNull DiscussionThread discussionThread,
                                            @NonNull EnrolledCoursesResponse courseData) {
        this.context = context;
        this.baseFragment = baseFragment;
        this.discussionThread = discussionThread;
        this.listener = listener;
        this.courseData = courseData;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public void setProgressVisible(boolean visible) {
        if (progressVisible != visible) {
            progressVisible = visible;
            int progressRowIndex = 1 + discussionResponses.size();
            if (visible) {
                notifyItemInserted(progressRowIndex);
            } else {
                notifyItemRemoved(progressRowIndex);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RowType.THREAD) {
            View discussionThreadRow = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.row_discussion_responses_thread, parent, false);

            return new DiscussionThreadViewHolder(discussionThreadRow);
        }
        if (viewType == RowType.PROGRESS) {
            return new LoadingViewHolder(parent);
        }

        View discussionResponseRow = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.row_discussion_responses_response, parent, false);
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
            case RowType.PROGRESS:
                bindViewHolderToShowMoreRow((LoadingViewHolder) holder);
                break;
        }

    }

    private void bindViewHolderToThreadRow(final DiscussionThreadViewHolder holder) {
        holder.authorLayoutViewHolder.populateViewHolder(config, discussionThread,
                discussionThread, initialTimeStampMs,
                new Runnable() {
                    @Override
                    public void run() {
                        listener.onClickAuthor(discussionThread.getAuthor());
                    }
                });

        holder.threadTitleTextView.setText(discussionThread.getTitle());

        DiscussionTextUtils.renderHtml(holder.threadBodyTextView, discussionThread.getRenderedBody());

        String groupName = discussionThread.getGroupName();
        if (groupName == null) {
            holder.threadVisibilityTextView.setText(R.string.discussion_post_visibility_everyone);
        } else {
            holder.threadVisibilityTextView.setText(ResourceUtil.getFormattedString(
                    context.getResources(), R.string.discussion_post_visibility_cohort,
                    "cohort", groupName));
        }

        bindNumberResponsesView(holder.numberResponsesViewHolder);

        if (TextUtils.equals(loginPrefs.getUsername(), discussionThread.getAuthor())) {
            holder.actionsBar.setVisibility(View.GONE);
        } else {
            holder.actionsBar.setVisibility(View.VISIBLE);

            bindSocialView(holder.socialLayoutViewHolder, discussionThread);
            holder.discussionReportViewHolder.reportLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    boolean isReported = holder.discussionReportViewHolder.toggleReported();
                    discussionService.setThreadFlagged(discussionThread.getIdentifier(),
                            new FlagBody(isReported))
                            .enqueue(new ErrorHandlingCallback<DiscussionThread>(
                                    context, null, new DialogErrorNotification(baseFragment)) {
                                @Override
                                protected void onResponse(@NonNull final DiscussionThread topicThread) {
                                    discussionThread = discussionThread.patchObject(topicThread);
                                    EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(discussionThread));
                                }
                                @Override
                                protected void onFailure(@NonNull final Throwable error) {
                                    notifyItemChanged(0);
                                }
                            });
                }
            });

            holder.discussionReportViewHolder.setReported(discussionThread.isAbuseFlagged());
        }
    }

    private void bindSocialView(final DiscussionSocialLayoutViewHolder holder, DiscussionThread thread) {
        holder.setDiscussionThread(thread);

        holder.voteViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isVoted = holder.toggleVote(discussionThread.isVoted() ? discussionThread.getVoteCount()-1: discussionThread.getVoteCount());
                discussionService.setThreadVoted(discussionThread.getIdentifier(),
                        new VoteBody(isVoted))
                        .enqueue(new ErrorHandlingCallback<DiscussionThread>(
                                context, null, new DialogErrorNotification(baseFragment)) {
                            @Override
                            protected void onResponse(@NonNull final DiscussionThread updatedDiscussionThread) {
                                discussionThread = discussionThread.patchObject(updatedDiscussionThread);
                                EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(discussionThread));
                            }
                            @Override
                            protected void onFailure(@NonNull final Throwable error) {
                                notifyItemChanged(0);
                            }
                        });
            }
        });

        holder.threadFollowContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isFollowing = holder.toggleFollow();
                discussionService.setThreadFollowed(discussionThread.getIdentifier(),
                        new FollowBody(isFollowing))
                        .enqueue(new ErrorHandlingCallback<DiscussionThread>(
                                context, null, new DialogErrorNotification(baseFragment)) {
                            @Override
                            protected void onResponse(@NonNull final DiscussionThread updatedDiscussionThread) {
                                discussionThread = discussionThread.patchObject(updatedDiscussionThread);
                                EventBus.getDefault().post(new DiscussionThreadUpdatedEvent(discussionThread));
                            }
                            @Override
                            protected void onFailure(@NonNull final Throwable error) {
                                notifyItemChanged(0);
                            }
                        });
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

    private void bindViewHolderToShowMoreRow(LoadingViewHolder holder) {
    }


    private void bindViewHolderToResponseRow(final DiscussionResponseViewHolder holder, final int position) {
        final DiscussionComment comment = discussionResponses.get(position - 1); // Subtract 1 for the discussion thread row at position 0

        holder.authorLayoutViewHolder.populateViewHolder(config, comment,
                comment, initialTimeStampMs,
                new Runnable() {
                    @Override
                    public void run() {
                        listener.onClickAuthor(comment.getAuthor());
                    }
                });

        if (comment.isEndorsed()) {
            holder.authorLayoutViewHolder.answerTextView.setVisibility(View.VISIBLE);
            holder.responseAnswerAuthorTextView.setVisibility(View.VISIBLE);
            DiscussionThread.ThreadType threadType = discussionThread.getType();
            DiscussionTextUtils.AuthorAttributionLabel authorAttributionLabel;
            @StringRes int endorsementTypeStringRes;
            switch (threadType) {
                case QUESTION:
                    authorAttributionLabel = DiscussionTextUtils.AuthorAttributionLabel.ANSWER;
                    endorsementTypeStringRes = R.string.discussion_responses_answer;
                    break;
                case DISCUSSION:
                default:
                    authorAttributionLabel = DiscussionTextUtils.AuthorAttributionLabel.ENDORSEMENT;
                    endorsementTypeStringRes = R.string.discussion_responses_endorsed;
                    break;
            }
            holder.authorLayoutViewHolder.answerTextView.setText(endorsementTypeStringRes);
            DiscussionTextUtils.setAuthorAttributionText(holder.responseAnswerAuthorTextView,
                    authorAttributionLabel, comment.getEndorserData(), initialTimeStampMs,
                    new Runnable() {
                        @Override
                        public void run() {
                            listener.onClickAuthor(comment.getEndorsedBy());
                        }
                    });
        } else {
            holder.authorLayoutViewHolder.answerTextView.setVisibility(View.GONE);
            holder.responseAnswerAuthorTextView.setVisibility(View.GONE);
        }

        DiscussionTextUtils.renderHtml(holder.responseCommentBodyTextView, comment.getRenderedBody());

        if (discussionThread.isClosed() && comment.getChildCount() == 0) {
            holder.addCommentLayout.setEnabled(false);
        } else if (courseData.isDiscussionBlackedOut() && comment.getChildCount() == 0) {
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

        bindNumberCommentsView(holder.numberResponsesViewHolder, comment);

        if (TextUtils.equals(loginPrefs.getUsername(), comment.getAuthor())) {
            holder.actionsBar.setVisibility(View.GONE);
        } else {
            holder.actionsBar.setVisibility(View.VISIBLE);

            bindSocialView(holder.socialLayoutViewHolder, position, comment);
            holder.discussionReportViewHolder.reportLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    boolean isReported = holder.discussionReportViewHolder.toggleReported();
                    discussionService.setCommentFlagged(comment.getIdentifier(),
                            new FlagBody(isReported))
                            .enqueue(new ErrorHandlingCallback<DiscussionComment>(
                                    context, null, new DialogErrorNotification(baseFragment)) {
                                @Override
                                protected void onResponse(@NonNull final DiscussionComment comment) {
                                    discussionResponses.get(position - 1).patchObject(comment);
                                    discussionResponses.set(position - 1, comment);
                                }
                                @Override
                                protected void onFailure(@NonNull final Throwable error) {
                                    notifyItemChanged(position);
                                }
                            });
                }
            });

            holder.discussionReportViewHolder.setReported(comment.isAbuseFlagged());
            holder.socialLayoutViewHolder.threadFollowContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void bindSocialView(final DiscussionSocialLayoutViewHolder holder, final int position, final DiscussionComment response) {
        holder.setDiscussionResponse(response);

        holder.voteViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isVoted = holder.toggleVote(response.isVoted() ? response.getVoteCount()-1: response.getVoteCount());
                discussionService.setCommentVoted(response.getIdentifier(),
                        new VoteBody(isVoted))
                        .enqueue(new ErrorHandlingCallback<DiscussionComment>(
                                context, null, new DialogErrorNotification(baseFragment)) {
                            @Override
                            protected void onResponse(@NonNull final DiscussionComment comment) {
                                discussionResponses.get(position - 1).patchObject(comment);
                                discussionResponses.set(position - 1, comment);
                            }
                            @Override
                            protected void onFailure(@NonNull final Throwable error) {
                                notifyItemChanged(position);
                            }
                        });
            }
        });
    }

    private void bindNumberCommentsView(NumberResponsesViewHolder holder, DiscussionComment response) {
        String text;
        Icon icon;

        int numChildren = response == null ? 0 : response.getChildCount();

        if (response.getChildCount() == 0) {
            if (discussionThread.isClosed() || courseData.isDiscussionBlackedOut()) {
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
                        .colorRes(context, R.color.primaryBaseColor)
                        .sizeRes(context, R.dimen.edx_small),
                null, null, null);
    }

    @Override
    public int getItemCount() {
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
            return RowType.PROGRESS;
        }

        return RowType.RESPONSE;
    }

    public void updateDiscussionThread(@NonNull DiscussionThread discussionThread) {
        this.discussionThread = discussionThread;
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        int responsesCount = discussionResponses.size();
        discussionResponses.clear();
        notifyItemRangeRemoved(1, responsesCount);
    }

    @Override
    public void addAll(List<DiscussionComment> items) {
        int offset = 1 + discussionResponses.size();
        discussionResponses.addAll(items);
        notifyItemRangeInserted(offset, items.size());
    }

    public void addNewResponse(@NonNull DiscussionComment response) {
        // Since, we have a added a new response we need to update timestamps of all responses
        initialTimeStampMs = System.currentTimeMillis();
        int offset = 1 + discussionResponses.size();
        discussionResponses.add(response);
        incrementResponseCount();
        notifyItemInserted(offset);
    }

    public void incrementResponseCount() {
        discussionThread.incrementResponseCount();
        notifyItemChanged(0); // Response count is shown in the thread details header, so it also needs to be refreshed.
    }

    public void addNewComment(@NonNull DiscussionComment parent) {
        // Since, we have a added a new comment we need to update timestamps of all responses as well
        initialTimeStampMs = System.currentTimeMillis();
        discussionThread.incrementCommentCount();
        String parentId = parent.getIdentifier();
        for (ListIterator<DiscussionComment> responseIterator = discussionResponses.listIterator();
             responseIterator.hasNext(); ) {
            DiscussionComment response = responseIterator.next();
            if (parentId.equals(response.getIdentifier())) {
                response.incrementChildCount();
                notifyItemChanged(1 + responseIterator.previousIndex());
                break;
            }
        }
    }

    public static class DiscussionThreadViewHolder extends RecyclerView.ViewHolder {
        View actionsBar;
        TextView threadTitleTextView;
        TextView threadBodyTextView;
        TextView threadVisibilityTextView;

        AuthorLayoutViewHolder authorLayoutViewHolder;
        NumberResponsesViewHolder numberResponsesViewHolder;
        DiscussionSocialLayoutViewHolder socialLayoutViewHolder;
        DiscussionReportViewHolder discussionReportViewHolder;

        public DiscussionThreadViewHolder(View itemView) {
            super(itemView);

            actionsBar = itemView.findViewById(R.id.discussion_actions_bar);
            threadTitleTextView = (TextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_title_text_view);
            threadBodyTextView = (TextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_body_text_view);
            threadVisibilityTextView = (TextView) itemView.
                    findViewById(R.id.discussion_responses_thread_row_visibility_text_view);

            authorLayoutViewHolder = new AuthorLayoutViewHolder(itemView.findViewById(R.id.discussion_user_profile_row));
            numberResponsesViewHolder = new NumberResponsesViewHolder(itemView);
            socialLayoutViewHolder = new DiscussionSocialLayoutViewHolder(itemView);
            discussionReportViewHolder = new DiscussionReportViewHolder(itemView);
        }
    }

    public static class DiscussionResponseViewHolder extends RecyclerView.ViewHolder {
        View actionsBar;
        RelativeLayout addCommentLayout;
        TextView responseCommentBodyTextView;
        TextView responseAnswerAuthorTextView;

        AuthorLayoutViewHolder authorLayoutViewHolder;
        NumberResponsesViewHolder numberResponsesViewHolder;
        DiscussionSocialLayoutViewHolder socialLayoutViewHolder;
        DiscussionReportViewHolder discussionReportViewHolder;

        public DiscussionResponseViewHolder(View itemView) {
            super(itemView);

            actionsBar = itemView.findViewById(R.id.discussion_actions_bar);
            addCommentLayout = (RelativeLayout) itemView.findViewById(R.id.discussion_responses_comment_relative_layout);
            responseCommentBodyTextView = (TextView) itemView.findViewById(R.id.discussion_responses_comment_body_text_view);
            responseAnswerAuthorTextView = (TextView) itemView.findViewById(R.id.discussion_responses_answer_author_text_view);

            authorLayoutViewHolder = new AuthorLayoutViewHolder(itemView.findViewById(R.id.discussion_user_profile_row));
            numberResponsesViewHolder = new NumberResponsesViewHolder(itemView);
            socialLayoutViewHolder = new DiscussionSocialLayoutViewHolder(itemView);
            discussionReportViewHolder = new DiscussionReportViewHolder(itemView);
        }
    }
}
