package org.edx.mobile.test.screenshot.test;

import org.edx.mobile.view.DiscoveryLaunchActivity;
import org.edx.mobile.view.DiscoveryLaunchPresenter;
import org.edx.mobile.view.PresenterActivityInstrumentationTest;
import org.junit.Test;

public class DiscoveryLaunchScreenshotTests extends PresenterActivityInstrumentationTest<DiscoveryLaunchActivity, DiscoveryLaunchPresenter, DiscoveryLaunchPresenter.ViewInterface> {

    @Test
    public void testScreenshot_withCourseDiscoveryDisabled() {
        view.setEnabledButtons(false, false);
    }

    @Test
    public void testScreenshot_withCourseDiscoveryEnabled() {
        view.setEnabledButtons(true, false);
    }

    @Test
    public void testScreenshot_withCourseDiscoveryAndExploreSubjectsEnabled() {
        view.setEnabledButtons(true, true);
    }
}
