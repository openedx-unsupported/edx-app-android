package org.humana.mobile.tta.ui.programs.pendingUnits;

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
    final String url_to_load = "http://35.154.251.7/courses/course-v1:Humana+NT101+NeTT_FNK-A/courseware/e17ff66471b74be4a677df8eaacf7abe/a218b35525494aa1ac3cdd7b518fd1fb/1?activate_block_id=block-v1%3AHumana%2BNT101%2BNeTT_FNK-A%2Btype%40vertical%2Bblock%40b5657e6309c24e74a68583f312062328";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_unit_web);
        webView = findViewById(R.id.web_view);

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

        webView.loadUrl(url_to_load);

    }


}