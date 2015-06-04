package org.edx.mobile.model.api;

import java.util.Date;

/**
 * Created by jakelim on 6/2/15.
 */
public class DiscussionThread {
    String identifier;
    String type;
    String courseId;
    String topicId;
    String groupId;
    String groupName;
    String title;
    String rawBody;
    String renderedBody;
    String author;
    String authorLabel;
    String commentListUrl;
    boolean hasEndorsed = false;
    boolean pinned = false;
    boolean closed = false;
    boolean following = false;
    boolean abuseFlagged = false;
    boolean voted = false;
    int voteCount = 0;
    Date createdAt;
    Date updatedAt;
    String editableFields;
}
