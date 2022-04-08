package org.edx.mobile.discovery.model;

public class SearchResultList {
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private String key;
    private String uuid;
    private SeachProgramDetails program_details;

    public SeachProgramDetails getProgram_details() {
        return program_details;
    }

    public void setProgram_details(SeachProgramDetails program_details) {
        this.program_details = program_details;
    }
}
