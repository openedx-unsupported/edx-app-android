package org.edx.mobile.test.feature.interactor;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

public class AppInteractor {
    public LandingScreenInteractor launchApp() {
        // Start launch activity
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final Intent launchIntent = instrumentation.getTargetContext().getPackageManager()
                .getLaunchIntentForPackage(instrumentation.getTargetContext().getPackageName())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instrumentation.startActivitySync(launchIntent);
        instrumentation.waitForIdleSync();

        return new LandingScreenInteractor();
    }
}
