package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import org.edx.mobile.R;
import org.edx.mobile.view.custom.ETextView;

public class WebViewDialogFragment extends DialogFragment {
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
            e.printStackTrace();
        }

        // Watch for button clicks.
        Button button = (Button) v.findViewById(R.id.positiveButton);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }
}