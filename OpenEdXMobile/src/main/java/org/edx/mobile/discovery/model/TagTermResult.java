package org.edx.mobile.discovery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TagTermResult {
    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @SerializedName("term")
    @Expose
    private String term;
    @SerializedName("count")
    @Expose
    private int count;
}
