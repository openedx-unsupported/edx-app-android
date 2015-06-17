package com.qualcomm.qlearn.sdk.discussion;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;

import java.util.List;

/**
 * Created by jakelim on 6/16/15.
 */
public class CourseTopics {
    List<DiscussionTopic> coursewareTopics;
    List<DiscussionTopic> nonCoursewareTopics;

    public static CourseTopics fromJsonObject(JsonObject jsonObj) throws JSONException {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        CourseTopics courseTopics = gson.fromJson(jsonObj, CourseTopics.class);
        return courseTopics;
    }
}
