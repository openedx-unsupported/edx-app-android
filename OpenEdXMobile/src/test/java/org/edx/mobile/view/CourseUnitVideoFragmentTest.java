package org.edx.mobile.view;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.VideoBlockModel;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import roboguice.activity.RoboFragmentActivity;

import static org.assertj.android.api.Assertions.assertThat;
import static org.edx.mobile.http.util.CallUtil.executeStrict;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

// We should add mock downloads, mock play, and state retention tests
// later. Also, online/offline transition tests; although the
// onOnline() and onOffline() methods don't seem to be called from
// anywhere yet?

// The SDK version needs to be lesser than Lollipop because of this
// issue: https://github.com/robolectric/robolectric/issues/1810
@Config(sdk = 19)
public class CourseUnitVideoFragmentTest extends UiTest {
    /**
     * Method for iterating through the mock course response data, and
     * returning the first video block leaf.
     *
     * @return The first {@link VideoBlockModel} leaf in the mock course data
     */
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

    /**
     * Testing initialization
     */
    @Test
    public void initializeTest() {
        CourseUnitVideoFragment fragment = CourseUnitVideoFragment.newInstance(getVideoUnit(), false, false);
        SupportFragmentTestUtil.startVisibleFragment(fragment, FragmentUtilActivity.class, 1);
        assertTrue(fragment.getRetainInstance());

        View view = fragment.getView();
        assertNotNull(view);
        View messageContainer = view.findViewById(R.id.message_container);
        assertNotNull(messageContainer);
    }

    /**
     * Generic method for testing setup on orientation changes
     *
     * @param fragment The current fragment
     * @param orientation The orientation change to test
     */
    private void testOrientationChange(
            CourseUnitVideoFragment fragment, int orientation) {
        Resources resources = fragment.getResources();
        Configuration config = resources.getConfiguration();
        assertNotEquals(orientation, config.orientation);
        config.orientation = orientation;
        fragment.onConfigurationChanged(config);

        boolean isLandscape = config.orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        View view = fragment.getView();
        assertNotNull(view);
        Window window = fragment.getActivity().getWindow();

        View playerContainer = view.findViewById(R.id.player_container);
        if (playerContainer != null) {
            assertThat(playerContainer).isInstanceOf(ViewGroup.class);
            ViewGroup.LayoutParams layoutParams = playerContainer.getLayoutParams();
            assertNotNull(layoutParams);
            assertThat(layoutParams).hasWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            int height = isLandscape ? displayMetrics.heightPixels :
                    (displayMetrics.widthPixels * 9 / 16);
            assertThat(layoutParams).hasHeight(height);
        }
    }

    /**
     * Testing orientation changes
     */
    @Test
    public void orientationChangeTest() {
        CourseUnitVideoFragment fragment = CourseUnitVideoFragment.newInstance(getVideoUnit(), false, false);
        SupportFragmentTestUtil.startVisibleFragment(fragment, FragmentUtilActivity.class, 1);
        assertNotEquals(Configuration.ORIENTATION_LANDSCAPE,
                fragment.getResources().getConfiguration().orientation);

        testOrientationChange(fragment, Configuration.ORIENTATION_LANDSCAPE);
        testOrientationChange(fragment, Configuration.ORIENTATION_PORTRAIT);
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
