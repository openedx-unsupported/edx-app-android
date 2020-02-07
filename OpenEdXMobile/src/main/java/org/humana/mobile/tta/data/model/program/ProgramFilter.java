package org.humana.mobile.tta.data.model.program;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class ProgramFilter implements Comparable<ProgramFilter> {

    private String id;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("internal_name")
    private String internalName;

    @SerializedName("isSelected")
    private boolean isSelected;

    @SerializedName("show_in")
    private List<String> showIn;

    private long order;

    private List<ProgramFilterTag> tags;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public List<String> getShowIn() {
        return showIn;
    }

    public void setShowIn(List<String> showIn) {
        this.showIn = showIn;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public List<ProgramFilterTag> getTags() {
        return tags;
    }

    public void setTags(List<ProgramFilterTag> tags) {
        this.tags = tags;
    }

    @Override
    public int compareTo(ProgramFilter o) {
        return Long.compare(order, o.order);
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramFilter filter = (ProgramFilter) o;
        return id.equals(filter.id) &&
                order == filter.order &&
                Objects.equals(displayName, filter.displayName) &&
                Objects.equals(internalName, filter.internalName);
    }
}
