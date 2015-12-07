package org.edx.mobile.course;

public class CourseDetail {
    public String course_id;
    public String name;
    public String number;
    public String org;
    public String description;
    public String start;
    public String start_type;
    public String start_display;
    public String end;
    public String enrollment_start;
    public String enrollment_end;
    public String blocks_url;
    public Media media;

    public static class Media {
        public Image course_image;
    }

    public static class Image {
        public String uri;
    }
}
