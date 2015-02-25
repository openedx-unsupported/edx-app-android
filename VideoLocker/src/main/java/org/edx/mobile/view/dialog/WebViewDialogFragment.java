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

    private String fileName;
    private boolean showTitle;
    private String dialogTitle;
    private ProgressBar progress;
    
    public WebViewDialogFragment() {
    }   
    
    public void setDialogContents(String fileName, boolean showTitle, String dialogTitle){
        this.fileName = fileName;
        this.showTitle = showTitle;
        this.dialogTitle = dialogTitle;
    }   

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_web_dialog,
                container, false);

        progress = (ProgressBar) v.findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

        try{
            WebView webView = (WebView)v.findViewById(R.id.eula_webView);
            URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(webView) {

                @Override
                public void onOpenExternalURL(String url) {
                    progress.setVisibility(View.GONE);

                    // open URL in external browser
                    BrowserUtil.open(getActivity(), url);
                }
            };
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
                public void onPageLoadError() {
                    progress.setVisibility(View.GONE);
                }

                @Override
                public void onPagePartiallyLoaded() {
                    progress.setVisibility(View.GONE);
                }
            });

            if(fileName!=null){
                webView.loadUrl(fileName);
            }
            ETextView tv_dialog_title = (ETextView)v.findViewById(R.id.tv_dialog_title);
            View viewSeperator = v.findViewById(R.id.view_seperator);
            if(showTitle){
                tv_dialog_title.setVisibility(View.VISIBLE);
                viewSeperator.setVisibility(View.VISIBLE);
                if(dialogTitle!=null){
                    tv_dialog_title.setText(dialogTitle);
                }
            }else{
                tv_dialog_title.setVisibility(View.INVISIBLE);
                viewSeperator.setVisibility(View.INVISIBLE);
            }
        }catch(Exception e){
            logger.error(e);
        }

        // Watch for button clicks.
        Button button = (Button) v.findViewById(R.id.positiveButton);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Check if the dialog is not removing(dismissing)
                // or is visible before dismissing the dialog
                if(!isRemoving() && isVisible())
                    dismiss();
            }
        });

        return v;
    }
}