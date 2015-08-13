package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class CourseOutlineActivityTest extends CourseBaseActivityTest {
    /**
     * Method for defining the subclass of {@link CourseOutlineActivity} that
     * is being tested. Should be overridden by subclasses.
     *
     * @return The {@link CourseOutlineActivity} subclass that is being tested
     */
    @Override
    protected Class<? extends CourseOutlineActivity> getActivityClass() {
        return CourseOutlineActivity.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Intent getIntent() {
        EnrolledCoursesResponse courseData;
        CourseComponent courseComponent;
        try {
            courseData = api.getEnrolledCourses().get(0);
            courseComponent = serviceManager.getCourseStructure(
                    courseData.getCourse().getId(),
                    OkHttpUtil.REQUEST_CACHE_TYPE.IGNORE_CACHE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Intent intent = super.getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
        extras.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponent.getId());
        intent.putExtra(Router.EXTRA_BUNDLE, extras);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean appliesPrevTransitionOnRestart() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void initializeTest() {
        super.initializeTest();

        Intent intent = getIntent();
        ActivityController<? extends CourseOutlineActivity> controller =
                Robolectric.buildActivity(getActivityClass()).withIntent(intent);
        CourseOutlineActivity activity = controller.get();

        controller.create(null).postCreate(null);
        Fragment fragment = activity.getSupportFragmentManager()
                .findFragmentByTag(CourseOutlineFragment.TAG);
        assertNotNull(fragment);
        assertThat(fragment).isInstanceOf(CourseOutlineFragment.class);
        assertTrue(fragment.getRetainInstance());
        Bundle args = fragment.getArguments();
        assertNotNull(args);
        Bundle data = intent.getBundleExtra(Router.EXTRA_BUNDLE);
        assertEquals(data.getSerializable(Router.EXTRA_ENROLLMENT),
                args.getSerializable(Router.EXTRA_ENROLLMENT));
        assertEquals(data.getString(Router.EXTRA_COURSE_COMPONENT_ID),
                args.getString(Router.EXTRA_COURSE_COMPONENT_ID));
    }
}
