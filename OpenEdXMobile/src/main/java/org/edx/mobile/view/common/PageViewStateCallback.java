package org.edx.mobile.view.common;

/**
 * For PageView, the onResume, onPause... is not called as it is still in memory. so we need
 * to define callback to sync with page movement
 *
 * SetInitialPage callback added to initialize first fragment (specifically needed for youtube fragment,
 * to automatically load the video after the fragment is initialized) going along with above reason.
 */
public interface PageViewStateCallback {
    void onFirstPageLoad();
    void onPageShow();
    void onPageDisappear();
}
