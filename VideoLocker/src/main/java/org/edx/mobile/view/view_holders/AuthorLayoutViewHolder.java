package org.edx.mobile.view.view_holders;

import android.view.View;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.IAuthorData;

public class AuthorLayoutViewHolder {

    public final TextView discussionAuthorTextView;

    public AuthorLayoutViewHolder(View itemView) {
        discussionAuthorTextView = (TextView) itemView.
                findViewById(R.id.discussion_author_layout_author_text_view);
    }
}
