package org.edx.mobile.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.databinding.FragmentWebviewDiscoveryBinding;
import org.edx.mobile.event.MainDashboardRefreshEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.util.UrlUtil;
import org.edx.mobile.util.links.DefaultActionListener;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WebViewDiscoverFragment extends BaseWebViewFragment {
    private static final String INSTANCE_CURRENT_DISCOVER_URL = "current_discover_url";

    protected FragmentWebviewDiscoveryBinding binding;
    private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWebviewDiscoveryBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setWebViewActionListener();
        setWebViewBackPressListener();

        // Check for search query in extras
        String searchQueryExtra = null;
        String searchUrl = null;

        if (savedInstanceState != null) {
            searchUrl = savedInstanceState.getString(INSTANCE_CURRENT_DISCOVER_URL, null);
        }
        if (searchUrl == null && getArguments() != null) {
            searchQueryExtra = getArguments().getString(Router.EXTRA_SEARCH_QUERY);
        }

        if (!TextUtils.isEmpty(searchQueryExtra)) {
            initSearch(searchQueryExtra);
        } else {
            loadUrl(searchUrl == null || !URLUtil.isValidUrl(searchUrl) ? getInitialUrl() : searchUrl);
        }

        binding.swipeContainer.setOnRefreshListener(() -> {
            loadUrl(binding.webview.getUrl());
            binding.swipeContainer.setRefreshing(false);
        });
        UiUtils.INSTANCE.setSwipeRefreshLayoutColors(binding.swipeContainer);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void setWebViewActionListener() {
        client.setActionListener(new DefaultActionListener(getActivity(), progressWheel,
                new DefaultActionListener.EnrollCallback() {
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
    }

    private void setWebViewBackPressListener() {
        binding.webview.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && binding.webview.canGoBack()) {
                    binding.webview.goBack();
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (URLUtil.isValidUrl(binding.webview.getUrl())) {
            // Saving the url to maintain the filtered state of the screen if user has applied it
            outState.putString(INSTANCE_CURRENT_DISCOVER_URL, binding.webview.getUrl());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public FullScreenErrorNotification initFullScreenErrorNotification() {
        return new FullScreenErrorNotification(binding.llContent);
    }

    private void initSearch(@NonNull String query) {
        String baseUrl = getInitialUrl();
        if (baseUrl.contains(UrlUtil.QUERY_PARAM_SEARCH)) {
            baseUrl = UrlUtil.removeQueryParameterFromURL(baseUrl, UrlUtil.QUERY_PARAM_SEARCH);
        }
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put(UrlUtil.QUERY_PARAM_SEARCH, query);
        loadUrl(UrlUtil.buildUrlWithQueryParams(logger, baseUrl, queryParams));
    }

    @NonNull
    protected String getInitialUrl() {
        return URLUtil.isValidUrl(binding.webview.getUrl()) ? binding.webview.getUrl() :
                environment.getConfig().getDiscoveryConfig().getBaseUrl();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(MainDashboardRefreshEvent event) {
        loadUrl(getInitialUrl());
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(new MainDashboardRefreshEvent());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        onNetworkConnectivityChangeEvent(event);
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return errorNotification != null && errorNotification.isShowing();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.swipeContainer.getViewTreeObserver().addOnScrollChangedListener(
                onScrollChangedListener = () -> {
                    binding.swipeContainer.setEnabled((binding.webview.getScrollY() == 0));
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.swipeContainer.getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
    }
}
