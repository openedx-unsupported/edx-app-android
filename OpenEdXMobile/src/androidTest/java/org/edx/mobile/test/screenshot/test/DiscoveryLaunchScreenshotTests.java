package org.edx.mobile.test.screenshot.test;

import org.edx.mobile.test.screenshot.test.base.PresenterActivityScreenshotTest;
import org.edx.mobile.view.DiscoveryLaunchActivity;
import org.edx.mobile.view.DiscoveryLaunchPresenter;
import org.junit.Test;

import dagger.hilt.android.testing.HiltAndroidTest;

@HiltAndroidTest
public class DiscoveryLaunchScreenshotTests extends PresenterActivityScreenshotTest<DiscoveryLaunchActivity, DiscoveryLaunchPresenter, DiscoveryLaunchPresenter.ViewInterface> {

    @Test
    public void testScreenshot_withCourseAndProgramDiscoveryDisabled() {
        view.setEnabledButtons(false, false);
    }

    @Test
    public void testScreenshot_withCourseDiscoveryEnabled() {
        view.setEnabledButtons(true, false);
    }

    @Test
    public void testScreenshot_withCourseAndProgramDiscoveryEnabled() {
        view.setEnabledButtons(true, true);
    }
}
