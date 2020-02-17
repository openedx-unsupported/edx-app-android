package org.humana.mobile.tta.event.program;

import org.humana.mobile.tta.data.model.program.ProgramFilter;

import java.util.List;

public class ProgramFilterSavedEvent {


    private List<ProgramFilter> programFilters;


    private Boolean isFetchFilters;

    public ProgramFilterSavedEvent(List<ProgramFilter> programFilters, Boolean isFetchFilters) {
        this.programFilters = programFilters;
        this.isFetchFilters = isFetchFilters;
    }


    public List<ProgramFilter> getProgramFilters() {
        return programFilters;
    }

    public void setProgramFilters(List<ProgramFilter> programFilters) {
        this.programFilters = programFilters;
    }
    public Boolean getFetchFilters() {
        return isFetchFilters;
    }

    public void setFetchFilters(Boolean fetchFilters) {
        isFetchFilters = fetchFilters;
    }

}
