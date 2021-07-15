package org.edx.mobile.view.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UiUtils;

public class DiscussionPostsAdapter extends BaseListAdapter<DiscussionThread> {
    // Record the current time at initialization to keep the display of the elapsed time durations stable.
    private long initialTimeStampMs = System.currentTimeMillis();

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_thread, environment);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionThread discussionThread) {
        ViewHolder holder = (ViewHolder) tag;
        {
            @DrawableRes final int iconResId;
            @ColorRes final int iconColorRes;
            if (discussionThread.getType() == DiscussionThread.ThreadType.QUESTION) {
                if (discussionThread.isHasEndorsed()) {
                    iconResId = R.drawable.ic_verified;
                    iconColorRes = R.color.successBase;
                } else {
                    iconResId = R.drawable.ic_help_center;
                    iconColorRes = R.color.secondaryDarkColor;
                }
            } else {
                iconResId = R.drawable.ic_chat;
                iconColorRes = (discussionThread.isRead() ? R.color.neutralXDark : R.color.primaryBaseColor);
            }
            holder.discussionPostTypeIcon.setImageDrawable(UiUtils.INSTANCE
                    .getDrawable(holder.discussionPostTypeIcon.getContext(), iconResId, 0, iconColorRes));
        }

        {
            final CharSequence threadTitle = discussionThread.getTitle();
            holder.discussionPostTitle.setText(threadTitle);
            if (!discussionThread.isRead()) {
                holder.discussionPostTitle.setTextAppearance(getContext(), R.style.discussion_title_text);
                holder.discussionPostTitle.setTypeface(ResourcesCompat.getFont(getContext(), R.font.inter_semi_bold));
            } else {
                holder.discussionPostTitle.setTextAppearance(getContext(), R.style.discussion_responses_read);
                holder.discussionPostRepliesTextView.setTextAppearance(getContext(), R.style.discussion_responses_read);
                holder.discussionPostDateTextView.setTextAppearance(getContext(), R.style.discussion_responses_read);
                holder.discussionUnreadRepliesTextView.setTextAppearance(getContext(), R.style.discussion_responses_read);
                UiUtils.INSTANCE.setImageViewColor(getContext(), holder.discussionPostTypeIcon, R.color.neutralXDark);
                UiUtils.INSTANCE.setImageViewColor(getContext(), holder.discussionPostClosedIcon, R.color.neutralXDark);
                UiUtils.INSTANCE.setImageViewColor(getContext(), holder.discussionPostPinIcon, R.color.neutralXDark);
                UiUtils.INSTANCE.setImageViewColor(getContext(), holder.discussionPostFollowIcon, R.color.neutralXDark);
            }
        }

        holder.discussionPostClosedIcon.setVisibility(discussionThread.isClosed() ? View.VISIBLE : View.GONE);
        holder.discussionPostPinIcon.setVisibility(discussionThread.isPinned() ? View.VISIBLE : View.GONE);
        holder.discussionPostFollowIcon.setVisibility(discussionThread.isFollowing() ? View.VISIBLE : View.GONE);

        {
            final int commentCount = discussionThread.getCommentCount();
            if (commentCount == 0) {
                holder.discussionPostRepliesTextView.setVisibility(View.GONE);
                holder.discussionSubtitleFirstPipe.setVisibility(View.GONE);
            } else {
                final CharSequence totalReplies = ResourceUtil.getFormattedString(
                        getContext().getResources(), R.string.discussion_post_total_replies,
                        "total_replies", getFormattedCount(commentCount));
                holder.discussionSubtitleFirstPipe.setVisibility(
                        isAnyIconVisible(discussionThread) ? View.VISIBLE : View.GONE
                );
                holder.discussionPostRepliesTextView.setText(totalReplies);
                holder.discussionPostRepliesTextView.setVisibility(View.VISIBLE);
            }
        }

        {
            final CharSequence lastPostDate = DiscussionTextUtils.getRelativeTimeSpanString(getContext(),
                    initialTimeStampMs, discussionThread.getUpdatedAt().getTime(),
                    DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR);
            holder.discussionSubtitleSecondPipe.setVisibility(
                    (isAnyIconVisible(discussionThread) || discussionThread.getCommentCount() != 0) ?
                            View.VISIBLE : View.GONE
            );
            holder.discussionPostDateTextView.setText(ResourceUtil.getFormattedString(
                    getContext().getResources(), R.string.discussion_post_last_interaction_date,
                    "date", lastPostDate));
        }

        {
            final int unreadCommentCount = discussionThread.getUnreadCommentCount();
            if (unreadCommentCount == 0) {
                holder.discussionUnreadRepliesTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.discussionUnreadRepliesTextView.setVisibility(View.VISIBLE);
                holder.discussionUnreadRepliesTextView.setText(getFormattedCount(unreadCommentCount));
            }
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new ViewHolder(convertView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    /**
     * Checks whether an icon is visible in the action layout.
     * Based on the result of this function we decide if we need to append
     * a pipe sign before the following text to the icons.
     *
     * @param thread The thread model.
     * @return <code>true</code> if a thread is closed, pinned or being followed by a user,
     * <code>false</code> otherwise.
     */
    private boolean isAnyIconVisible(@NonNull DiscussionThread thread) {
        return thread.isClosed() || thread.isFollowing() || thread.isPinned();
    }

    /**
     * Format a number according to the count format.
     * Based on the count value this function decides what string has to be
     * shown to the user.
     *
     * @param count The count.
     * @return 99+ if the count is equal to or greater than 99, otherwise the actual count
     * as a String.
     */
    private String getFormattedCount(int count) {
        return count >= 99 ? "99+" : String.valueOf(count);
    }

    private static class ViewHolder extends BaseViewHolder {
        final AppCompatImageView discussionPostTypeIcon;
        final TextView discussionPostTitle;
        final AppCompatImageView discussionPostClosedIcon;
        final AppCompatImageView discussionPostPinIcon;
        final AppCompatImageView discussionPostFollowIcon;
        final TextView discussionPostRepliesTextView;
        final TextView discussionPostDateTextView;
        final TextView discussionUnreadRepliesTextView;
        final View discussionSubtitleFirstPipe;
        final View discussionSubtitleSecondPipe;

        public ViewHolder(View convertView) {
            discussionPostTypeIcon = (AppCompatImageView) convertView.findViewById(R.id.discussion_post_type_icon);
            discussionPostTitle = (TextView) convertView.findViewById(R.id.discussion_post_title);
            discussionPostClosedIcon = (AppCompatImageView) convertView.findViewById(R.id.discussion_post_closed_icon);
            discussionPostPinIcon = (AppCompatImageView) convertView.findViewById(R.id.discussion_post_pin_icon);
            discussionPostFollowIcon = (AppCompatImageView) convertView.findViewById(R.id.discussion_post_following_icon);
            discussionPostRepliesTextView = (TextView) convertView.findViewById(R.id.discussion_post_replies_count);
            discussionPostDateTextView = (TextView) convertView.findViewById(R.id.discussion_post_date);
            discussionUnreadRepliesTextView = (TextView) convertView.findViewById(R.id.discussion_unread_replies_text);
            discussionSubtitleFirstPipe = convertView.findViewById(R.id.discussion_subtitle_first_pipe);
            discussionSubtitleSecondPipe = convertView.findViewById(R.id.discussion_subtitle_second_pipe);
        }
    }
}
