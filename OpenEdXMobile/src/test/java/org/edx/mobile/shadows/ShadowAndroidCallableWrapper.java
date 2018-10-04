package org.edx.mobile.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

import roboguice.util.AndroidCallableWrapper;


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
        Shadow.directlyOn(realAndroidCallableWrapper, AndroidCallableWrapper.class, "doOnPreCall");
    }
}
