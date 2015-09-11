package org.edx.mobile.view.view_holders;

import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.IAuthorData;
import org.edx.mobile.view.custom.ETextView;

public class AuthorLayoutViewHolder {

    public final ETextView discussionAuthorTextView;

    public AuthorLayoutViewHolder(View itemView) {
        discussionAuthorTextView = (ETextView) itemView.
                findViewById(R.id.discussion_author_layout_author_text_view);
    }
}
