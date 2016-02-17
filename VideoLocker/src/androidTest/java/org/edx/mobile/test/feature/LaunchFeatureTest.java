package org.edx.mobile.test.feature;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.EdxEnvironment;
import org.edx.mobile.module.prefs.PrefManager;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
public class LaunchFeatureTest {

    @Test
    public void whenAppLaunched_withAnonymousUser_landingScreenIsDisplayed() {
        {
            // Ensure we are not logged in
            final MainApplication application = MainApplication.instance();
            final EdxEnvironment environment = application.getInjector().getInstance(EdxEnvironment.class);
            new PrefManager(application, PrefManager.Pref.LOGIN).clearAuth();
            environment.getSegment().resetIdentifyUser();
        }
        {
            // Start launch activity
            final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
            final Intent launchIntent = instrumentation.getTargetContext().getPackageManager()
                    .getLaunchIntentForPackage(instrumentation.getTargetContext().getPackageName())
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            instrumentation.startActivitySync(launchIntent);
            instrumentation.waitForIdleSync();
        }
        {
            // Look for edx_logo view which (we assume) is only present on the landing screen
            onView(withId(R.id.edx_logo)).check(matches(isCompletelyDisplayed()));
        }
    }
}
