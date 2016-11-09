package org.edx.mobile.view;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;

import org.edx.mobile.R;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.view.dialog.WebViewActivity;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowWebView;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.assertj.android.appcompat.v7.api.Assertions.assertThat;

public class WebViewActivityTest extends BaseTestCase {

    /**
     * Testing method for displaying web view dialog
     */
    // TODO: Convert to Parameterized test once we have a Robolectric parameterized test runner.
    @Test
    public void test_StartWebViewActivity_LoadsUrlAndShowsTitle()
            throws PackageManager.NameNotFoundException {
        final String url = "https://www.edx.org";
        final String title = "title";
        test_StartWebViewActivity_LoadsUrlAndShowsTitle(url, title);
        test_StartWebViewActivity_LoadsUrlAndShowsTitle(url, null);
    }

    /**
     * Generic method for testing proper display of WebViewDialogFragment
     *
     * @param url   The url to load
     * @param title The title to show, if any
     */
    private void test_StartWebViewActivity_LoadsUrlAndShowsTitle(@NonNull String url,
                                                                 @Nullable String title)
            throws PackageManager.NameNotFoundException {
        final WebViewActivity activity =
                Robolectric.buildActivity(WebViewActivity.class)
                        .withIntent(WebViewActivity.newIntent(
                                RuntimeEnvironment.application, url, title)).setup().get();
        final View contentView = Shadows.shadowOf(activity).getContentView();
        assertNotNull(contentView);
        final WebView webView = (WebView) contentView.findViewById(R.id.webView);
        assertNotNull(webView);
        final ShadowWebView shadowWebView = Shadows.shadowOf(webView);
        assertEquals(shadowWebView.getLastLoadedUrl(), url);
        final ActionBar actionBar = activity.getSupportActionBar();
        assertNotNull(actionBar);
        assertThat(actionBar).isShowing();
        if (!TextUtils.isEmpty(title)) {
            assertThat(actionBar).hasTitle(title);
        }
        /*
        Robolectric is not providing the correct default title which is why this code has
        been commented.

        else {
            final PackageManager pm = activity.getPackageManager();
            final ActivityInfo aInfo = pm.getActivityInfo(activity.getComponentName(), 0);
            final String defaultTitle = aInfo.loadLabel(pm).toString();
            assertThat(actionBar).hasTitle(defaultTitle);
        }
        */
    }
}
