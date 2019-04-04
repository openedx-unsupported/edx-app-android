package org.edx.mobile.tta.ui.interfaces;

import org.edx.mobile.discussion.DiscussionComment;

public interface DiscussionCommentClickListener {

    void onClickLike(DiscussionComment comment);
    void onClickReply(DiscussionComment comment);
    void onClickUser(DiscussionComment comment);
    void onClickDefault(DiscussionComment comment);

}
