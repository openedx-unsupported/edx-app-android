package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.edx.mobile.tta.data.model.feed.FeedMetadata;

@Entity(tableName = "feed")
public class Feed {

    @PrimaryKey
    private long id;

    private String action;

    private String action_by;

    private String action_on;

    private String created_at;

    private String modified_at;

    @Embedded(prefix = "meta_data_")
    private FeedMetadata meta_data;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction_by() {
        return action_by;
    }

    public void setAction_by(String action_by) {
        this.action_by = action_by;
    }

    public String getAction_on() {
        return action_on;
    }

    public void setAction_on(String action_on) {
        this.action_on = action_on;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getModified_at() {
        return modified_at;
    }

    public void setModified_at(String modified_at) {
        this.modified_at = modified_at;
    }

    public FeedMetadata getMeta_data() {
        return meta_data;
    }

    public void setMeta_data(FeedMetadata meta_data) {
        this.meta_data = meta_data;
    }
}
