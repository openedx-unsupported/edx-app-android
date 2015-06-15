package com.qualcomm.qlearn.sdk.discussion;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.Date;
import java.util.List;

/**
 * Created by jakelim on 6/2/15.
 */
public class DiscussionComment {
    @SerializedName("id") String identifier;
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

    public static DiscussionComment fromJsonObject(JsonObject jsonObj) throws JSONException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        DiscussionComment comment = gson.fromJson(jsonObj, DiscussionComment.class);
        return comment;
    }
}
