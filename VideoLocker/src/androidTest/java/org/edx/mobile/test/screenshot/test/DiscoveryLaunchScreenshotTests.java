package org.edx.mobile.test.screenshot.test;

import com.facebook.testing.screenshot.Screenshot;

import org.edx.mobile.R;
import org.edx.mobile.view.DiscoveryLaunchActivity;
import org.edx.mobile.view.DiscoveryLaunchPresenter;
import org.edx.mobile.view.PresenterActivityInstrumentationTest;
import org.junit.Test;

public class DiscoveryLaunchScreenshotTests extends PresenterActivityInstrumentationTest<DiscoveryLaunchActivity, DiscoveryLaunchPresenter, DiscoveryLaunchPresenter.ViewInterface> {

    @Test
    public void testScreenshot_withCourseDiscoveryDisabled() {
        startActivity(null);
        view.setEnabledButtons(false, false);
        Screenshot.snap(activity.findViewById(R.id.root_view)).record();
    }

    @Test
    public void testScreenshot_withCourseDiscoveryEnabled() {
        startActivity(null);
        view.setEnabledButtons(true, false);
        Screenshot.snap(activity.findViewById(R.id.root_view)).record();
    }

    @Test
    public void testScreenshot_withCourseDiscoveryAndExploreSubjectsEnabled() {
        startActivity(null);
        view.setEnabledButtons(true, true);
        Screenshot.snap(activity.findViewById(R.id.root_view)).record();
    }
}
