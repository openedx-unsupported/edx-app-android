package org.edx.mobile.shadows;

import android.view.Menu;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import roboguice.util.AndroidCallableWrapper;

import static org.robolectric.internal.Shadow.*;

/**
 * Shadow for {@link AndroidCallableWrapper} to remove potential deadlock
 * caused by having it executed on the main thread (if the
 * {@link org.robolectric.util.Scheduler} is paused).
 * @param <ResultT>
 */
@Implements(AndroidCallableWrapper.class)
@SuppressWarnings("unused")
public class ShadowAndroidCallableWrapper<ResultT> {
    @RealObject
    private AndroidCallableWrapper<ResultT> realAndroidCallableWrapper;

    @Implementation
    public void beforeCall() {
        directlyOn(realAndroidCallableWrapper, AndroidCallableWrapper.class, "doOnPreCall");
    }
}
