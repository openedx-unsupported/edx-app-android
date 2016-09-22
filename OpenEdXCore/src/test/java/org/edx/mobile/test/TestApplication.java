package org.edx.mobile.test;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.edx.mobile.base.MainApplication;

/**
 * The {@link MainApplication} class is overridden for testing in
 * order to only have the components enabled that are relevant to
 * the tests, and setting a mock RoboGuice module.
 *
 * The following components are not enabled:
 *
 * - Application lifecycle callbacks.
 *   This was used to detect to force the application to start
 *   from the main screen when relaunched from the background,
 *   which is not present in the current tests.
 *
 * - RoboGuice injector initialization.
 *
 * - Crashlytics/Fabric crash reporting.
 *
 * - Facebook SDK intialization.
 *
 * - Parse notifications initialization and subscription.
 *
 * - Checking for application upgrades, and repairing download
 *   statuses and clearing the web view cookie cache.
 */
public class TestApplication extends MainApplication {
    @Override
    public void onCreate() {
        // Register Font Awesome module in android-iconify library
        Iconify.with(new FontAwesomeModule());
        application = this;
    }
}
