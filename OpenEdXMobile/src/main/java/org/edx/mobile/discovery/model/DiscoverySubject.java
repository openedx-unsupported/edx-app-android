package org.edx.mobile.discovery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DiscoverySubject {
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }


    @SerializedName("count")
    @Expose
    private Integer count;

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    @SerializedName("next")
    @Expose
    private String next;
    @SerializedName("previous")
    @Expose
    private String previous;

    public List<DiscoverySubjectResult> getResults() {
        return results;
    }

    public void setResults(List<DiscoverySubjectResult> results) {
        this.results = results;
    }

    @SerializedName("results")
    @Expose
    private List<DiscoverySubjectResult> results;

}
