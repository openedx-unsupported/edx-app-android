package org.edx.mobile.programs;

public class MyProgramListModel {
    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getProgramUUid() {
        return programUUid;
    }

    public void setProgramUUid(String programUUid) {
        this.programUUid = programUUid;
    }

    private String programName;
    private String tagName;
    private String programUUid;

    public ResumePrograms getResume_program() {
        return resume_program;
    }

    public void setResume_program(ResumePrograms resume_program) {
        this.resume_program = resume_program;
    }

    private ResumePrograms resume_program;

}
