package org.edx.mobile.tta.analytics;

import org.edx.mobile.tta.analytics.analytics_enums.Status;

/**
 * Created by JARVICE on 19-01-2018.
 */

public class AnalyticModel {
    public String user_id;
    //primary key in Db
    public String analytic_id;
    public String action;
    public String source;
    public String metadata;
    public String page;
    public Long event_timestamp;
    public int version=2;// 2 means new TTA Redesign analytics

    public String nav;
    public String action_id;

    //its not a API Field //Having it for local storage.
    public Status status;

    public String getUser_Id() {
        if (user_id == null || user_id.trim().length() == 0) {
            return "";
        }
        return user_id;
    }
    public String getAction() {
        if (action == null || action.trim().length() == 0) {
            return "";
        }
        return action;
    }
    public String getMetadata() {
        if (metadata == null || metadata.trim().length() == 0) {
            return "";
        }
        return metadata;
    }
    public String getPage() {
        if (page == null || page.trim().length() == 0) {
            return "";
        }
        return page;
    }

    public void setEvent_date() {
        event_timestamp = System.currentTimeMillis();
    }

    public Long getEvent_timestamp() {
       return event_timestamp;
    }

    public String getNav() {
        if (nav == null || nav.trim().length() == 0) {
            return "";
        }
        return nav;
    }

    public String getAction_id() {
        if (action_id == null || action_id.trim().length() == 0) {
            return "";
        }
        return action_id;
    }

    public int getStatus() {
        if (status.OFFLINE == Status.OFFLINE)
            return 0;
        else
            return 1;
    }

    public void setStatus(int value) {
        if (value==0)
           status=Status.OFFLINE;
        else
            status=Status.LIVE;
    }
}
