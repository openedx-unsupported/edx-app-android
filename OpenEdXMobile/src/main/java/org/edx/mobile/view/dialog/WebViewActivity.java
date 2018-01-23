package org.edx.mobile.view.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

public class WebViewActivity extends BaseFragmentActivity {

    private static final String ARG_URL = "url";
    private static final String ARG_TITLE = "title";

    WebView webView;

    public static Intent newIntent(@NonNull Context context, @NonNull String url, @Nullable String title) {
        return new Intent(context, WebViewActivity.class)
                .putExtra(ARG_URL, url)
                .putExtra(ARG_TITLE, title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        super.setToolbarAsActionBar();

        final ProgressBar progress = (ProgressBar) findViewById(R.id.loading_indicator);
        progress.setVisibility(View.GONE);

        webView = (WebView) findViewById(R.id.webView);
        final URLInterceptorWebViewClient client =
                new URLInterceptorWebViewClient(this, webView);
        client.setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {

            @Override
            public void onPageStarted() {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished() {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onPageLoadError(WebView view, int errorCode, String description, String failingUrl) {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onPageLoadError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse,
                                        boolean isMainRequestFailure) {
                if (isMainRequestFailure) {
                    progress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPagePartiallyLoaded() {
                progress.setVisibility(View.GONE);
            }
        });


        webView.loadUrl(getIntent().getStringExtra(ARG_URL));

        final String title = getIntent().getStringExtra(ARG_TITLE);
        if (!TextUtils.isEmpty(title)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(title);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        webView.destroy();
    }
}
