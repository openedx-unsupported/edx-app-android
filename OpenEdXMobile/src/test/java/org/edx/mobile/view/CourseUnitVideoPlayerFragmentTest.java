package org.edx.mobile.view;

public class CourseUnitVideoPlayerFragmentTest extends BaseCourseUnitVideoFragmentTest {

    @Override
    protected BaseCourseUnitVideoFragment getCourseUnitPlayerFragmentInstance() {
        return CourseUnitVideoPlayerFragment.newInstance(getVideoUnit(), false, false);
    }
}
