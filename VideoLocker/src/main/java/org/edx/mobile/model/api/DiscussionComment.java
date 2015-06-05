package org.edx.mobile.model.api;

import org.edx.mobile.util.DateUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public DiscussionComment(JSONObject jsonObj) throws JSONException {
        identifier = jsonObj.getString("id");
        parentId = jsonObj.optString("parent_id");
        threadId = jsonObj.optString("thread_id");
        rawBody = jsonObj.optString("raw_body");
        renderedBody = jsonObj.optString("rendered_body");
        author = jsonObj.optString("author");
        authorLabel = jsonObj.optString("author_label");
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
        endorsed = jsonObj.optBoolean("endorsed");
        endorsedBy = jsonObj.optString("endorsed_by");
        endorsedByLabel = jsonObj.optString("endorsed_by_label");
        dateStr = jsonObj.optString("endorsed_at");
        if (dateStr != null) {
            endorsedAt = DateUtil.convertToDate(dateStr);
        }
        abuseFlagged = jsonObj.optBoolean("abuse_flagged");
        editableFields = jsonObj.optString("editable_fields");
        JSONArray jsonArray = jsonObj.optJSONArray("children");
        if (jsonArray != null) {
            ArrayList<DiscussionComment> childrenArray = new ArrayList<DiscussionComment>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject childObj = jsonArray.getJSONObject(i);
                DiscussionComment child = new DiscussionComment(childObj);
                childrenArray.add(child);
            }
            children = childrenArray;
        }
    }
}
