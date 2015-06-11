package org.edx.mobile.test;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.Environment;

/**
 * The {@link MainApplication} class is overridden for testing in
 * order to only have the components enabled that are relevant to
 * the tests.
 *
 * The following components are not enabled:
 *
 * - Application lifecycle callbacks.
 *   This was used to detect to force the application to start
 *   from the main screen when relaunched from the background,
 *   which is not present in the current tests.
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
        // initialize logger
        Logger.init(this.getApplicationContext());

        application = this;

        // setup environment
        Environment env = new Environment();
        env.setupEnvironment(this.getApplicationContext());

        // setup image cache
        createImageCache();

        // initialize SegmentIO
        if (Config.getInstance().getSegmentConfig().isEnabled()) {
            SegmentFactory.makeInstance(this);
        }
    }
}
