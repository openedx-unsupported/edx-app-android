package org.humana.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.humana.mobile.R;
import org.humana.mobile.model.course.BlockType;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.services.ViewPagerDownloadManager;
import org.humana.mobile.util.BrowserUtil;
import org.humana.mobile.view.custom.AuthenticatedWebView;
import org.humana.mobile.view.custom.URLInterceptorWebViewClient;

/**
 *
 */
public class CourseUnitMobileNotSupportedFragment extends CourseUnitFragment {

    /**
     * Create a new instance of fragment
     */
    public static CourseUnitMobileNotSupportedFragment newInstance(CourseComponent unit) {
        CourseUnitMobileNotSupportedFragment f = new CourseUnitMobileNotSupportedFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_grade, container, false);
        AuthenticatedWebView webView = v.findViewById(R.id.auth_webview);

        ((TextView) v.findViewById(R.id.not_available_message)).setText(
                unit.getType() == BlockType.VIDEO ? R.string.video_only_on_web_short : R.string.assessment_not_available);
        v.findViewById(R.id.view_on_web_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowserUtil.open(getActivity(), unit.getWebUrl());
                environment.getAnalyticsRegistry().trackOpenInBrowser(unit.getId()
                        , unit.getCourseId(), unit.isMultiDevice(), unit.getBlockId());
            }
        });

        webView.initWebView(getActivity(), true, false);
        webView.getWebViewClient().setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
            @Override
            public void onPageStarted() {
            }

            @Override
            public void onPageFinished() {
                ViewPagerDownloadManager.instance.done(CourseUnitMobileNotSupportedFragment.this, true);
            }

            @Override
            public void onPageLoadError(WebView view, int errorCode, String description, String failingUrl) {
                if (failingUrl != null && failingUrl.equals(view.getUrl())) {
                    ViewPagerDownloadManager.instance.done(CourseUnitMobileNotSupportedFragment.this, false);
                }
            }

            @Override
            public void onPageLoadError(WebView view, WebResourceRequest request,
                                        WebResourceResponse errorResponse, boolean isMainRequestFailure) {
                if (isMainRequestFailure) {
                    ViewPagerDownloadManager.instance.done(CourseUnitMobileNotSupportedFragment.this,
                            false);
                }
            }

            @Override
            public void onPageLoadProgressChanged(WebView webView, int progress) {

            }
        });
        webView.loadUrl(false, unit.getBlockUrl());

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ViewPagerDownloadManager.instance.inInitialPhase(unit))
            ViewPagerDownloadManager.instance.addTask(this);
    }


    @Override
    public void run() {
        ViewPagerDownloadManager.instance.done(this, true);
    }

}