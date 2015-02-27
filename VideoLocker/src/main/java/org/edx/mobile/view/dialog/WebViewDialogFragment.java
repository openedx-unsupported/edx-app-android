package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

public class WebViewDialogFragment extends DialogFragment {
    private final Logger logger = new Logger(getClass().getName());

    private static final String TAG = WebViewDialogFragment.class.getCanonicalName();

    private String strFileName;
    private boolean showTitle;
    private String strDialogTitle;
    private ProgressBar progressBar;
    
    public WebViewDialogFragment() {
    }   
    
    public void setDialogContents(String fileName, boolean showTitle, String dialogTitle){
        this.strFileName = fileName;
        this.showTitle = showTitle;
        this.strDialogTitle = dialogTitle;
    }   

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_web_dialog,
                container, false);

        progressBar = (ProgressBar) v.findViewById(R.id.progress);

        try{
            WebView webView = (WebView)v.findViewById(R.id.eula_webView);
            URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(webView) {

                @Override
                public void onOpenExternalURL(String url) {
                    // open URL in external browser
                    BrowserUtil.open(getActivity(), url);
                }
            };
            client.setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {

                @Override
                public void onPageStarted() {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onPageLoadError() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onPagePartiallyLoaded() {
                    //Nothing to do here
                }
            });

            if(strFileName !=null){
                webView.loadUrl(strFileName);
            }
            ETextView txtDialogTitle = (ETextView)v.findViewById(R.id.tv_dialog_title);
            View viewSeperator = v.findViewById(R.id.view_seperator);
            if(showTitle){
                txtDialogTitle.setVisibility(View.VISIBLE);
                viewSeperator.setVisibility(View.VISIBLE);
                if(strDialogTitle !=null){
                    txtDialogTitle.setText(strDialogTitle);
                }
            }else{
                txtDialogTitle.setVisibility(View.INVISIBLE);
                viewSeperator.setVisibility(View.INVISIBLE);
            }
        }catch(Exception e){
            logger.error(e);
        }

        // Watch for button clicks.
        Button btnPositive = (Button) v.findViewById(R.id.positiveButton);
        btnPositive.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Check if the dialog is not removing(dismissing)
                // or is visible before dismissing the dialog
                if (!isRemoving() && isVisible())
                    dismiss();
            }
        });

        return v;
    }
}