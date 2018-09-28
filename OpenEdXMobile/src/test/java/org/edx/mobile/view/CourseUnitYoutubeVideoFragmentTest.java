package org.edx.mobile.view;

import android.support.v4.app.Fragment;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.VideoBlockModel;

import org.junit.Test;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import roboguice.activity.RoboFragmentActivity;

import static org.edx.mobile.http.util.CallUtil.executeStrict;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CourseUnitYoutubeVideoFragmentTest extends  UiTest {

    private VideoBlockModel getVideoUnit() {
        EnrolledCoursesResponse courseData;
        try {
            courseData = executeStrict(courseAPI.getEnrolledCourses()).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String courseId = courseData.getCourse().getId();
        CourseStructureV1Model model;
        CourseComponent courseComponent;
        try {
            model = executeStrict(courseAPI.getCourseStructure(courseId));
            courseComponent = (CourseComponent) CourseAPI.normalizeCourseStructure(model, courseId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return (VideoBlockModel) courseComponent.getVideos().get(0);
    }
    private void testOrientationChange(
            CourseUnitYoutubeVideoFragment fragment, int orientation) {
        Resources resources = fragment.getResources();
        Configuration config = resources.getConfiguration();
        assertNotEquals(orientation, config.orientation);
        config.orientation = orientation;
        fragment.onConfigurationChanged(config);
    }

    /**
     * Testing orientation changes
     */
    @Test
    public void orientationChangeTest() {
        CourseUnitYoutubeVideoFragment fragment = CourseUnitYoutubeVideoFragment.newInstance(getVideoUnit(), false, false);
        SupportFragmentTestUtil.startVisibleFragment(fragment, FragmentUtilActivity.class, 1);
        assertNotEquals(Configuration.ORIENTATION_LANDSCAPE,
                fragment.getResources().getConfiguration().orientation);

        testOrientationChange(fragment, Configuration.ORIENTATION_LANDSCAPE);
        testOrientationChange(fragment, Configuration.ORIENTATION_PORTRAIT);
    }
    @Test
    public void initializeTest() {
        CourseUnitYoutubeVideoFragment fragment = CourseUnitYoutubeVideoFragment.newInstance(getVideoUnit(), false, false);
        SupportFragmentTestUtil.startVisibleFragment(fragment, FragmentUtilActivity.class, 1);
        assertTrue(fragment.getRetainInstance());

        View view = fragment.getView();
        assertNotNull(view);
        View playerContainer = view.findViewById(R.id.player_container);
        assertNotNull(playerContainer);
    }

    @Test
    public void onPageShowTest() {
        CourseUnitYoutubeVideoFragment fragment = CourseUnitYoutubeVideoFragment.newInstance(getVideoUnit(), false, false);
        SupportFragmentTestUtil.startVisibleFragment(fragment, FragmentUtilActivity.class, 1);

        fragment.onPageShow();

        Fragment playerContainer = fragment.getChildFragmentManager().findFragmentById(R.id.player_container);
        assertNotNull(playerContainer);
        assertTrue(playerContainer instanceof YouTubePlayerSupportFragment);
    }

    @Test
    public void onPageDisappearTest() {
        CourseUnitYoutubeVideoFragment fragment = CourseUnitYoutubeVideoFragment.newInstance(getVideoUnit(), false, false);
        SupportFragmentTestUtil.startVisibleFragment(fragment, FragmentUtilActivity.class, 1);

        fragment.onPageDisappear();

        Fragment playerContainer = fragment.getChildFragmentManager().findFragmentById(R.id.player_container);
        assertNotNull(playerContainer);
        assertTrue(playerContainer instanceof Fragment);
    }

    private static class FragmentUtilActivity extends RoboFragmentActivity implements CourseUnitFragment.HasComponent {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LinearLayout view = new LinearLayout(this);
            // noinspection ResourceType
            view.setId(1);

            setContentView(view);
        }

        @Override
        public CourseComponent getComponent() {
            return null;
        }

        @Override
        public void navigateNextComponent() {
        }

        @Override
        public void navigatePreviousComponent() {
        }
    }
}