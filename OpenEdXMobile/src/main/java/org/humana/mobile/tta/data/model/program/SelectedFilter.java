package org.humana.mobile.tta.data.model.program;

import androidx.annotation.Nullable;

import java.util.Objects;

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


    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectedFilter filter = (SelectedFilter) o;
        return selected_tag.equals(filter.selected_tag) &&
                selected_tag_item == filter.selected_tag_item;
    }
}
