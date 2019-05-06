package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "content_status")
public class ContentStatus {

    @PrimaryKey
    private long id;

    @SerializedName("content")
    private long content_id;

    @SerializedName("source_key")
    private String source_identity;

    private String username;

    private String started;

    private String ready_for_certificate;

    private String completed;

    private String error;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContent_id() {
        return content_id;
    }

    public void setContent_id(long content_id) {
        this.content_id = content_id;
    }

    public String getSource_identity() {
        return source_identity;
    }

    public void setSource_identity(String source_identity) {
        this.source_identity = source_identity;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getReady_for_certificate() {
        return ready_for_certificate;
    }

    public void setReady_for_certificate(String ready_for_certificate) {
        this.ready_for_certificate = ready_for_certificate;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof ContentStatus && (((ContentStatus) obj).id == id);
    }
}
