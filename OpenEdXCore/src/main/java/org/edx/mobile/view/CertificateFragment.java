package org.edx.mobile.view;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.util.HashMap;
import java.util.Map;

import org.edx.mobile.base.BaseFragment;
import roboguice.inject.InjectExtra;

public class CertificateFragment extends BaseFragment {

    static public final String TAG = CertificateFragment.class.getCanonicalName();
    static public final String ENROLLMENT = "enrollment";

    @Inject
    private ISegment segIO;

    @InjectExtra(ENROLLMENT)
    EnrolledCoursesResponse courseData;

    private WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        segIO.trackScreenView(ISegment.Screens.CERTIFICATE);
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
                        ShareUtils.newShareIntent(shareText),
                        getActivity().findViewById(R.id.menu_item_share),
                        new ShareUtils.ShareMenuItemListener() {
                            @Override
                            public void onMenuItemClick(@NonNull ComponentName componentName, @NonNull ShareUtils.ShareType shareType) {
                                segIO.certificateShared(courseData.getCourse().getId(), courseData.getCertificateURL(), shareType);
                                final Intent intent = ShareUtils.newShareIntent(shareText);
                                intent.setComponent(componentName);
                                startActivity(intent);
                            }
                        },
                R.string.share_certificate_popup_header);
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
            public void onPageLoadError() {
                loadingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onPagePartiallyLoaded() {
                loadingIndicator.setVisibility(View.GONE);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Clear cookies before loading so that social sharing buttons are not displayed inside web view
        EdxCookieManager.getSharedInstance().clearWebWiewCookie(getActivity());

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
