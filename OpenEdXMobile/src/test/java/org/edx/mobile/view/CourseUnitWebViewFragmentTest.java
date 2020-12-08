package org.edx.mobile.view;

import androidx.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.exception.CourseContentNotValidException;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.view.custom.PreLoadingListener;
import org.junit.Test;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import roboguice.activity.RoboFragmentActivity;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.edx.mobile.http.util.CallUtil.executeStrict;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CourseUnitWebViewFragmentTest extends UiTest {

    EnrolledCoursesResponse courseData;

    /**
     * Method to initialize Course Data from API
     */
    private void initializeCourseData(){
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
            model = executeStrict(courseAPI.getCourseStructure(config.getApiUrlVersionConfig().getBlocksApiVersion(), courseId));
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
        CourseUnitWebViewFragment fragment = CourseUnitWebViewFragment.newInstance(getHtmlUnit(), courseData.getMode());
        SupportFragmentTestUtil.startVisibleFragment(fragment, PreLoadingListenerActivity.class, android.R.id.content);
        View view = fragment.getView();
        assertNotNull(view);

        View courseUnitWebView = view.findViewById(R.id.webview);
        assertNotNull(courseUnitWebView);
        assertThat(courseUnitWebView).isInstanceOf(WebView.class);
        WebView webView = (WebView) courseUnitWebView;
        assertTrue(webView.getSettings().getJavaScriptEnabled());
    }

    /**
     * The {@link CourseUnitWebViewFragment} requires its parent activity to implement the
     * {@link PreLoadingListener} interface, which is why this dummy activity has been created.
     */
    private static class PreLoadingListenerActivity extends RoboFragmentActivity implements PreLoadingListener {
        @Override
        public void setLoadingState(@NonNull State newState) {

        }

        @Override
        public boolean isMainUnitLoaded() {
            return false;
        }
    }
}
