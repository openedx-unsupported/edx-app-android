package org.edx.mobile.tta.event;

import org.edx.mobile.model.api.EnrolledCoursesResponse;

public class CourseEnrolledEvent {

    private EnrolledCoursesResponse course;

    public CourseEnrolledEvent(EnrolledCoursesResponse course) {
        this.course = course;
    }

    public EnrolledCoursesResponse getCourse() {
        return course;
    }

    public void setCourse(EnrolledCoursesResponse course) {
        this.course = course;
    }
}
