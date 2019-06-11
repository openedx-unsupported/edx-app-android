package org.edx.mobile.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.event.UnitLoadedEvent;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.util.links.WebViewLink;
import org.edx.mobile.view.custom.AuthenticatedWebView;
import org.edx.mobile.view.custom.PreLoadingListener;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

public class CourseUnitWebViewFragment extends CourseUnitFragment {

    @InjectView(R.id.auth_webview)
    private AuthenticatedWebView authWebView;

    @InjectView(R.id.swipe_container)
    protected SwipeRefreshLayout swipeContainer;

    @Inject
    private UserPrefs pref;

    @Inject
    private IDownloadManager dm;

    private PreLoadingListener preloadingListener;
    private boolean isPageLoading = false;
    private String resourceUrl;
    private final int REQUEST_CODE = 1;

    public static CourseUnitWebViewFragment newInstance(HtmlBlockModel unit) {
        CourseUnitWebViewFragment fragment = new CourseUnitWebViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_authenticated_webview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof PreLoadingListener) {
            preloadingListener = (PreLoadingListener) getActivity();
        } else {
            throw new RuntimeException("Parent activity of this Fragment should implement the PreLoadingListener interface");
        }
        swipeContainer.setEnabled(false);
        authWebView.initWebView(getActivity(), true, false);
        authWebView.getWebViewClient().setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
            @Override
            public void onPageStarted() {
                isPageLoading = true;
            }

            @Override
            public void onPageFinished() {
                if (authWebView.isPageLoaded()) {
                    if (getUserVisibleHint()) {
                        preloadingListener.setLoadingState(PreLoadingListener.State.MAIN_UNIT_LOADED);
                    }
                    isPageLoading = false;
                    EventBus.getDefault().post(new UnitLoadedEvent());
                }
            }

            @Override
            public void onPageLoadError(WebView view, int errorCode, String description, String failingUrl) {
                isPageLoading = false;
            }

            @Override
            public void onPageLoadError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse, boolean isMainRequestFailure) {
                isPageLoading = false;
            }

            @Override
            public void onPageLoadProgressChanged(WebView webView, int progress) {
            }
        });

        authWebView.getWebViewClient().setActionListener(new URLInterceptorWebViewClient.ActionListener() {

            @Override
            public void onLinkRecognized(@NonNull WebViewLink helper) {

            }

            @Override
            public void downloadResource(String strUrl) {
                resourceUrl = strUrl;
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                } else {
                    final String fileName = strUrl.substring(strUrl.lastIndexOf("/") + 1);
                    dm.addDownload(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), strUrl, pref.isDownloadOverWifiOnly(), fileName);
                }
            }
        });

        // Only load the unit if it is currently visible to user or the visible unit has finished loading
        if (getUserVisibleHint() || preloadingListener.isMainUnitLoaded()) {
            loadUnit();
        }
    }

    private void loadUnit() {
        if (authWebView != null) {
            if (!authWebView.isPageLoaded() && !isPageLoading) {
                authWebView.loadUrl(true, unit.getBlockUrl());
                if (getUserVisibleHint()) {
                    preloadingListener.setLoadingState(PreLoadingListener.State.MAIN_UNIT_LOADING);
                }
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // Only load the unit if it is currently visible to user
        if (isVisibleToUser) {
            loadUnit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        authWebView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        authWebView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        authWebView.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        authWebView.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(UnitLoadedEvent event) {
        loadUnit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && !resourceUrl.isEmpty()) {
                    final String fileName = resourceUrl.substring(resourceUrl.lastIndexOf("/") + 1);
                    dm.addDownload(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), resourceUrl, pref.isDownloadOverWifiOnly(), fileName);
                }
                return;
            }
        }
    }
}
