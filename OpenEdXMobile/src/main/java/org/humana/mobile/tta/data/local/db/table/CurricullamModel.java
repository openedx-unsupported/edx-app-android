package org.humana.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;


import java.util.List;

public class CurricullamModel {

    @SerializedName("tab_title")
    private String tab_title;

    @SerializedName("id")
    private String id;

    private List<CurricullamChaptersModel> chapters;

    public String getTab_title() {
        return tab_title;
    }

    public void setTab_title(String tab_title) {
        this.tab_title = tab_title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CurricullamChaptersModel> getChapters() {
        return chapters;
    }

    public void setChapters(List<CurricullamChaptersModel> chapters) {
        this.chapters = chapters;
    }
}
