package org.edx.mobile.view;

import android.view.View;
import android.webkit.WebView;

import org.edx.mobile.R;
import org.edx.mobile.test.http.HttpBaseTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.util.SupportFragmentTestUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

// There is currently a Robolectric issue with initializing EdxWebView:
// https://github.com/robolectric/robolectric/issues/793
// We should add mock web server and test the handling later
@RunWith(RobolectricGradleTestRunner.class)
public class CourseUnitWebviewFragmentTest extends HttpBaseTestCase {
    /**
     * Testing initialization
     */
    @Test
    public void initializeTest() {
        CourseUnitWebviewFragment fragment = CourseUnitWebviewFragment.newInstance(null);
        SupportFragmentTestUtil.startVisibleFragment(fragment);
        View view = fragment.getView();
        assertNotNull(view);

        View courseUnitWebView = view.findViewById(R.id.course_unit_webView);
        assertNotNull(courseUnitWebView);
        assertThat(courseUnitWebView).isInstanceOf(WebView.class);
        WebView webView = (WebView) courseUnitWebView;
        assertTrue(webView.getSettings().getJavaScriptEnabled());
    }
}
