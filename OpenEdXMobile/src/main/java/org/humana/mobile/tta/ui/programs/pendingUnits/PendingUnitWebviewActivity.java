package org.humana.mobile.tta.ui.programs.pendingUnits;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.humana.mobile.R;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.library.LibraryFragment;


public class PendingUnitWebviewActivity  extends AppCompatActivity {

    public static final String TAG = LibraryFragment.class.getCanonicalName();
    WebView webView;
    final String url_to_load = "http://35.154.251.7/xblock/";
    String url;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_unit_web);
        webView = findViewById(R.id.web_view);
        Intent in = getIntent();
        url = in.getStringExtra("BlockId");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
            @Override
            public void onPageFinished(WebView view, final String url) {
            }
        });

        webView.loadUrl(url_to_load+url);

    }


}