package org.edx.mobile.view;

import static org.assertj.android.api.Assertions.assertThat;
import static org.edx.mobile.http.util.CallUtil.executeStrict;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.view.app_nav.CourseUnitNavigationActivity;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public abstract class CourseBaseActivityTest extends BaseFragmentActivityTest {
    /**
     * Method for defining the subclass of {@link CourseUnitNavigationActivity} that
     * is being tested. Should be overridden by subclasses.
     *
     * @return The {@link CourseUnitNavigationActivity} subclass that is being tested
     */
    @Override
    protected Class<? extends CourseUnitNavigationActivity> getActivityClass() {
        return CourseUnitNavigationActivity.class;
    }

    /**
     * Parameterized flag for whether to provide the course ID explicitly, or
     * allow CourseUnitNavigationActivity to fallback to loading the base course.
     */
    @Parameter
    public boolean provideCourseId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Intent getIntent() {
        EnrolledCoursesResponse courseData;
        try {
            courseData = executeStrict(courseAPI.getEnrolledCourses())
                    .getEnrollments()
                    .get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Intent intent = super.getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        if (provideCourseId) {
            String courseId = courseData.getCourse().getId();
            CourseStructureV1Model model;
            CourseComponent courseComponent;
            try {
                model = executeStrict(courseAPI.getCourseStructureWithStale(courseId));
                courseComponent = (CourseComponent) CourseAPI.Companion.normalizeCourseStructure(model, courseId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            extras.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponent.getId());
        }
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
     * Testing initialization
     */
    @Test
    @SuppressLint("RtlHardcoded")
    public void initializeTest() {
        ActivityController<? extends CourseUnitNavigationActivity> controller =
                Robolectric.buildActivity(getActivityClass(), getIntent());
        CourseUnitNavigationActivity activity = controller.get();

        controller.create();

        controller.postCreate(null).resume().postResume().visible();
    }

    /**
     * Testing process start and finish method functionality
     */
    @Test
    public void processLifecycleTest() {
        // We need to retrieve the progressWheel view before calling visible(), since that
        // initializes fragment views as well, which might add other views with the same id
        ActivityController<? extends CourseUnitNavigationActivity> controller =
                Robolectric.buildActivity(getActivityClass(), getIntent())
                        .create().start().postCreate(null).resume();
        CourseUnitNavigationActivity activity = controller.get();
        ProgressBar progressWheel = (ProgressBar)
                activity.findViewById(R.id.loading_indicator);
        controller.visible();
//        if (progressWheel == null) {
//            activity.startProcess();
//            activity.finishProcess();
//        } else {
//            assertThat(progressWheel).isNotVisible();
//            activity.startProcess();
//            assertThat(progressWheel).isVisible();
//            activity.finishProcess();
//            assertThat(progressWheel).isNotVisible();
//        }
    }
}
