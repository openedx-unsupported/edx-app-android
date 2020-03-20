package org.edx.mobile.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.event.CourseUpgradedEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

import static org.edx.mobile.util.links.WebViewLink.Authority.ENROLLED_COURSE_INFO;

public class CourseUpgradeWebViewFragment extends AuthenticatedWebViewFragment {
    @Inject
    AnalyticsRegistry analyticsRegistry;

    @InjectView(R.id.loading_indicator)
    private ProgressBar progressWheel;

    public static Fragment newInstance(@NonNull String url, @Nullable String javascript,
                                       boolean isManuallyReloadable,
                                       @NonNull EnrolledCoursesResponse courseData,
                                       @Nullable CourseComponent unit) {
        final Fragment fragment = new CourseUpgradeWebViewFragment();
        final Bundle bundle = makeArguments(url, javascript, isManuallyReloadable);
        bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        if (unit != null) {
            bundle.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        }
        fragment.setArguments(bundle);
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

            authWebView.getWebViewClient().addInternalLinkHost("https://payment.edx.org");

            authWebView.getWebViewClient().setActionListener(helper -> {
                if (getActivity() != null) {
                    // This means that the transaction completed successfully and user tapped on the
                    // WebView's `View Course` button.
                    if (helper.authority == ENROLLED_COURSE_INFO) {
                        onCoursePaymentSuccessful();
                    }
                }
            });
        }
        analyticsRegistry.trackScreenView(Analytics.Screens.PLACE_ORDER_COURSE_UPGRADE);
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
                onCoursePaymentSuccessful();
            }
        }
    }

    public void onCoursePaymentSuccessful() {
        // Finish activity
        final Activity activity = getActivity();
        if( activity != null) {
            activity.finish();
        }

        final Bundle arguments = getArguments();
        if (arguments != null) {
            final EnrolledCoursesResponse courseData =
                    (EnrolledCoursesResponse) arguments.getSerializable(Router.EXTRA_COURSE_DATA);

            // Fire Course Upgraded event
            EventBus.getDefault().postSticky(new CourseUpgradedEvent(courseData.getCourse().getId()));

            // Fire analytics
            final CourseComponent courseUnit = (CourseComponent) arguments.getSerializable(Router.EXTRA_COURSE_UNIT);
            if (courseUnit != null) {
                analyticsRegistry.trackCourseUpgradeSuccess(courseUnit.getId(),
                        courseData.getCourse().getId(), courseUnit.getBlockId());
            } else {
                analyticsRegistry.trackCourseUpgradeSuccess(null,
                        courseData.getCourse().getId(), null);
            }
        } else {
            // Fire Course Upgraded event
            EventBus.getDefault().postSticky(new CourseUpgradedEvent(null));
        }
    }
}
