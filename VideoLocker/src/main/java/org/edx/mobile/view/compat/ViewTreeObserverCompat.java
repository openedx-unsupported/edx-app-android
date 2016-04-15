package org.edx.mobile.view.compat;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.ViewTreeObserver;

/**
 * Helper for accessing features in {@link ViewTreeObserver}
 * introduced in newer API levels in a backwards compatible fashion.
 */
public class ViewTreeObserverCompat {
    private ViewTreeObserverCompat() {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove a previously installed global layout callback
     *
     * @param listener The callback to remove
     *
     * @throws IllegalStateException If {@link ViewTreeObserver#isAlive()} returns false
     */
    @UiThread
    public static void removeOnGlobalLayoutListener(@NonNull ViewTreeObserver observer,
                                                    @NonNull ViewTreeObserver.OnGlobalLayoutListener listener) {
        IMPL.removeOnGlobalLayoutListener(observer, listener);
    }

    static final ViewTreeObserverCompatImpl IMPL;
    static {
        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.JELLY_BEAN) {
            IMPL = new JBViewTreeObserverCompatImpl();
        } else {
            IMPL = new BaseViewTreeObserverCompatImpl();
        }
    }

    private interface ViewTreeObserverCompatImpl {
        @UiThread
        void removeOnGlobalLayoutListener(@NonNull ViewTreeObserver observer,
                @NonNull ViewTreeObserver.OnGlobalLayoutListener listener);
    }

    @SuppressWarnings("deprecation")
    private static class BaseViewTreeObserverCompatImpl implements ViewTreeObserverCompatImpl {
        @Override
        @UiThread
        public void removeOnGlobalLayoutListener(@NonNull ViewTreeObserver observer,
                @NonNull ViewTreeObserver.OnGlobalLayoutListener listener) {
            observer.removeGlobalOnLayoutListener(listener);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static class JBViewTreeObserverCompatImpl extends BaseViewTreeObserverCompatImpl {
        @Override
        @UiThread
        public void removeOnGlobalLayoutListener(@NonNull ViewTreeObserver observer,
                @NonNull ViewTreeObserver.OnGlobalLayoutListener listener) {
            observer.removeOnGlobalLayoutListener(listener);
        }
    }
}
