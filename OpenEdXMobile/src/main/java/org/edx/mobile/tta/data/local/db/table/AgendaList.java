package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.edx.mobile.tta.data.model.AgendaItem;

import java.util.List;

@Entity(tableName = "agenda_list")
public class AgendaList {

    @PrimaryKey
    private String list_id;

    private String list_name;

    @Ignore
    private List<AgendaItem> result;

    public String getList_id() {
        return list_id;
    }

    public void setList_id(String list_id) {
        this.list_id = list_id;
    }

    public String getList_name() {
        return list_name;
    }

    public void setList_name(String list_name) {
        this.list_name = list_name;
    }

    public List<AgendaItem> getResult() {
        return result;
    }

    public void setResult(List<AgendaItem> result) {
        this.result = result;
    }
}
