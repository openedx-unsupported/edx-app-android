package org.edx.mobile.view.common;

import android.support.annotation.NonNull;

/**
 * Interface for showing temporary or
 * permanent message banners on the screen.
 */
public interface BannerDisplayCallback {
    /**
     * Display a banner with the message strings and
     * icon as specified by the type.
     *
     * @param bannerType The banner type
     */
    void showBanner(@NonNull BannerType bannerType);
}
