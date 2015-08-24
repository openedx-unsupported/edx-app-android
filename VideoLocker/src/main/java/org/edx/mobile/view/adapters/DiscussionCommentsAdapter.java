package org.edx.mobile.view.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.APICallback;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;
import com.qualcomm.qlearn.sdk.discussion.DiscussionComment;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.task.FlagCommentTask;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.third_party.iconify.Iconify;

public class DiscussionCommentsAdapter extends BaseListAdapter <DiscussionComment> {

    private final Context context;
    private FlagCommentTask flagCommentTask;

    @Inject
    public DiscussionCommentsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_comment, environment);
        this.context = context;
    }

    @Override
    public void render(BaseViewHolder tag, final DiscussionComment discussionComment) {
        ViewHolder holder = (ViewHolder) tag;

        String commentBody = discussionComment.getRawBody();
        holder.discussionCommentBody.setText(commentBody);

        int childrenSize = discussionComment.getChildren().size();
        Iconify.IconValue icon = childrenSize == 0 ? Iconify.IconValue.fa_flag : Iconify.IconValue.fa_comment; // show flag icon for comment, and comment icon for response
        holder.discussionCommentCountReportIcon.setIcon(icon);

        holder.discussionCommentAuthorTextView.setText(discussionComment.getAuthor());

        CharSequence formattedDate = DateUtils.getRelativeTimeSpanString(
                discussionComment.getCreatedAt().getTime(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE);

        holder.discussionCommentDateTextView.setText(formattedDate);
        // TODO: localization
        String report = discussionComment.isAbuseFlagged() ? "Reported" : "Report";
        holder.discussionCommentCountReportTextView.setText(childrenSize == 0 ? report : (childrenSize + " comment" + (childrenSize == 1 ? "" : "s")));

        holder.discussionCommentCountReportTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                flagComment(v, discussionComment);
            }
        });
    }


    protected void flagComment(final View v, final DiscussionComment discussionComment) {

        if ( flagCommentTask != null ){
            flagCommentTask.cancel(true);
        }
        flagCommentTask = new FlagCommentTask(getContext(), discussionComment, true) {
            @Override
            public void onSuccess(DiscussionComment comment) {
                // TODO: change Report to Reported and the flag icon color to red?
                TextView reportTextView = (TextView) v;
                reportTextView.setText("Reported");
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                //  hideProgress();

            }
        };
        flagCommentTask.execute();

    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();

        holder.discussionCommentRow = (LinearLayout) convertView.findViewById(R.id.row_discussion_comment_linear_layout);
        holder.discussionCommentBody = (TextView) convertView.findViewById(R.id.discussion_comment_body);
        holder.discussionCommentAuthorTextView = (TextView) convertView.findViewById(R.id.discussion_comment_author_text_view);
        holder.discussionCommentDateTextView = (TextView) convertView.findViewById(R.id.discussion_comment_date_text_view);
        holder.discussionCommentCountReportIcon = (IconView) convertView.findViewById(R.id.discussion_comment_count_report_icon);
        holder.discussionCommentCountReportTextView = (TextView) convertView.findViewById(R.id.discussion_comment_count_report_text_view);

        return holder;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {}

    private static class ViewHolder extends BaseViewHolder {
        LinearLayout discussionCommentRow;
        TextView discussionCommentBody;
        TextView discussionCommentAuthorTextView;
        TextView discussionCommentDateTextView;
        IconView discussionCommentCountReportIcon;
        TextView discussionCommentCountReportTextView;

    }

}