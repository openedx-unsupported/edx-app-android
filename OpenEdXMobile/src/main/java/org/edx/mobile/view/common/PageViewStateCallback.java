package org.edx.mobile.view.common;

/**
 * For PageView, the onResume, onPause... is not called as it is still in memory. so we need
 * to define callback to sync with page movement
 */
public interface PageViewStateCallback {
    void onPageShow();
    void onPageDisappear();
}
