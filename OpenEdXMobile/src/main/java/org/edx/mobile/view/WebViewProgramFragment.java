package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.edx.mobile.R;
import org.edx.mobile.annotation.Nullable;
import org.edx.mobile.event.EnrolledInCourseEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.util.links.DefaultActionListener;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

public class WebViewProgramFragment extends AuthenticatedWebViewFragment {

    @Nullable
    @InjectView(R.id.loading_indicator)
    private ProgressBar progressWheel;

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
            authWebView.getWebViewClient().setActionListener(new DefaultActionListener(getActivity(),
                    progressWheel, new DefaultActionListener.EnrollCallback() {
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

            authWebView.getWebViewClient().setPageStatusListener(new URLInterceptorWebViewClient.IPageStatusListener() {
                @Override
                public void onPageStarted() {
                }

                @Override
                public void onPageFinished() {
                    swipeContainer.setRefreshing(false);
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
            UiUtil.setSwipeRefreshLayoutColors(swipeContainer);
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // We already have spinner inside the WebView, so we don't need the SwipeRefreshLayout's spinner
                    swipeContainer.setEnabled(false);
                    authWebView.loadUrl(true, authWebView.getWebView().getUrl());
                }
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
            swipeContainer.getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener =
                    new ViewTreeObserver.OnScrollChangedListener() {
                        @Override
                        public void onScrollChanged() {
                            if (!tryEnablingSwipeContainer())
                                swipeContainer.setEnabled(false);
                        }
                    });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!isSystemUpdatingWebView()) {
            swipeContainer.getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isSystemUpdatingWebView() && refreshOnResume) {
            refreshOnResume = false;
            // Swipe refresh shouldn't work while the page is refreshing
            swipeContainer.setEnabled(false);
            authWebView.loadUrl(true, authWebView.getWebView().getUrl());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        OfflineSupportUtils.setUserVisibleHint(getActivity(), isVisibleToUser,
                authWebView != null && authWebView.isShowingError());
    }

    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        if (!isSystemUpdatingWebView() && getActivity() != null) {
            if (!tryEnablingSwipeContainer()) {
                //Disable swipe functionality and hide the loading view
                swipeContainer.setEnabled(false);
                swipeContainer.setRefreshing(false);
            }
            OfflineSupportUtils.onNetworkConnectivityChangeEvent(getActivity(), getUserVisibleHint(),
                    authWebView != null && authWebView.isShowingError());
        }
    }

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
     * Tries enabling the {@link #swipeContainer} if certain conditions are met and tells the caller
     * if it was enabled or not.
     *
     * @return <code>true</code> if {@link #swipeContainer} was enabled, <code>false</code> otherwise.
     */
    private boolean tryEnablingSwipeContainer() {
        if (!isSystemUpdatingWebView() && getActivity() != null) {
            if (NetworkUtil.isConnected(getActivity())
                    && !authWebView.isShowingError()
                    && progressWheel.getVisibility() != View.VISIBLE
                    && authWebView.getWebView().getScrollY() == 0) {
                swipeContainer.setEnabled(true);
                return true;
            }
        }
        return false;
    }
}
