package org.edx.mobile.discovery.model;

import java.util.List;

public class EnrollAndUnenrollData {
    private DataCreation data;

    public DataCreation getData() {
        return data;
    }

    public void setData(DataCreation data) {
        this.data = data;
    }

    public static class DataCreation {
        private String courses;

        public String getProgram_uuid() {
            return program_uuid;
        }

        public void setProgram_uuid(String program_uuid) {
            this.program_uuid = program_uuid;
        }

        private String program_uuid;
        private String action;
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getCourses() {
            return courses;
        }

        public void setCourses(String courses) {
            this.courses = courses;
        }
    }
}

