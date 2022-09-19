package org.edx.mobile.view;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.edx.mobile.http.util.CallUtil.executeStrict;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.webkit.WebView;

import org.edx.mobile.R;
import org.edx.mobile.base.UiTest;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.exception.CourseContentNotValidException;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentController;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

@HiltAndroidTest
@Config(application = HiltTestApplication.class)
@RunWith(RobolectricTestRunner.class)
public class CourseUnitWebViewFragmentTest extends UiTest {

    @Rule()
    public HiltAndroidRule hiltAndroidRule = new HiltAndroidRule(this);

    @Before
    public void init() {
        hiltAndroidRule.inject();
    }


    EnrolledCoursesResponse courseData;

    /**
     * Method to initialize Course Data from API
     */
    private void initializeCourseData() {
        try {
            courseData = executeStrict(courseAPI.getEnrolledCourses()).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method for iterating through the mock course response data, and
     * returning the first block leaf.
     *
     * @return The first {@link HtmlBlockModel} leaf in the mock course data
     */
    private HtmlBlockModel getHtmlUnit() throws CourseContentNotValidException {
        String courseId = courseData.getCourse().getId();
        CourseStructureV1Model model;
        try {
            model = executeStrict(courseAPI.getCourseStructure(courseId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CourseComponent courseComponent = (CourseComponent)
                CourseAPI.normalizeCourseStructure(model, courseId);
        List<CourseComponent> htmlBlockUnits = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(htmlBlockUnits,
                EnumSet.of(BlockType.HTML));
        return (HtmlBlockModel) htmlBlockUnits.get(0);
    }

    /**
     * Testing initialization
     */
    @Test
    public void initializeTest() throws CourseContentNotValidException {
        initializeCourseData();
        CourseUnitWebViewFragment fragment = CourseUnitWebViewFragment.newInstance(getHtmlUnit(), courseData.getCourse().getName(),
                courseData.getMode(), false);
        SupportFragmentController.setupFragment(fragment, HiltTestActivity.class,
                android.R.id.content, null);
        View view = fragment.getView();
        assertNotNull(view);

        View courseUnitWebView = view.findViewById(R.id.webview);
        assertNotNull(courseUnitWebView);
        Java6Assertions.assertThat(courseUnitWebView).isInstanceOf(WebView.class);
        WebView webView = (WebView) courseUnitWebView;
        assertTrue(webView.getSettings().getJavaScriptEnabled());
    }
}
