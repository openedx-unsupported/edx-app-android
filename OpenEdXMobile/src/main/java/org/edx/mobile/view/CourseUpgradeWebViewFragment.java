package org.edx.mobile.view;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.ProgressBar;

import org.edx.mobile.R;

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

    @Override
    public void onStop() {
        super.onStop();
        /*
         * Workaround to handle the case where a user has successfully made the payment and has
         * reached the receipt page and somehow they leave the screen and OS decides to destroy the
         * activity.
         * If this workaround is not in place, upon return to the app, the user will again see the
         * payment page presenting them with a form for the payment to upgrade a course.
         *
         * Steps to reproduce the issue:
         * 1 - Set up don't keep activities to true from developer options.
         * 2 - Upgrade the course by placing the order and screen will redirect to Thank you page.
         * 3 - Move the app to background and then back to foreground.
         * 4 - App opens the place order screen instated of a thank you screen.
         */
        if (getActivity() != null && authWebView.getWebView() != null) {
            final String url = authWebView.getWebView().getUrl();
            if (url != null && url.contains("checkout/receipt")) {
                getActivity().finish();
            }

        }
    }
}
