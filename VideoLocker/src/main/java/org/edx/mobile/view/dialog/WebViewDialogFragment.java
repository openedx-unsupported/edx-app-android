package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

public class WebViewDialogFragment extends AppCompatDialogFragment {

    private static final String ARG_URL = "url";
    private static final String ARG_TITLE = "title";

    public static WebViewDialogFragment newInstance(@NonNull String url, @Nullable String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_TITLE, title);
        final WebViewDialogFragment fragment = new WebViewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,
                R.style.AppTheme_NoActionBar);
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_web_dialog,
                container, false);

        final ProgressBar progress = (ProgressBar) view.findViewById(R.id.loading_indicator);
        progress.setVisibility(View.GONE);

        final WebView webView = (WebView) view.findViewById(R.id.eula_webView);
        final URLInterceptorWebViewClient client =
                new URLInterceptorWebViewClient(getActivity(), webView);
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


        webView.loadUrl(getArguments().getString(ARG_URL));

        final TextView tv_dialog_title = (TextView) view.findViewById(R.id.tv_dialog_title);
        final View viewSeperator = view.findViewById(R.id.view_seperator);
        final String title = getArguments().getString(ARG_TITLE);
        if (TextUtils.isEmpty(title)) {
            tv_dialog_title.setVisibility(View.INVISIBLE);
            viewSeperator.setVisibility(View.INVISIBLE);
        } else {
            tv_dialog_title.setVisibility(View.VISIBLE);
            viewSeperator.setVisibility(View.VISIBLE);
            tv_dialog_title.setText(title);
        }

        view.findViewById(R.id.positiveButton).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Check if the dialog is not removing(dismissing)
                // or is visible before dismissing the dialog
                if (!isRemoving() && isVisible())
                    dismiss();
            }
        });

        return view;
    }
}
