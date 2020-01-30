package org.humana.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "periodDesc")
public class DownloadPeriodDesc {
    @PrimaryKey
    private long id;

    private String about_url;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAbout_url() {
        return about_url;
    }

    public void setAbout_url(String about_url) {
        this.about_url = about_url;
    }


}
