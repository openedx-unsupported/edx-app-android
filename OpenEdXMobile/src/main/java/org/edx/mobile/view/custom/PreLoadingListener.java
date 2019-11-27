package org.edx.mobile.view.custom;

import androidx.annotation.NonNull;

/**
 * Provides valuable loading information of pages inside a ViewPager to optimize its pre-loading
 * behavior.
 */
public interface PreLoadingListener {
    enum State {
        /**
         * Default state when no loading state has been set explicitly.
         */
        DEFAULT,
        /**
         * Specifies that the currently visible page of the ViewPager is currently loading.
         */
        MAIN_UNIT_LOADING,
        /**
         * Specifies that the currently visible page of the ViewPager has loaded successfully.
         */
        MAIN_UNIT_LOADED
    }

    void setLoadingState(@NonNull State newState);

    boolean isMainUnitLoaded();
}
