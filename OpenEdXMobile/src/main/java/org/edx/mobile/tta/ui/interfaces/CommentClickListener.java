package org.edx.mobile.tta.ui.interfaces;

import org.edx.mobile.tta.wordpress_client.model.Comment;

public interface CommentClickListener {

    void onClickUser(Comment comment);
    void onClickLike(Comment comment);
    void onClickReply(Comment comment);
    void onClickDefault(Comment comment);

}
