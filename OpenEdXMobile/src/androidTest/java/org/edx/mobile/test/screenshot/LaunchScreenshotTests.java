package org.edx.mobile.test.screenshot;

import android.app.Activity;

import androidx.test.rule.ActivityTestRule;

import com.facebook.testing.screenshot.Screenshot;

import org.edx.mobile.view.LaunchActivity;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

@Ignore
@HiltAndroidTest
public class LaunchScreenshotTests {

    @Rule(order = 0)
    public HiltAndroidRule hiltAndroidRule = new HiltAndroidRule(this);

    @Rule(order = 1)
    public ActivityTestRule<LaunchActivity> mActivityRule =
            new ActivityTestRule<>(LaunchActivity.class, true, true);

    @Rule(order = 2)
    public TestName testName = new TestName();

    @Before
    public void before() {
        hiltAndroidRule.inject();
    }

    @Test
    public void testScreenshot_recordLaunchActivity() throws Throwable {
        Activity activity = mActivityRule.getActivity();
        activity.runOnUiThread(() -> {
            Screenshot.snap(activity.findViewById(android.R.id.content)).setName(getClass().getName() + "_" + testName.getMethodName()).record();
        });
    }
}
