package org.humana.mobile.tta.data.model.program;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SelectedProgramFilters {

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



    public boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
