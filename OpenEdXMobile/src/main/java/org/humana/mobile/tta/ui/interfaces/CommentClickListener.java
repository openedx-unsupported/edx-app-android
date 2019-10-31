package org.humana.mobile.tta.ui.interfaces;

import org.humana.mobile.tta.wordpress_client.model.Comment;

public interface CommentClickListener {

    void onClickUser(Comment comment);
    void onClickLike(Comment comment);
    void onClickReply(Comment comment);
    void onClickDefault(Comment comment);

}
