package org.edx.mobile.view;

import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import org.assertj.android.api.Assertions;
import org.edx.mobile.R;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.view.dialog.WebViewDialogActivity;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowWebView;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class WebViewDialogActivityTest extends BaseTestCase {

    /**
     * Testing method for displaying web view dialog
     */
    @Test
    public void test_StartWebViewDialogActivity_LoadsUrlAndShowsTitle() {
        final String url = "https://www.edx.org";
        final String title = "title";
        test_StartWebViewDialogActivity_LoadsUrlAndShowsTitle(url, title);
        test_StartWebViewDialogActivity_LoadsUrlAndShowsTitle(url, null);
    }

    /**
     * Generic method for testing proper display of WebViewDialogFragment
     *
     * @param url   The url to load
     * @param title The title to show, if any
     */
    protected static void test_StartWebViewDialogActivity_LoadsUrlAndShowsTitle(String url, String title) {
        final WebViewDialogActivity activity =
                Robolectric.buildActivity(WebViewDialogActivity.class)
                        .withIntent(WebViewDialogActivity.newIntent(RuntimeEnvironment.application, url, title)).setup().get();
        final View dialogView = Shadows.shadowOf(activity).getContentView();
        assertNotNull(dialogView);
        final WebView webView = (WebView) dialogView.findViewById(R.id.eula_webView);
        assertNotNull(webView);
        final ShadowWebView shadowWebView = Shadows.shadowOf(webView);
        assertEquals(shadowWebView.getLastLoadedUrl(), url);
        final TextView titleView = (TextView) dialogView.findViewById(R.id.tv_dialog_title);
        assertNotNull(titleView);
        if (TextUtils.isEmpty(title)) {
            Assertions.assertThat(titleView).isNotVisible();
        } else {
            Assertions.assertThat(titleView).isVisible();
            Assertions.assertThat(titleView).hasText(title);
        }
        dialogView.findViewById(R.id.positiveButton).performClick();
        assertTrue(activity.isFinishing());
    }
}
