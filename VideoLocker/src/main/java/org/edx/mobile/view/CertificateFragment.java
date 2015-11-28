package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import roboguice.fragment.RoboFragment;

public class CertificateFragment extends RoboFragment {

    static public final String TAG = CertificateFragment.class.getCanonicalName();
    static public final String ENROLLMENT = "enrollment";

    @Inject
    private ISegment segIO;

    private WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        segIO.trackScreenView(ISegment.Screens.CERTIFICATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_certificate, container, false);
        webview = (WebView) view.findViewById(R.id.webview);
        final View loadingIndicator = view.findViewById(R.id.api_spinner);
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
        final EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getActivity().getIntent().getSerializableExtra(ENROLLMENT);

        // Clear cookies before loading so that social sharing buttons are not displayed inside web view
        EdxCookieManager.getSharedInstance().clearWebWiewCookie(getActivity());

        webview.loadUrl(courseData.getCertificateURL());
    }
}
