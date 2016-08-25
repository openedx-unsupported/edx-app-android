package org.edx.mobile.test.screenshot.test;

import android.support.test.rule.UiThreadTestRule;
import android.view.View;

import com.facebook.testing.screenshot.Screenshot;

import org.edx.mobile.R;
import org.edx.mobile.view.DiscoveryLaunchActivity;
import org.edx.mobile.view.DiscoveryLaunchPresenter;
import org.edx.mobile.view.PresenterActivityInstrumentationTest;
import org.junit.Rule;
import org.junit.Test;

public class DiscoveryLaunchScreenshotTests extends PresenterActivityInstrumentationTest<DiscoveryLaunchActivity, DiscoveryLaunchPresenter, DiscoveryLaunchPresenter.ViewInterface> {

    @Rule
    public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Test
    public void testScreenshot_withCourseDiscoveryDisabled() throws Throwable {
        startActivity(null);
        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setEnabledButtons(false, false);
            }
        });
        Screenshot.snap(activity.findViewById(R.id.root_view)).record();
    }

    @Test
    public void testScreenshot_withCourseDiscoveryEnabled() throws Throwable {
        startActivity(null);
        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setEnabledButtons(true, false);
            }
        });
        Screenshot.snap(activity.findViewById(R.id.root_view)).record();
    }

    @Test
    public void testScreenshot_withCourseDiscoveryAndExploreSubjectsEnabled() throws Throwable {
        startActivity(null);
        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setEnabledButtons(true, true);
            }
        });
        Screenshot.snap(activity.findViewById(R.id.root_view)).record();
    }
}
