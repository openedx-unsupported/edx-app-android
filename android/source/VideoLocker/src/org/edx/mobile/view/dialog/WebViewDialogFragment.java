package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.view.custom.ETextView;

public class WebViewDialogFragment extends DialogFragment {
    private final Logger logger = new Logger(getClass().getName());

    private static final String TAG = WebViewDialogFragment.class.getCanonicalName();

    String fileName;
    boolean showTitle;
    String dialogTitle;
    
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
        try{
            WebView webView = (WebView)v.findViewById(R.id.eula_webView);
            webView.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                    BrowserUtil.open(getActivity(), url);
                    return true;
                }
            });
            if(fileName!=null){
                webView.loadUrl(fileName);
                //eulaWeb.loadUrl("file:///android_asset/EULA.htm");
            }
            ETextView tv_dialog_title = (ETextView)v.findViewById(R.id.tv_dialog_title);
            View view_seperator = (View)v.findViewById(R.id.view_seperator);
            if(showTitle){
                tv_dialog_title.setVisibility(View.VISIBLE);
                view_seperator.setVisibility(View.VISIBLE);
                if(dialogTitle!=null){
                    tv_dialog_title.setText(dialogTitle);
                }
            }else{
                tv_dialog_title.setVisibility(View.INVISIBLE);
                view_seperator.setVisibility(View.INVISIBLE);
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