package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
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
    @ColorInt
    private final int primaryBaseColor;
    @ColorInt
    private final int neutral_x_dark;
    @ColorInt
    private final int secondary_dark_color;
    @ColorInt
    private final int edx_utility_success_dark;

    // Record the current time at initialization to keep the display of the elapsed time durations stable.
    private long initialTimeStampMs = System.currentTimeMillis();

    private final Typeface semiBoldFont;

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_thread, environment);
        primaryBaseColor = context.getResources().getColor(R.color.primaryBaseColor);
        neutral_x_dark = context.getResources().getColor(R.color.neutralXDark);
        secondary_dark_color = context.getResources().getColor(R.color.secondaryDarkColor);
        edx_utility_success_dark = context.getResources().getColor(R.color.successBase);
        semiBoldFont = ResourcesCompat.getFont(context, R.font.inter_semi_bold);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionThread discussionThread) {
        ViewHolder holder = (ViewHolder) tag;
        {
            @DrawableRes final int iconResId;
            @ColorInt final int iconColor;
            if (discussionThread.getType() == DiscussionThread.ThreadType.QUESTION) {
                if (discussionThread.isHasEndorsed()) {
                    iconResId = R.drawable.ic_verified;
                    iconColor = edx_utility_success_dark;
                } else {
                    iconResId = R.drawable.ic_help_center;
                    iconColor = secondary_dark_color;
                }
            } else {
                iconResId = R.drawable.ic_chat;
                iconColor = (discussionThread.isRead() ? neutral_x_dark : primaryBaseColor);
            }
            holder.discussionPostTypeIcon.setImageDrawable(UiUtils.INSTANCE
                    .getDrawable(holder.discussionPostTypeIcon.getContext(), iconResId, 0, iconColor));
        }

        {
            final CharSequence threadTitle = discussionThread.getTitle();
            holder.discussionPostTitle.setText(threadTitle);
            if (!discussionThread.isRead()) {
                holder.discussionPostTitle.setTextAppearance(getContext(), R.style.discussion_title_text);
                holder.discussionPostTitle.setTypeface(semiBoldFont);
            } else {
                holder.discussionPostTitle.setTextAppearance(getContext(), R.style.discussion_responses_read);
                holder.discussionPostRepliesTextView.setTextAppearance(getContext(), R.style.discussion_responses_read);
                holder.discussionPostDateTextView.setTextAppearance(getContext(), R.style.discussion_responses_read);
                holder.discussionUnreadRepliesTextView.setTextAppearance(getContext(), R.style.discussion_responses_read);
                holder.discussionPostTypeIcon.setColorFilter(neutral_x_dark, PorterDuff.Mode.SRC_IN);
                holder.discussionPostClosedIcon.setColorFilter(neutral_x_dark, PorterDuff.Mode.SRC_IN);
                holder.discussionPostPinIcon.setColorFilter(neutral_x_dark, PorterDuff.Mode.SRC_IN);
                holder.discussionPostFollowIcon.setColorFilter(neutral_x_dark, PorterDuff.Mode.SRC_IN);
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
