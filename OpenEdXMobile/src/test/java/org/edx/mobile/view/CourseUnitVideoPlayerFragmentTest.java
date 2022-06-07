package org.edx.mobile.view;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@RunWith(RobolectricTestRunner.class)
public class CourseUnitVideoPlayerFragmentTest extends BaseCourseUnitVideoFragmentTest {

    @Override
    protected BaseCourseUnitVideoFragment getCourseUnitPlayerFragmentInstance() {
        return CourseUnitVideoPlayerFragment.newInstance(getVideoUnit(), false, false);
    }
}
