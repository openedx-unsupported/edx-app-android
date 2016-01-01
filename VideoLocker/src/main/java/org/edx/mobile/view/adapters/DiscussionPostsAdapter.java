package org.edx.mobile.view.adapters;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.widget.TextViewCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.discussion.DiscussionThread;

public class DiscussionPostsAdapter extends BaseListAdapter<DiscussionThread> {

    private boolean voteCountsEnabled;

    @ColorInt
    private final int edx_brand_primary_base;
    @ColorInt
    private final int edx_grayscale_neutral_light;

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_thread, environment);
        edx_brand_primary_base = context.getResources().getColor(R.color.edx_brand_primary_base);
        edx_grayscale_neutral_light = context.getResources().getColor(R.color.edx_grayscale_neutral_light);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionThread discussionThread) {
        ViewHolder holder = (ViewHolder) tag;

        {
            Icon icon = FontAwesomeIcons.fa_comments;
            if (discussionThread.getType() == DiscussionThread.ThreadType.QUESTION) {
                icon = discussionThread.isHasEndorsed() ?
                        FontAwesomeIcons.fa_check_square_o : FontAwesomeIcons.fa_question;
            }
            holder.discussionPostTypeIcon.setIcon(icon);
        }

        {
            String threadTitle = discussionThread.getTitle();
            holder.discussionPostTitle.setText(threadTitle);
        }

        holder.discussionPostClosedIcon.setVisibility(discussionThread.isClosed() ? View.VISIBLE : View.GONE);
        holder.discussionPostPinIcon.setVisibility(discussionThread.isPinned() ? View.VISIBLE : View.GONE);
        holder.discussionPostFollowIcon.setVisibility(discussionThread.isFollowing() ? View.VISIBLE : View.GONE);

        {
            String authorLabel = discussionThread.getAuthorLabel();
            if (authorLabel != null) {
                holder.discussionPostAuthor.setVisibility(View.VISIBLE);
                holder.discussionPostAuthor.setText(getContext().getString(R.string.discussion_priviledged_author_attribution, authorLabel));

            } else {
                holder.discussionPostAuthor.setVisibility(View.GONE);
            }
        }

        {
            final String text;
            final Icon icon;
            @ColorInt
            final int indicatorColor;
            if (voteCountsEnabled) {
                text = Integer.toString(discussionThread.getVoteCount());
                icon = FontAwesomeIcons.fa_plus;
                indicatorColor = discussionThread.isVoted() ? edx_brand_primary_base : edx_grayscale_neutral_light;
            } else {
                text = Integer.toString(discussionThread.getCommentCount());
                icon = FontAwesomeIcons.fa_comment;
                indicatorColor = discussionThread.getUnreadCommentCount() == 0 ? edx_grayscale_neutral_light : edx_brand_primary_base;
            }
            holder.discussionPostIndicatorTextView.setText(text);
            holder.discussionPostIndicatorTextView.setTextColor(indicatorColor);

            final IconDrawable iconDrawable = new IconDrawable(getContext(), icon).sizePx((int) holder.discussionPostIndicatorTextView.getTextSize()).color(indicatorColor);
            if (voteCountsEnabled) {
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        holder.discussionPostIndicatorTextView, iconDrawable, null, null, null);
            } else {
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        holder.discussionPostIndicatorTextView, null, null, iconDrawable, null);
            }

        }
        holder.discussionPostRow.setBackgroundResource(discussionThread.isRead() ? R.drawable.bg_discussion_thread_read : R.drawable.bg_discussion_thread_unread);
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new ViewHolder(convertView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    public void setVoteCountsEnabled(boolean voteCountsEnabled) {
        this.voteCountsEnabled = voteCountsEnabled;
    }

    private static class ViewHolder extends BaseViewHolder {
        final View discussionPostRow;
        final IconImageView discussionPostTypeIcon;
        final TextView discussionPostTitle;
        final IconImageView discussionPostClosedIcon;
        final IconImageView discussionPostPinIcon;
        final IconImageView discussionPostFollowIcon;
        final TextView discussionPostAuthor;
        final TextView discussionPostIndicatorTextView;

        public ViewHolder(View convertView) {
            discussionPostRow = convertView;
            discussionPostTypeIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_type_icon);
            discussionPostTitle = (TextView) convertView.findViewById(R.id.discussion_post_title);
            discussionPostClosedIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_closed_icon);
            discussionPostPinIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_pin_icon);
            discussionPostFollowIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_following_icon);
            discussionPostAuthor = (TextView) convertView.findViewById(R.id.discussion_post_author);
            discussionPostIndicatorTextView = (TextView) convertView.findViewById(R.id.discussion_post_indicator_text);
        }

    }
}
