package com.qualcomm.qlearn.sdk.discussion;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.List;

/**
 * Created by jakelim on 6/16/15.
 */
public class DiscussionTopic {
    @SerializedName("id") String identifier;
    String name;
    String threadListUrl;
    List<DiscussionTopic> children;

    public static DiscussionTopic fromJsonObject(JsonObject jsonObj) throws JSONException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        DiscussionTopic topic = gson.fromJson(jsonObj, DiscussionTopic.class);
        return topic;
    }
}
