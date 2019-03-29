package org.edx.mobile.tta.data.model.search;

import java.util.List;

import androidx.annotation.Nullable;

public class FilterSection implements Comparable<FilterSection> {

    private long id;

    private String name;

    private long order;

    private List<FilterTag> tags;

    private boolean in_profile;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public List<FilterTag> getTags() {
        return tags;
    }

    public void setTags(List<FilterTag> tags) {
        this.tags = tags;
    }

    public boolean isIn_profile() {
        return in_profile;
    }

    public void setIn_profile(boolean in_profile) {
        this.in_profile = in_profile;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof FilterSection) && (id == ((FilterSection) obj).id);
    }

    @Override
    public int compareTo(FilterSection o) {
        return Long.compare(order, o.order);
    }
}
