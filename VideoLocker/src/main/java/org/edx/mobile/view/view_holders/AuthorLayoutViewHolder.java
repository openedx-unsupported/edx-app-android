package org.edx.mobile.view.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.view.custom.ETextView;

public class AuthorLayoutViewHolder extends RecyclerView.ViewHolder {

    public ETextView discussionAuthorTextView;
    public ETextView discussionAuthorCreatedAtTextView;
    public ETextView discussionAuthorPrivilegedAuthorTextView;

    public AuthorLayoutViewHolder(View itemView) {
        super(itemView);

        discussionAuthorTextView = (ETextView) itemView.
                findViewById(R.id.discussion_author_layout_author_text_view);
        discussionAuthorCreatedAtTextView = (ETextView) itemView.
                findViewById(R.id.discussion_author_layout_created_at_text_view);
        discussionAuthorPrivilegedAuthorTextView = (ETextView) itemView.
                findViewById(R.id.discussion_author_layout_privileged_author_text_view);
    }
}
