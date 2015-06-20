package com.qualcomm.qlearn.sdk.discussion;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;

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

    public static DiscussionThread fromJsonObject(JsonObject jsonObj) throws JSONException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        DiscussionThread thread = gson.fromJson(jsonObj, DiscussionThread.class);
        return thread;
    }
}
