package org.edx.mobile.view;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.view.custom.DisableableViewPager;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.android.api.Assertions.assertThat;
import static org.junit.Assert.*;

// I don't understand the data model cycle behind this, and I couldn't run
// the demo sandbox to see this Activity, so I'm not writing tests for the
// adapter interactions yet
public class CourseUnitNavigationActivityTest extends CourseBaseActivityTest {
    /**
     * Method for defining the subclass of {@link CourseUnitNavigationActivity}
     * that is being tested. Should be overridden by subclasses.
     *
     * @return The {@link CourseUnitNavigationActivity} subclass that is being
     * tested
     */
    @Override
    protected Class<? extends CourseUnitNavigationActivity> getActivityClass() {
        return CourseUnitNavigationActivity.class;
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
        List<CourseComponent> leafComponents = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(leafComponents,
                EnumSet.allOf(BlockType.class));
        CourseComponent courseUnit = leafComponents.get(0);
        courseComponent = courseUnit.getParent();
        if (courseUnit.getPath().getPath().size() % 2 > 0) {
            courseComponent = courseComponent.getParent();
        }
        Intent intent = super.getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
        extras.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponent.getId());
        extras.putSerializable(Router.EXTRA_COURSE_UNIT, courseUnit);
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

        ActivityController<? extends CourseUnitNavigationActivity> controller =
                Robolectric.buildActivity(getActivityClass()).withIntent(getIntent());
        CourseUnitNavigationActivity activity = controller.get();

        controller.create();
        assertNotNull(activity.findViewById(R.id.course_unit_nav_bar));
        assertNotNull(activity.findViewById(R.id.goto_prev));
        assertNotNull(activity.findViewById(R.id.goto_next));
    }

    /**
     * Generic method for asserting proper setup for the current orientation
     *
     * @param activity The current activity
     */
    private void assertOrientationSetup(CourseUnitNavigationActivity activity) {
        boolean isLandscape = activity.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        ActionBar bar = activity.getActionBar();
        if (bar != null) {
            assertEquals(!isLandscape, bar.isShowing());
        }
        View courseUnitNavBar = activity.findViewById(R.id.course_unit_nav_bar);
        assertNotNull(courseUnitNavBar);
        if (isLandscape) {
            assertThat(courseUnitNavBar).isNotVisible();
        } else {
            assertThat(courseUnitNavBar).isVisible();
        }
        View pagerView = activity.findViewById(R.id.pager);
        assertNotNull(pagerView);
        assertThat(pagerView).isInstanceOf(DisableableViewPager.class);
        assertEquals(!isLandscape, ((DisableableViewPager) pagerView).isPagingEnabled());
    }

    /**
     * Generic method for testing the setup for the specified orientation
     *
     * @param orientation The orientation to be tested
     */
    public void testOrientationSetup(int orientation) {
        ActivityController<? extends CourseUnitNavigationActivity> controller =
                Robolectric.buildActivity(getActivityClass()).withIntent(getIntent());
        CourseUnitNavigationActivity activity = controller.get();
        activity.getResources().getConfiguration().orientation = orientation;
        controller.create();
        assertOrientationSetup(activity);
    }

    /**
     * Testing setup for different orientations
     */
    @Test
    public void orientationsSetupTest() {
        testOrientationSetup(Configuration.ORIENTATION_PORTRAIT);
        testOrientationSetup(Configuration.ORIENTATION_LANDSCAPE);
    }

    /**
     * Generic method for testing setup on orientation changes
     *
     * @param activity The current activity
     * @param orientation The orientation change to test
     */
    private void testOrientationChange(
            CourseUnitNavigationActivity activity, int orientation) {
        Configuration config = activity.getResources().getConfiguration();
        assertNotEquals(orientation, config.orientation);
        config.orientation = orientation;
        activity.onConfigurationChanged(config);
        assertOrientationSetup(activity);
    }

    /**
     * Testing orientation changes
     */
    @Test
    public void orientationChangeTest() {
        CourseUnitNavigationActivity activity =
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).setup().get();
        assertNotEquals(Configuration.ORIENTATION_LANDSCAPE,
                activity.getResources().getConfiguration().orientation);
        assertOrientationSetup(activity);

        testOrientationChange(activity, Configuration.ORIENTATION_LANDSCAPE);
        testOrientationChange(activity, Configuration.ORIENTATION_PORTRAIT);
    }

    /**
     * Ignoring functionality testing upon receiving a DownloadEvent, as
     * {@link CourseUnitNavigationActivity} also overrides and ignores it
     */
    @Override
    @Ignore
    @Test
    public void downloadEventTest() {}
}
