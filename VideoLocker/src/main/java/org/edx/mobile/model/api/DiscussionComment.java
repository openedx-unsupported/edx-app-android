package org.edx.mobile.model.api;

import java.util.Date;
import java.util.List;

/**
 * Created by jakelim on 6/2/15.
 */
public class DiscussionComment {
    String identifier;
    String parentId;
    String threadId;
    String rawBody;
    String renderedBody;
    String author;
    String authorLabel;
    boolean flagged = false;
    boolean voted = false;
    int voteCount = 0;
    Date createdAt;
    Date updatedAt;
    boolean endorsed = false;
    String endorsedBy;
    String endorsedByLabel;
    Date endorsedAt;
    boolean abuseFlagged = false;
    String editableFields;
    List<DiscussionComment> children;
}
