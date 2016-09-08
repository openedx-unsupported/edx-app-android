package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.util.ResourceUtil;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class DiscussionPostsAdapter extends BaseListAdapter<DiscussionThread> {
    @ColorInt
    private final int edx_brand_primary_base;
    @ColorInt
    private final int edx_grayscale_neutral_dark;
    @ColorInt
    private final int edx_brand_secondary_dark;
    @ColorInt
    private final int edx_utility_success_dark;

    // Record the current time at initialization to keep the display of the elapsed time durations stable.
    private long initialTimeStampMs = System.currentTimeMillis();

    private final Typeface openSansSemiBoldFont;

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_thread, environment);
        edx_brand_primary_base = context.getResources().getColor(R.color.edx_brand_primary_base);
        edx_grayscale_neutral_dark = context.getResources().getColor(R.color.edx_brand_gray_base);
        edx_brand_secondary_dark = context.getResources().getColor(R.color.edx_brand_secondary_dark);
        edx_utility_success_dark = context.getResources().getColor(R.color.edx_success_text);
        openSansSemiBoldFont = TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf");
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionThread discussionThread) {
        ViewHolder holder = (ViewHolder) tag;
        {
            final Icon icon;
            @ColorInt
            final int iconColor;
            if (discussionThread.getType() == DiscussionThread.ThreadType.QUESTION) {
                if (discussionThread.isHasEndorsed()) {
                    icon = FontAwesomeIcons.fa_check_square_o;
                    iconColor = edx_utility_success_dark;
                } else {
                    icon = FontAwesomeIcons.fa_question;
                    iconColor = edx_brand_secondary_dark;
                }
            } else {
                icon = FontAwesomeIcons.fa_comments;
                iconColor = (discussionThread.isRead() ? edx_grayscale_neutral_dark : edx_brand_primary_base);
            }
            holder.discussionPostTypeIcon.setIcon(icon);
            holder.discussionPostTypeIcon.setIconColor(iconColor);
        }

        {
            final CharSequence threadTitle = discussionThread.getTitle();
            holder.discussionPostTitle.setText(discussionThread.isRead() ? threadTitle :
                    CalligraphyUtils.applyTypefaceSpan(threadTitle, openSansSemiBoldFont));
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
        final IconImageView discussionPostTypeIcon;
        final TextView discussionPostTitle;
        final IconImageView discussionPostClosedIcon;
        final IconImageView discussionPostPinIcon;
        final IconImageView discussionPostFollowIcon;
        final TextView discussionPostRepliesTextView;
        final TextView discussionPostDateTextView;
        final TextView discussionUnreadRepliesTextView;
        final View discussionSubtitleFirstPipe;
        final View discussionSubtitleSecondPipe;

        public ViewHolder(View convertView) {
            discussionPostTypeIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_type_icon);
            discussionPostTitle = (TextView) convertView.findViewById(R.id.discussion_post_title);
            discussionPostClosedIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_closed_icon);
            discussionPostPinIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_pin_icon);
            discussionPostFollowIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_following_icon);
            discussionPostRepliesTextView = (TextView) convertView.findViewById(R.id.discussion_post_replies_count);
            discussionPostDateTextView = (TextView) convertView.findViewById(R.id.discussion_post_date);
            discussionUnreadRepliesTextView = (TextView) convertView.findViewById(R.id.discussion_unread_replies_text);
            discussionSubtitleFirstPipe = convertView.findViewById(R.id.discussion_subtitle_first_pipe);
            discussionSubtitleSecondPipe = convertView.findViewById(R.id.discussion_subtitle_second_pipe);
        }
    }
}
