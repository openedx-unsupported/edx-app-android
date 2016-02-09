package org.edx.mobile.base;

import android.app.Application;
import android.support.annotation.NonNull;

import org.edx.mobile.util.Config;

/**
 * This class exists so you can easily call custom code during the application lifecycle.
 * This file should not be edited by edX unless absolutely necessary.
 */
public class CustomApplicationExtension {

    public CustomApplicationExtension(@NonNull Config config, @NonNull Application application) {
    }

    /**
     * Will be called after the application is created and initialized
     */
    public void onApplicationCreated() {
        // Add custom code here
    }

    /**
     * Will be called when the application is about to be terminated
     */
    public void onApplicationTerminated() {
        // Add custom code here
    }
}
