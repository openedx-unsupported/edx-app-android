package org.edx.mobile.programs;

import java.util.List;

public class Programs {
    private String program_title;
    private String program_uuid;

    public ResumePrograms getResumePrograms() {
        return resume_program;
    }

    public void setResumePrograms(ResumePrograms resumePrograms) {
        this.resume_program = resumePrograms;
    }

    private ResumePrograms resume_program;

    public String getProgram_title() {
        return program_title;
    }

    public void setProgram_title(String program_title) {
        this.program_title = program_title;
    }

    public String getProgram_uuid() {
        return program_uuid;
    }

    public void setProgram_uuid(String program_uuid) {
        this.program_uuid = program_uuid;
    }

    private List<String> tags;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
