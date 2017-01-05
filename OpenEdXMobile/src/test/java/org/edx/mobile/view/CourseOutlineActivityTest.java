package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.IBlock;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.edx.mobile.http.util.CallUtil.executeStrict;
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
     * Provide both true and false values as the {@link #provideCourseId}
     * parameter in order to have both states tested.
     *
     * @return The sets of parameters containing true and false values
     */
    @Parameters(name = "{index}: provide course id = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { false }, { true } });
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
        initialize(getIntent());
    }

    /**
     * Generic method for running the initialization tests and returning the
     * controller.
     *
     * @param intent The {@link Intent} to start the {@link CourseOutlineActivity} with
     * @return The {@link ActivityController} instance used to initialize the
     *         {@link CourseOutlineActivity}
     */
    private ActivityController<? extends CourseOutlineActivity> initialize(Intent intent) {
        ActivityController<? extends CourseOutlineActivity> controller =
                Robolectric.buildActivity(getActivityClass()).withIntent(intent);
        CourseOutlineActivity activity = controller.create(null).postCreate(null).get();
        // TODO: Write a comprehensive and isolated test suite for the Fragment
        Fragment fragment = activity.getSupportFragmentManager()
                .findFragmentByTag(CourseOutlineFragment.TAG);
        assertNotNull(fragment);
        assertThat(fragment).isInstanceOf(CourseOutlineFragment.class);
        assertTrue(fragment.getRetainInstance());
        Bundle args = fragment.getArguments();
        assertNotNull(args);
        Bundle data = intent.getBundleExtra(Router.EXTRA_BUNDLE);
        assertEquals(data.getSerializable(Router.EXTRA_COURSE_DATA),
                args.getSerializable(Router.EXTRA_COURSE_DATA));
        if (provideCourseId) {
            assertEquals(data.getString(Router.EXTRA_COURSE_COMPONENT_ID),
                    args.getString(Router.EXTRA_COURSE_COMPONENT_ID));
        }
        return controller;
    }

    // Since Robolectric doesn't simulate actual Activity navigation, we
    // can only test forward navigation, and only up to one level. This
    // blocks us from testing the back stack restructuring upon switching
    // to a different section from CourseUnitNavigationActivityTest.
    /**
     * Testing navigation to a section
     */
    @Test
    public void sectionNavigationTest() {
        Intent intent = getIntent();
        Bundle extras = intent.getBundleExtra(Router.EXTRA_BUNDLE);
        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse)
                extras.getSerializable(Router.EXTRA_COURSE_DATA);
        assertNotNull(courseData);
        String courseId = courseData.getCourse().getId();
        CourseStructureV1Model model;
        try {
            model = executeStrict(courseAPI.getCourseStructure(courseId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CourseComponent courseComponent = (CourseComponent)
                CourseAPI.normalizeCourseStructure(model, courseId);
        int subsectionRowIndex = -1;
        String subsectionId = null;
        CourseComponent subsectionUnit = null;
        List<IBlock> sections = courseComponent.getChildren();
        sectionIteration: for (@SuppressWarnings("unused") IBlock section : sections) {
            subsectionRowIndex++;
            for (IBlock subsection : section.getChildren()) {
                subsectionRowIndex++;
                if (((CourseComponent) subsection).isContainer()) {
                    subsectionId = subsection.getId();
                    List<CourseComponent> leafComponents = new ArrayList<>();
                    courseComponent.fetchAllLeafComponents(leafComponents,
                            EnumSet.allOf(BlockType.class));
                    subsectionUnit = leafComponents.get(0);
                    break sectionIteration;
                }
            }
        }
        assertNotNull(subsectionId);
        extras.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponent.getId());

        ActivityController<? extends CourseOutlineActivity> controller = initialize(intent);
        CourseOutlineActivity activity = controller.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(
                CourseOutlineFragment.TAG);
        assertThat(fragment).isInstanceOf(CourseOutlineFragment.class);
        CourseOutlineFragment courseOutlineFragment = (CourseOutlineFragment) fragment;
        clickRow(controller, courseOutlineFragment, subsectionRowIndex);
        Intent newIntent = assertNextStartedActivity(activity, CourseOutlineActivity.class);
        Bundle newData = newIntent.getBundleExtra(Router.EXTRA_BUNDLE);
        assertNotNull(newData);
        assertEquals(courseData, newData.getSerializable(Router.EXTRA_COURSE_DATA));
        assertEquals(subsectionId, newData.getString(Router.EXTRA_COURSE_COMPONENT_ID));

        // Back stack reconstruction upon receiving a specific path
        Intent resultData = new Intent();
        resultData.putExtra(Router.EXTRA_COURSE_COMPONENT_ID, subsectionUnit.getId());
        courseOutlineFragment.onActivityResult(
                CourseOutlineFragment.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                Activity.RESULT_OK, resultData);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        BlockPath outlinePath = courseComponent.getPath();
        BlockPath leafPath = subsectionUnit.getPath();
        int outlinePathSize = outlinePath.getPath().size();
        for (int i = outlinePathSize + 1;; i += 2) {
            newIntent = shadowActivity.getNextStartedActivity();
            CourseComponent nextComp = leafPath.get(i);
            if (nextComp == null || !nextComp.isContainer()) {
                assertNull(newIntent);
                break;
            }
            assertNotNull(newIntent);
            assertThat(newIntent).hasComponent(activity, CourseOutlineActivity.class);
            newData = newIntent.getBundleExtra(Router.EXTRA_BUNDLE);
            assertNotNull(newData);
            assertEquals(courseData, newData.getSerializable(Router.EXTRA_COURSE_DATA));
            assertEquals(nextComp.getId(), newData.getString(Router.EXTRA_COURSE_COMPONENT_ID));
        }
    }

    /**
     * Testing navigation to a unit
     */
    @Test
    public void unitNavigationTest() {
        Intent intent = getIntent();
        Bundle extras = intent.getBundleExtra(Router.EXTRA_BUNDLE);
        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse)
                extras.getSerializable(Router.EXTRA_COURSE_DATA);
        assertNotNull(courseData);
        String courseId = courseData.getCourse().getId();
        CourseStructureV1Model model;
        try {
            model = executeStrict(courseAPI.getCourseStructure(courseId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CourseComponent courseComponent = (CourseComponent)
                CourseAPI.normalizeCourseStructure(model, courseId);
        List<CourseComponent> leafComponents = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(leafComponents,
                EnumSet.allOf(BlockType.class));
        CourseComponent courseUnit = leafComponents.get(0);
        CourseComponent lastUnit = leafComponents.get(leafComponents.size() - 1);
        assertNotEquals(lastUnit, courseUnit);
        courseComponent = courseUnit.getParent();
        if (courseUnit.getPath().getPath().size() % 2 > 0) {
            courseComponent = courseComponent.getParent();
        }
        int subsectionRowIndex = -1;
        List<IBlock> sections = courseComponent.getChildren();
        sectionIteration: for (@SuppressWarnings("unused") IBlock section : sections) {
            subsectionRowIndex++;
            if (courseUnit.equals(section)) {
                break;
            }
            for (@SuppressWarnings("unused") IBlock subsection : section.getChildren()) {
                subsectionRowIndex++;
                if (courseUnit.equals(subsection)) {
                    break sectionIteration;
                }
            }
        }
        extras.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponent.getId());

        ActivityController<? extends CourseOutlineActivity> controller = initialize(intent);
        CourseOutlineActivity activity = controller.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(
                CourseOutlineFragment.TAG);
        assertThat(fragment).isInstanceOf(CourseOutlineFragment.class);
        CourseOutlineFragment courseOutlineFragment = (CourseOutlineFragment) fragment;
        clickRow(controller, courseOutlineFragment, subsectionRowIndex);
        Intent newIntent = assertNextStartedActivity(activity, CourseUnitNavigationActivity.class);
        Bundle newData = newIntent.getBundleExtra(Router.EXTRA_BUNDLE);
        assertNotNull(newData);
        assertEquals(courseData, newData.getSerializable(Router.EXTRA_COURSE_DATA));
        assertEquals(courseUnit.getId(), newData.getSerializable(
                Router.EXTRA_COURSE_COMPONENT_ID));

        // Test the back stack reconstruction upon receiving a specific path
        // Should not perform any action if it receives a unit selection from itself
        Intent resultData = new Intent();
        resultData.putExtra(Router.EXTRA_COURSE_COMPONENT_ID, courseUnit.getId());
        courseOutlineFragment.onActivityResult(
                CourseOutlineFragment.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                Activity.RESULT_OK, resultData);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        assertNull(shadowActivity.getNextStartedActivity());
        assertFalse(shadowActivity.isFinishing());
        // Should finish itself to start the new navigation back stack if it receives
        // a unit selection from another section
        resultData.putExtra(Router.EXTRA_COURSE_COMPONENT_ID, lastUnit.getId());
        courseOutlineFragment.onActivityResult(
                CourseOutlineFragment.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                Activity.RESULT_OK, resultData);
        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(shadowActivity.isFinishing());
    }

    /**
     * Generic method for clicking on a list row provided an index with
     * appropriate assertions
     *
     * @param controller The {link ActivityController} controlling the
     *                   {@link CourseOutlineActivity}
     * @param courseOutlineFragment The {@link CourseOutlineFragment} instance
     * @param rowIndex The row index
     */
    private void clickRow(ActivityController<? extends CourseOutlineActivity> controller,
            CourseOutlineFragment courseOutlineFragment, int rowIndex) {
        controller.resume().postResume().visible();
        View fragmentView = courseOutlineFragment.getView();
        assertNotNull(fragmentView);
        View outlineList = fragmentView.findViewById(R.id.outline_list);
        assertNotNull(outlineList);
        assertThat(outlineList).isInstanceOf(ListView.class);
        ListView listView = (ListView) outlineList;
        listView.performItemClick(listView.getChildAt(rowIndex),
                rowIndex, listView.getItemIdAtPosition(rowIndex));
    }
}
