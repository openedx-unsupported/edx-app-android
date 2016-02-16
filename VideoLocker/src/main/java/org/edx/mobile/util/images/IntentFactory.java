package org.edx.mobile.util.images;

import android.content.ComponentName;
import android.content.Intent;

import org.edx.mobile.BuildConfig;

public enum IntentFactory {
    ;
    /**
     * @return a new Intent that will start the activity that this method was called from
     */
    public static Intent newIntentForComponent() {
        final StackTraceElement[] s = new RuntimeException().getStackTrace();
        return new Intent().setComponent(new ComponentName(BuildConfig.APPLICATION_ID, s[1].getClassName()));
    }
}
