package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import androidx.annotation.Nullable;

import org.edx.mobile.tta.data.model.feed.FeedMetadata;

@Entity(tableName = "feed", primaryKeys = {"id", "username"})
public class Feed implements Comparable<Feed> {

    @NonNull
    private String id;

    private String action;

    private String action_by;

    private String action_on;

    private String created_at;

    private String modified_at;

    @Embedded(prefix = "meta_data_")
    private FeedMetadata meta_data;

    @NonNull
    private String username;

    private String state;

    private boolean have_read;

    private double order;

    private long count;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isHave_read() {
        return have_read;
    }

    public void setHave_read(boolean have_read) {
        this.have_read = have_read;
    }

    public double getOrder() {
        return order;
    }

    public void setOrder(double order) {
        this.order = order;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Feed && (((Feed) obj).id.equals(id));
    }

    @Override
    public int compareTo(Feed o) {
        return (int) (order - o.order);
    }
}
