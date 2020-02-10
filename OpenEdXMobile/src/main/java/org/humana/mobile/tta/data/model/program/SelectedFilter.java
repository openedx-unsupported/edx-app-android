package org.humana.mobile.tta.data.model.program;

public class SelectedFilter {
    private String internal_name;
    private String display_name;
    private String selected_tag;



    private ProgramFilterTag selected_tag_item;

    public String getInternal_name() {
        return internal_name;
    }

    public void setInternal_name(String internal_name) {
        this.internal_name = internal_name;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getSelected_tag() {
        return selected_tag;
    }

    public void setSelected_tag(String selected_tag) {
        this.selected_tag = selected_tag;
    }

    public ProgramFilterTag getSelected_tag_item() {
        return selected_tag_item;
    }

    public void setSelected_tag_item(ProgramFilterTag selected_tag_item) {
        this.selected_tag_item = selected_tag_item;
    }



}
