package org.edx.mobile.view.common;

import org.edx.mobile.model.course.CourseComponent;

/**
 * Created by hanning on 6/9/15.
 */
public interface RunnableCourseComponent extends Runnable{
    CourseComponent getCourseComponent();
}
