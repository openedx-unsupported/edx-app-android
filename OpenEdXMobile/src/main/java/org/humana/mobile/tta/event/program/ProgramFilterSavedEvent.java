package org.humana.mobile.tta.event.program;

import org.humana.mobile.tta.data.model.program.ProgramFilter;

import java.util.List;

public class ProgramFilterSavedEvent {


    private List<ProgramFilter> programFilters;

    public ProgramFilterSavedEvent(List<ProgramFilter> programFilters) {
        this.programFilters = programFilters;
    }


    public List<ProgramFilter> getProgramFilters() {
        return programFilters;
    }

    public void setProgramFilters(List<ProgramFilter> programFilters) {
        this.programFilters = programFilters;
    }
}
