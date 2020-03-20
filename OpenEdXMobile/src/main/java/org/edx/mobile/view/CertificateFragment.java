package org.edx.mobile.view;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.util.HashMap;
import java.util.Map;

import roboguice.inject.InjectExtra;

public class CertificateFragment extends BaseFragment {

    static public final String TAG = CertificateFragment.class.getCanonicalName();
    static public final String ENROLLMENT = "enrollment";

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @InjectExtra(ENROLLMENT)
    EnrolledCoursesResponse courseData;

    private WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsRegistry.trackScreenView(Analytics.Screens.CERTIFICATE);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_certificate, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share: {
                final Map<String, CharSequence> shareTextParams = new HashMap<>();
                shareTextParams.put("platform_name", getString(R.string.platform_name));
                shareTextParams.put("certificate_url", courseData.getCertificateURL());
                final String shareText = ResourceUtil.getFormattedString(getResources(), R.string.share_certificate_message, shareTextParams).toString();
                ShareUtils.showShareMenu(
                        getActivity(),
                        ShareUtils.newShareIntent(shareText),
                        getActivity().findViewById(R.id.menu_item_share),
                        new ShareUtils.ShareMenuItemListener() {
                            @Override
                            public void onMenuItemClick(@NonNull ComponentName componentName, @NonNull ShareUtils.ShareType shareType) {
                                analyticsRegistry.certificateShared(courseData.getCourse().getId(), courseData.getCertificateURL(), shareType);
                                final Intent intent = ShareUtils.newShareIntent(shareText);
                                intent.setComponent(componentName);
                                startActivity(intent);
                            }
                        });
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_certificate, container, false);
        webview = (WebView) view.findViewById(R.id.webview);
        final View loadingIndicator = view.findViewById(R.id.loading_indicator);
        final URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(getActivity(), webview);
        client.setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
            @Override
            public void onPageStarted() {
                loadingIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished() {
                loadingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onPageLoadError(WebView view, int errorCode, String description, String failingUrl) {
                loadingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onPageLoadError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse,
                                        boolean isMainRequestFailure) {
                if (isMainRequestFailure) {
                    loadingIndicator.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageLoadProgressChanged(WebView view, int progress) {
                if (progress > AppConstants.PAGE_LOAD_THRESHOLD) loadingIndicator.setVisibility(View.GONE);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Clear cookies before loading so that social sharing buttons are not displayed inside web view
        EdxCookieManager.getSharedInstance(getContext()).clearWebWiewCookie();

        webview.loadUrl(courseData.getCertificateURL());
    }

    @Override
    public void onResume() {
        super.onResume();
        webview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webview.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webview.destroy();
    }
}
