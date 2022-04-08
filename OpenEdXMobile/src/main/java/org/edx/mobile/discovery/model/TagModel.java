package org.edx.mobile.discovery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TagModel {
    @SerializedName("total")
    @Expose
    private int total;
    @SerializedName("other")
    @Expose
    private int other;
    @SerializedName("_type")
    @Expose
    private String _type;
    @SerializedName("missing")
    @Expose
    private int missing;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getOther() {
        return other;
    }

    public void setOther(int other) {
        this.other = other;
    }

    public String get_type() {
        return _type;
    }

    public void set_type(String _type) {
        this._type = _type;
    }

    public int getMissing() {
        return missing;
    }

    public void setMissing(int missing) {
        this.missing = missing;
    }

    public List<TagTermResult> getTerms() {
        return terms;
    }

    public void setTerms(List<TagTermResult> terms) {
        this.terms = terms;
    }

    @SerializedName("terms")
    @Expose
    private List<TagTermResult> terms;
}
