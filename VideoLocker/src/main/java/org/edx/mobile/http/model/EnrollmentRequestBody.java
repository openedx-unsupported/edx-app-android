package org.edx.mobile.http.model;


import java.io.Serializable;

public class EnrollmentRequestBody implements Serializable{
    public CourseIdObject course_details;

    public static class LastAccessRequestBody implements Serializable{
        public String last_visited_module_id;
        public String modification_date;
    }
}
