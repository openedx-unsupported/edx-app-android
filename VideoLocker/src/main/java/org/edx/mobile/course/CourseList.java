package org.edx.mobile.course;

import java.util.List;

public class CourseList {

    public Pagination pagination;
    public List<CourseDetail> results;

    public static class Pagination {
        public int count;
        public int num_pages;
        public String next;
        public String previous;
    }
}
