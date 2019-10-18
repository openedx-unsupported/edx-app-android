package org.edx.mobile.view;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.util.links.WebViewLink;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import roboguice.inject.InjectView;

import static org.edx.mobile.util.links.WebViewLink.Authority.ENROLLED_COURSE_INFO;

public class CourseUpgradeWebViewFragment extends AuthenticatedWebViewFragment {
    @InjectView(R.id.loading_indicator)
    private ProgressBar progressWheel;

    public static Fragment newInstance(@NonNull String url, @Nullable String javascript, boolean isManuallyReloadable) {
        final Fragment fragment = new CourseUpgradeWebViewFragment();
        fragment.setArguments(makeArguments(url, javascript, isManuallyReloadable));
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isSystemUpdatingWebView()) {
            /*
             * Setting the host explicitly to fix a problem on Marshmallow and above devices where
             * the host is resolved after a number of redirections happen within the webview, which
             * causes the webview to open phone's browser upon the click of Place Order button.
             * This is not the case in the pre-Marshmallow devices where the host is resolved correctly.
             *
             * Note: The host resolution happens URLInterceptorWebViewClient class's onPageStarted
             * function which we are overriding in this specific case.
             */
            authWebView.getWebViewClient().setHostForThisPage(Uri.parse(getArguments().getString(ARG_URL)).getHost());

            authWebView.getWebViewClient().setActionListener(helper -> {
                if (getActivity() != null) {
                    // This means that the transaction completed successfully and user tapped on the
                    // WebView's `View Course` button.
                    if (helper.authority == ENROLLED_COURSE_INFO) {
                        getActivity().finish();
                    }
                }
            });
        }
    }
}
