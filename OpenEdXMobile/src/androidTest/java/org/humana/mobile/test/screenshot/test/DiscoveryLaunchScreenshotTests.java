package org.humana.mobile.test.screenshot.test;

import org.humana.mobile.view.DiscoveryLaunchActivity;
import org.humana.mobile.view.DiscoveryLaunchPresenter;
import org.humana.mobile.view.PresenterActivityScreenshotTest;
import org.junit.Test;

public class DiscoveryLaunchScreenshotTests extends PresenterActivityScreenshotTest<DiscoveryLaunchActivity, DiscoveryLaunchPresenter, DiscoveryLaunchPresenter.ViewInterface> {

    @Test
    public void testScreenshot_withCourseDiscoveryDisabled() {
        view.setEnabledButtons(false);
    }

    @Test
    public void testScreenshot_withCourseDiscoveryEnabled() {
        view.setEnabledButtons(true);
    }
}
