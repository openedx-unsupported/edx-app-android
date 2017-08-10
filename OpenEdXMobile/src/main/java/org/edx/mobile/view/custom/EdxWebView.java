package org.edx.mobile.view.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.LocaleUtils;

import java.util.Locale;
import java.util.Map;

public class EdxWebView extends WebView {
    protected final Logger logger = new Logger(getClass().getName());

    @SuppressLint("SetJavaScriptEnabled")
    public EdxWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setUserAgentString(
                settings.getUserAgentString() + " " +
                        context.getString(R.string.app_name) + "/" +
                        BuildConfig.APPLICATION_ID + "/" +
                        BuildConfig.VERSION_NAME
        );
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        setLanguageCookie(url);
        super.loadUrl(url, additionalHttpHeaders);
    }

    @Override
    public void loadUrl(String url) {
        setLanguageCookie(url);
        super.loadUrl(url);
    }

    private void setLanguageCookie(final String url) {
        /*
         * Webview caches the previously loaded data and shows the cached data ignoring language
         * change (if it happens), so its essential that we clear cache before loading a URL.
         */
        clearCache(true);
        final String languageCode = LocaleUtils.getLanguageCodeFromLocale(Locale.getDefault());
        CookieManager.getInstance().setCookie(url,
                String.format("%s=%s", LocaleUtils.WEB_VIEW_LANGUAGE_COOKIE_NAME, languageCode));
    }
}
