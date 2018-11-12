package org.edx.mobile.test;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.state.StateSaver;
import com.facebook.FacebookSdk;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;

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
 * - Checking for application upgrades, and repairing download
 *   statuses and clearing the web view cookie cache.
 */
public class TestApplication extends MainApplication {
    @Override
    public void onCreate() {
        // Register Font Awesome module in android-iconify library
        Iconify.with(new FontAwesomeModule());
        application = this;

        // Facebook sdk should be initialized through AndroidManifest meta data declaration but
        // we are generating the meta data through gradle script due to which it is necessary
        // to manually initialize the sdk here.
        // Initialize to a Fake Application ID as it will not connect to the actual API
        FacebookSdk.setApplicationId("1234567812345678");
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Init Bridge for state saving/restoration
        Bridge.initialize(this, new SavedStateHandler() {
            @Override
            public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
                StateSaver.saveInstanceState(target, state);
            }

            @Override
            public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
                StateSaver.restoreInstanceState(target, state);
            }
        });
    }
}
