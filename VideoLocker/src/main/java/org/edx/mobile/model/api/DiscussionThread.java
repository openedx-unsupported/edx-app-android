package org.edx.mobile.model.api;

import org.edx.mobile.util.DateUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by jakelim on 6/2/15.
 */
public class DiscussionThread {
    String identifier;
    String type;
    String courseId;
    String topicId;
    int groupId;
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

    public DiscussionThread(JSONObject jsonObj) throws JSONException {
        identifier = jsonObj.getString("id");
        type = jsonObj.optString("type");
        courseId = jsonObj.optString("course_id");
        topicId = jsonObj.optString("topic_id");
        groupId = jsonObj.optInt("group_id");
        groupName = jsonObj.optString("group_name");
        title = jsonObj.optString("title");
        rawBody = jsonObj.optString("raw_body");
        renderedBody = jsonObj.optString("rendered_body");
        author = jsonObj.optString("author");
        authorLabel = jsonObj.optString("author_label");
        commentListUrl = jsonObj.optString("comment_list_url");
        hasEndorsed = jsonObj.optBoolean("has_endorsed");
        pinned = jsonObj.optBoolean("pinned");
        closed = jsonObj.optBoolean("closed");
        following = jsonObj.optBoolean("following");
        abuseFlagged = jsonObj.optBoolean("abuse_flagged");
        voted = jsonObj.optBoolean("voted");
        voteCount = jsonObj.optInt("vote_count");
        String dateStr = jsonObj.optString("created_at");
        if (dateStr != null) {
            createdAt = DateUtil.convertToDate(dateStr);
        }
        dateStr = jsonObj.optString("updated_at");
        if (dateStr != null) {
            updatedAt = DateUtil.convertToDate(dateStr);
        }
        editableFields = jsonObj.optString("editable_fields");
    }
}
