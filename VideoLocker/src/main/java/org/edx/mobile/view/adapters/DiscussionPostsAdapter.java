package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.discussion.PinnedAuthor;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.third_party.iconify.Iconify;

public class DiscussionPostsAdapter extends BaseListAdapter<DiscussionThread> {

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_thread, environment);
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionThread discussionThread) {
        ViewHolder holder = (ViewHolder) tag;

        Iconify.IconValue iconValue = Iconify.IconValue.fa_comments;
        if (discussionThread.getType() == DiscussionThread.ThreadType.QUESTION) {
            iconValue = discussionThread.isHasEndorsed() ?
                    Iconify.IconValue.fa_check_square_o : Iconify.IconValue.fa_question;
        }
        holder.discussionPostTypeIcon.setIcon(iconValue);

        String threadTitle = discussionThread.getTitle();
        holder.discussionPostTitle.setText(threadTitle);

        if (discussionThread.getAuthorLabel() != null) {
            holder.discussionPostPinTextView.setVisibility(View.VISIBLE);
            String pinFollowTextLabel = "";
            if (discussionThread.getAuthorLabel() == PinnedAuthor.STAFF) {
                pinFollowTextLabel = getContext().getString(R.string.discussion_priviledged_author_label_staff);

            } else if (discussionThread.getAuthorLabel() == PinnedAuthor.COMMUNITY_TA) {
                pinFollowTextLabel = getContext().getString(R.string.discussion_priviledged_author_label_ta);
            }
            final Drawable icon = discussionThread.isPinned() ?
                    new IconDrawable(getContext(), Iconify.IconValue.fa_thumb_tack).colorRes(R.color.edx_grayscale_neutral_base).sizeRes(R.dimen.edx_x_small)
                    : null;
            holder.discussionPostPinTextView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            holder.discussionPostPinTextView.setText(pinFollowTextLabel);

        } else {
            holder.discussionPostPinTextView.setVisibility(View.GONE);
        }

        if (discussionThread.isFollowing()) {
            holder.discussionPostFollowTextView.setVisibility(View.VISIBLE);
            holder.discussionPostFollowTextView.setCompoundDrawablesWithIntrinsicBounds(
                    new IconDrawable(getContext(), Iconify.IconValue.fa_star).colorRes(R.color.edx_grayscale_neutral_base).sizeRes(R.dimen.edx_x_small), null, null, null);
        } else {
            holder.discussionPostFollowTextView.setVisibility(View.GONE);
        }

        final int commentColor = discussionThread.getUnreadCommentCount() == 0 ?
                R.color.edx_grayscale_neutral_light : R.color.edx_brand_primary_base;
        holder.discussionPostNumCommentsTextView.setText(Integer.toString(discussionThread.getCommentCount()));
        holder.discussionPostNumCommentsTextView.setTextColor(getContext().getResources().getColor(commentColor));
        holder.discussionPostCommentIcon.setIcon(Iconify.IconValue.fa_comment);
        holder.discussionPostCommentIcon.setIconColor(getContext().getResources().getColor(commentColor));
        holder.discussionPostRow.setBackgroundColor(getContext().getResources().getColor(
                discussionThread.isRead() ? R.color.edx_grayscale_neutral_xx_light : R.color.edx_grayscale_neutral_white));
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        final ViewHolder holder = new ViewHolder();

        holder.discussionPostRow = (RelativeLayout) convertView.findViewById(R.id.row_discussion_post_relative_layout);
        holder.discussionPostTypeIcon = (IconView) convertView.findViewById(R.id.discussion_post_type_icon);
        holder.discussionPostTitle = (TextView) convertView.findViewById(R.id.discussion_post_title);
        holder.discussionPostPinTextView = (TextView) convertView.findViewById(R.id.discussion_post_pin_text_view);
        holder.discussionPostFollowTextView = (TextView) convertView.findViewById(R.id.discussion_post_following_text_view);
        holder.discussionPostNumCommentsTextView = (TextView) convertView.findViewById(R.id.discussion_post_num_comments_text_view);
        holder.discussionPostCommentIcon = (IconView) convertView.findViewById(R.id.discussion_post_comment_icon);

        return holder;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    private static class ViewHolder extends BaseViewHolder {
        RelativeLayout discussionPostRow;
        IconView discussionPostTypeIcon;
        TextView discussionPostTitle;
        TextView discussionPostPinTextView;
        TextView discussionPostFollowTextView;
        TextView discussionPostNumCommentsTextView;
        IconView discussionPostCommentIcon;

    }
}
