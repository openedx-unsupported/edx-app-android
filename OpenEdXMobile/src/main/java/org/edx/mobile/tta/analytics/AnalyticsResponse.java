package org.edx.mobile.tta.analytics;

/**
 * Created by manprax on 5/1/17.
 */

public class AnalyticsResponse {
    int id;
    String user;
    String timestamp;
    String metadata;
    String action;
    String user_id;

    public int id() {
        return id;
    }
    public String user() {
        return user;
    }
    public String metadata() {
        return metadata;
    }
    public String action() {
        return action;
    }
    public String timestamp() {
        return timestamp;
    }
    public String user_id() {
        return user_id;
    }
}
