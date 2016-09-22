package org.edx.mobile.shadows;

import android.webkit.WebSettings;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link WebSettings} to provide working implementations of
 * methods that are stripped out from
 * {@link org.robolectric.fakes.RoboWebSettings} in lower API levels,
 * as reported at https://github.com/robolectric/robolectric/issues/2097
 */
@Implements(WebSettings.class)
public class ShadowWebSettings {
    private boolean loadsImagesAutomatically;

    @SuppressWarnings("unused")
    @Implementation
    public boolean getLoadsImagesAutomatically() {
        return loadsImagesAutomatically;
    }

    @SuppressWarnings("unused")
    @Implementation
    public void setLoadsImagesAutomatically(boolean loadsImagesAutomatically) {
        this.loadsImagesAutomatically = loadsImagesAutomatically;
    }
}
