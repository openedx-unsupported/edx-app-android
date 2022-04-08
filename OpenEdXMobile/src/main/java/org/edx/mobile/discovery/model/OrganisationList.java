package org.edx.mobile.discovery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrganisationList {
    public List<OrganisationModel> getResults() {
        return results;
    }

    public void setResults(List<OrganisationModel> results) {
        this.results = results;
    }

    @SerializedName("results")
    @Expose
    private List<OrganisationModel> results;
}
