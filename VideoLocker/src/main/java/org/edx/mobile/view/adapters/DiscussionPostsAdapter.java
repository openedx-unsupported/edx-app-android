package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionThread;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.view.Router;

public class DiscussionPostsAdapter extends BaseListAdapter<DiscussionThread> {

    @Inject
    Router router;

    private final Context context;

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_thread, environment);
        this.context = context;
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

        if (discussionThread.isPinned() || discussionThread.isFollowing()) {
            holder.discussionPostPinFollowIcon.setVisibility(View.VISIBLE);
            holder.discussionPostPinFollowTextView.setVisibility(View.VISIBLE);

            Iconify.IconValue pinFollowIcon = discussionThread.isPinned() ?
                    Iconify.IconValue.fa_pinterest : Iconify.IconValue.fa_star;
            holder.discussionPostPinFollowIcon.setIcon(pinFollowIcon);

            String pinFollowTextLabel = "";
            if (discussionThread.isFollowing()) {
                pinFollowTextLabel = context.getString(R.string.discussion_posts_following);

            } else if (discussionThread.getAuthorLabel() == DiscussionThread.PinnedAuthor.STAFF) {
                pinFollowTextLabel = context.getString(R.string.discussion_posts_author_label_staff);

            } else if (discussionThread.getAuthorLabel() == DiscussionThread.PinnedAuthor.COMMUNITY_TA) {
                pinFollowTextLabel = context.getString(R.string.discussion_posts_author_label_ta);
            }
            holder.discussionPostPinFollowTextView.setText(pinFollowTextLabel);

            // If the discussion thread has an extra line, add 25dp to row height
            // TODO: Add the additional height if the title spans 2 lines
            AbsListView.LayoutParams params = (AbsListView.LayoutParams) holder.discussionPostRow.getLayoutParams();
            int boxHeight = context.getResources().getDimensionPixelSize(
                    R.dimen.discussion_post_box_height);
            int extraHeight = context.getResources().getDimensionPixelSize(
                    R.dimen.discussion_post_extra_box_height);
            params.height = boxHeight + extraHeight;
            holder.discussionPostRow.setLayoutParams(params);
        }

        int commentColor = discussionThread.getUnreadCommentCount() == 0 ?
                R.color.edx_grayscale_neutral_light : R.color.edx_brand_primary_base;
        holder.discussionPostNumCommentsTextView.setText(Integer.toString(discussionThread.getCommentCount()));
        holder.discussionPostNumCommentsTextView.setTextColor(context.getResources().getColor(commentColor));
        holder.discussionPostCommentIcon.setIcon(Iconify.IconValue.fa_comment);
        holder.discussionPostCommentIcon.setIconColor(context.getResources().getColor(commentColor));

        // TODO: Read property is not being returned in API to get the thread list
        if (discussionThread.isRead()) {
            holder.discussionPostRow.setBackgroundColor(context.getResources().getColor(
                    R.color.edx_grayscale_neutral_xx_light));
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();

        holder.discussionPostRow = (RelativeLayout) convertView.findViewById(R.id.row_discussion_post_relative_layout);
        holder.discussionPostTypeIcon = (IconView) convertView.findViewById(R.id.discussion_post_type_icon);
        holder.discussionPostTitle = (TextView) convertView.findViewById(R.id.discussion_post_title);
        holder.discussionPostPinFollowIcon = (IconView) convertView.findViewById(R.id.discussion_post_pin_or_following_icon);
        holder.discussionPostPinFollowTextView = (TextView) convertView.findViewById(R.id.discussion_post_pin_or_following_text_view);
        holder.discussionPostNumCommentsTextView = (TextView) convertView.findViewById(R.id.discussion_post_num_comments_text_view);
        holder.discussionPostCommentIcon = (IconView) convertView.findViewById(R.id.discussion_post_comment_icon);

        return holder;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DiscussionThread thread = getItem(position);
        router.showCourseDiscussionResponses(parent.getContext(), thread.getIdentifier());
    }

    private static class ViewHolder extends BaseViewHolder {
        RelativeLayout discussionPostRow;
        IconView discussionPostTypeIcon;
        TextView discussionPostTitle;
        IconView discussionPostPinFollowIcon;
        TextView discussionPostPinFollowTextView;
        TextView discussionPostNumCommentsTextView;
        IconView discussionPostCommentIcon;

    }
}
