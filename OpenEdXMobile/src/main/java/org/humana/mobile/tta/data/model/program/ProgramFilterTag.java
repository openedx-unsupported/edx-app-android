package org.humana.mobile.tta.data.model.program;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class ProgramFilterTag implements Comparable<ProgramFilterTag> {

    private long id;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("internal_name")
    private String internalName;



    @SerializedName("isSelected")
    private Boolean isSelected;

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

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramFilterTag tag = (ProgramFilterTag) o;
        return id == tag.id &&
                order == tag.order &&
                Objects.equals(displayName, tag.displayName) &&
                Objects.equals(internalName, tag.internalName) &&
                Objects.equals(isSelected, tag.isSelected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, internalName, isSelected, order);
    }
}
