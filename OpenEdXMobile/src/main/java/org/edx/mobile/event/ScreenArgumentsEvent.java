package org.edx.mobile.event;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Event fired to pass the arguments ({@link Bundle Bundle}) to a Fragment inside a ViewPager
 */
public class ScreenArgumentsEvent {

    @NonNull
    private Bundle bundle;

    public ScreenArgumentsEvent(@NonNull Bundle bundle) {
        this.bundle = bundle;
    }

    @NonNull
    public Bundle getBundle() {
        return bundle;
    }
}
