package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.EnrolledInCourseEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.util.links.DefaultActionListener;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WebViewProgramFragment extends AuthenticatedWebViewFragment {

    @Inject
    IEdxEnvironment environment;

    private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;
    private boolean refreshOnResume = false;

    public static Fragment newInstance(@NonNull String url) {
        final Fragment fragment = new WebViewProgramFragment();
        fragment.setArguments(makeArguments(url, null, true));
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isSystemUpdatingWebView()) {
            getBinding().authWebview.getWebViewClient().setActionListener(new DefaultActionListener(requireActivity(),
                    getBinding().authWebview.getProgressWheel(), new DefaultActionListener.EnrollCallback() {
                @Override
                public void onResponse(@NonNull EnrolledCoursesResponse course) {

                }

                @Override
                public void onFailure(@NonNull Throwable error) {

                }

                @Override
                public void onUserNotLoggedIn(@NonNull String courseId, boolean emailOptIn) {

                }
            }));

            getBinding().authWebview.getWebViewClient().setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
                @Override
                public void onPageStarted() {
                }

                @Override
                public void onPageFinished() {
                    getBinding().swipeContainer.setRefreshing(false);
                    tryEnablingSwipeContainer();
                }

                @Override
                public void onPageLoadError(WebView view, int errorCode, String description, String failingUrl) {
                    onPageFinished();
                }

                @Override
                public void onPageLoadError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse, boolean isMainRequestFailure) {
                    onPageFinished();
                }

                @Override
                public void onPageLoadProgressChanged(WebView webView, int progress) {

                }
            });

            tryEnablingSwipeContainer();
            UiUtils.INSTANCE.setSwipeRefreshLayoutColors(getBinding().swipeContainer);
            getBinding().swipeContainer.setOnRefreshListener(() -> {
                // We already have spinner inside the WebView, so we don't need the SwipeRefreshLayout's spinner
                getBinding().swipeContainer.setEnabled(false);
                getBinding().authWebview.loadUrl(true, getBinding().authWebview.getWebView().getUrl());
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isSystemUpdatingWebView()) {
            /*
            SwipeRefreshLayout intercepts and acts upon the scroll even when its child layout hasn't
            scrolled to its top, which leads to refresh logic happening and spinner appearing mid-scroll.
            With the following logic, we are forcing the SwipeRefreshLayout to use the scroll only when
            the underlying WebView has scrolled to its top.
            More info can be found on this SO question: https://stackoverflow.com/q/24658428/1402616
             */
            getBinding().swipeContainer.getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener = () -> {
                if (!tryEnablingSwipeContainer())
                    getBinding().swipeContainer.setEnabled(false);
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!isSystemUpdatingWebView()) {
            getBinding().swipeContainer.getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isSystemUpdatingWebView() && refreshOnResume) {
            refreshOnResume = false;
            // Swipe refresh shouldn't work while the page is refreshing
            getBinding().swipeContainer.setEnabled(false);
            getBinding().authWebview.loadUrl(true, getBinding().authWebview.getWebView().getUrl());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        getBinding();
        OfflineSupportUtils.setUserVisibleHint(getActivity(), isVisibleToUser,
                getBinding().authWebview.isShowingError());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        if (!isSystemUpdatingWebView() && getActivity() != null) {
            if (!tryEnablingSwipeContainer()) {
                //Disable swipe functionality and hide the loading view
                getBinding().swipeContainer.setEnabled(false);
                getBinding().swipeContainer.setRefreshing(false);
            }
            OfflineSupportUtils.onNetworkConnectivityChangeEvent(getActivity(), getUserVisibleHint(),
                    getBinding().authWebview.isShowingError());
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEventMainThread(EnrolledInCourseEvent event) {
        refreshOnResume = true;
    }

    @Override
    protected void onRevisit() {
        tryEnablingSwipeContainer();
        OfflineSupportUtils.onRevisit(getActivity());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Tries enabling the swipeContainer if certain conditions are met and tells the caller
     * if it was enabled or not.
     *
     * @return <code>true</code> if swipeContainer was enabled, <code>false</code> otherwise.
     */
    private boolean tryEnablingSwipeContainer() {
        if (!isSystemUpdatingWebView() && getActivity() != null) {
            if (NetworkUtil.isConnected(getActivity())
                    && !getBinding().authWebview.isShowingError()
                    && getBinding().authWebview.getProgressWheel().getVisibility() != View.VISIBLE
                    && getBinding().authWebview.getWebView().getScrollY() == 0) {
                getBinding().swipeContainer.setEnabled(true);
                return true;
            }
        }
        return false;
    }
}
