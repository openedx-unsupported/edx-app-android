package org.edx.mobile.discovery.model;

import java.util.List;

public class SeachProgramDetails {
    private List<String> programs;
    private List<String> program_id;

    public List<String> getPrograms() {
        return programs;
    }

    public void setPrograms(List<String> programs) {
        this.programs = programs;
    }

    public List<String> getProgram_id() {
        return program_id;
    }

    public void setProgram_id(List<String> program_id) {
        this.program_id = program_id;
    }

    public List<SearchTags> getTags() {
        return tags;
    }

    public void setTags(List<SearchTags> tags) {
        this.tags = tags;
    }

    private List<SearchTags> tags;
}
