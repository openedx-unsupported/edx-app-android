package org.edx.mobile.test;

import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.mocked.MockedCourseOutlineProvider;
import org.edx.mobile.services.CourseManager;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by hanning on 4/29/15.
 */
public class CourseManagerTest {

    @Test
    public void testGetDisplayVersionName() throws Exception {
        MockedCourseOutlineProvider provider = new MockedCourseOutlineProvider();
        String courseOutline = provider.getCourseOutline();
        ICourse course = CourseManager.fromEnrollment(courseOutline, "a-course-id");
        assertTrue("get chapters", course.getChapters().size() != 0 );
        ISequential sequential = (ISequential) course.getChapters().get(0);
        assertTrue("get getVerticals", sequential.getVerticals().size() != 0 );
    }



}
