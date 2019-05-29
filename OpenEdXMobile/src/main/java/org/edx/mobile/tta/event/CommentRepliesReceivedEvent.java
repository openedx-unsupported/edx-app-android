package org.edx.mobile.tta.event;

import org.edx.mobile.tta.wordpress_client.model.Comment;

public class CommentRepliesReceivedEvent {

    private Comment comment;

    public CommentRepliesReceivedEvent(Comment comment) {
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
