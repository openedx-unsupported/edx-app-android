package org.edx.mobile.test;

import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.services.CourseManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO - we will create a separate PR for course manager unit test.
  */
@Ignore
public class CourseManagerTest {
    CourseStructureV1Model model;
    @Before
    public  void setUp() throws Exception {
    }

    @Test
    public void testNormalizeCourseStructure() throws Exception {
        IBlock block = CourseManager.normalizeCourseStructure(model, "");
    }



}
