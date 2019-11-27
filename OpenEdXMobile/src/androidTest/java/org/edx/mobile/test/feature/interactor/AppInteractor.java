package org.edx.mobile.test.feature.interactor;

import android.app.Instrumentation;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;

public class AppInteractor {
    public AppLaunchInteractor launchApp() {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final Intent launchIntent = instrumentation.getTargetContext().getPackageManager()
                .getLaunchIntentForPackage(instrumentation.getTargetContext().getPackageName())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        instrumentation.startActivitySync(launchIntent);
        instrumentation.waitForIdleSync();
        return new AppLaunchInteractor();
    }

    /**
     * Any screen that is possible to land on when cold launching the app should be added here
     */
    public static class AppLaunchInteractor {
        // Anonymous users should land here
        public LandingScreenInteractor observeLandingScreen() {
            return new LandingScreenInteractor().observeLandingScreen();
        }

        // Authenticated users should land here
        public MyCoursesScreenInteractor observeMyCoursesScreen() {
            return new MyCoursesScreenInteractor().observeMyCoursesScreen();
        }

        // Users with bad auth tokens should land here
        public LogInScreenInteractor observeLogInScreen() {
            return new LogInScreenInteractor().observeLogInScreen();
        }
    }
}
