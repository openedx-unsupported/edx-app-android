package org.edx.mobile.shadows;

import android.os.Handler;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.android.util.concurrent.RoboExecutorService;

import roboguice.util.SafeAsyncTask;

/**
 * Shadow for {@link SafeAsyncTask} to ensure that it runs on
 * the Robolectric background @{link org.robolectric.util.Scheduler}
 * to enable instrumentation.
 */
@Implements(SafeAsyncTask.class)
@SuppressWarnings({"unused", "deprecation"})
public class ShadowSafeAsyncTask<ResultT> {
    @RealObject
    private SafeAsyncTask<ResultT> realSafeAsyncTask;

    public void __constructor__() {
        realSafeAsyncTask.executor(new RoboExecutorService());
    }

    public void __constructor__(Handler handler) {
        __constructor__();
        realSafeAsyncTask.handler(handler);
    }
}
