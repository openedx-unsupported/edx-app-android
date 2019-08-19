package org.edx.mobile.tta.data.model.program;

import com.google.gson.annotations.SerializedName;

public class ProgramFilterTag implements Comparable<ProgramFilterTag> {

    private long id;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("internal_name")
    private String internalName;

    private long order;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    @Override
    public int compareTo(ProgramFilterTag o) {
        return Long.compare(order, o.order);
    }
}
