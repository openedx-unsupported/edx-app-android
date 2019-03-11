package org.edx.mobile.tta.data.model.agenda;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AgendaList {

    private String level;

    private List<AgendaItem> result;

    private long list_id;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<AgendaItem> getResult() {
        return result;
    }

    public void setResult(List<AgendaItem> result) {
        this.result = result;
    }

    public long getList_id() {
        return list_id;
    }

    public void setList_id(long list_id) {
        this.list_id = list_id;
    }
}
