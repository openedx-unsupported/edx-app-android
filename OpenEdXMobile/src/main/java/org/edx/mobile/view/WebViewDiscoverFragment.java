package org.edx.mobile.view;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.TextView;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentWebviewDiscoveryBinding;
import org.edx.mobile.event.DiscoveryTabSelectedEvent;
import org.edx.mobile.event.MainDashboardRefreshEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.UrlUtil;
import org.edx.mobile.util.links.DefaultActionListener;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.util.UrlUtil.QUERY_PARAM_SEARCH;
import static org.edx.mobile.util.UrlUtil.buildUrlWithQueryParams;

/**
 * An abstract fragment providing basic functionality of searching the webpage via toolbar searchview.
 */
public abstract class WebViewDiscoverFragment extends BaseWebViewFragment {
    private static final String INSTANCE_CURRENT_DISCOVER_URL = "current_discover_url";

    protected FragmentWebviewDiscoveryBinding binding;
    private SearchView searchView;
    private ToolbarCallbacks toolbarCallbacks;

    protected abstract String getSearchUrl();

    protected abstract int getQueryHint();

    protected abstract boolean isSearchEnabled();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_webview_discovery, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setWebViewActionListener();
        // Check for search query in extras
        String searchQueryExtra = null;
        String searchUrl = null;

        if (savedInstanceState != null) {
            searchUrl = savedInstanceState.getString(INSTANCE_CURRENT_DISCOVER_URL, null);
        }
        if (searchUrl == null && getArguments() != null) {
            searchQueryExtra = getArguments().getString(Router.EXTRA_SEARCH_QUERY);
        }

        if (searchQueryExtra != null) {
            initSearch(searchQueryExtra);
        } else {
            loadUrl(searchUrl == null || !URLUtil.isValidUrl(searchUrl) ? getInitialUrl() : searchUrl);
        }
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
        if (baseUrl.contains(QUERY_PARAM_SEARCH)) {
            baseUrl = UrlUtil.removeQueryParameterFromURL(baseUrl, QUERY_PARAM_SEARCH);
        }
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put(QUERY_PARAM_SEARCH, query);
        loadUrl(buildUrlWithQueryParams(logger, baseUrl, queryParams));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarCallbacks = getActivity() instanceof ToolbarCallbacks ?
                (ToolbarCallbacks) getActivity() : null;
        initSearchView();
    }

    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            initSearch(query);
            searchView.onActionViewCollapsed();
            final boolean isLoggedIn = environment.getLoginPrefs().getUsername() != null;
            environment.getAnalyticsRegistry().trackCoursesSearch(query, isLoggedIn, BuildConfig.VERSION_NAME);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    private SearchView.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean queryTextFocused) {
            if (!queryTextFocused) {
                updateTitleVisibility(View.VISIBLE);
                searchView.onActionViewCollapsed();
            } else {
                updateTitleVisibility(View.GONE);
            }
        }
    };

    private void initSearchView() {
        searchView = toolbarCallbacks.getSearchView();
        if (getUserVisibleHint()) {
            setupSearchViewListeners();
        }
        if (searchView.hasFocus()) {
            updateTitleVisibility(View.GONE);
        }
    }

    private void setupSearchViewListeners() {
        searchView.setQueryHint(getResources().getString(getQueryHint()));
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setOnQueryTextFocusChangeListener(onFocusChangeListener);
        setupEmptyQuerySubmitListener();
    }

    private void setupEmptyQuerySubmitListener() {
        // Inspiration: https://github.com/Foso/Notes/blob/master/Android/EmptySubmitSearchView.java
        SearchView.SearchAutoComplete searchSrcTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchSrcTextView.setOnEditorActionListener((textView, actionId, event) ->
                onQueryTextListener.onQueryTextSubmit(searchView.getQuery().toString()));
    }

    private void updateTitleVisibility(int visibility) {
        final TextView titleView = toolbarCallbacks != null ? toolbarCallbacks.getTitleView() : null;
        if (titleView != null) {
            titleView.setVisibility(visibility);
        }
    }

    @NonNull
    protected String getInitialUrl() {
        return URLUtil.isValidUrl(binding.webview.getUrl()) ?
                binding.webview.getUrl() : getSearchUrl();
    }

    @SuppressWarnings("unused")
    public void onEvent(MainDashboardRefreshEvent event) {
        loadUrl(getInitialUrl());
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(new MainDashboardRefreshEvent());
    }

    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        onNetworkConnectivityChangeEvent(event);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull DiscoveryTabSelectedEvent event) {
        // OfflineSupportBaseFragment.setUserVisibleHint(*) should be called automatically whenever
        // the fragment visibility is changed to user but in the case of WebViewDiscoverCoursesFragment
        // & WebViewDiscoverProgramsFragment setUserVisibleHint is not getting called on tab selection
        // that's why we need to call it manually.
        if (!isHidden()) {
            setUserVisibleHint(getUserVisibleHint());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        onFragmentVisibilityChange(isVisibleToUser);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        onFragmentVisibilityChange(!hidden);
    }

    private void onFragmentVisibilityChange(boolean isVisible) {
        if (searchView != null) {
            if (isVisible && isSearchEnabled()) {
                searchView.setVisibility(View.VISIBLE);
                setupSearchViewListeners();
            } else {
                searchView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return errorNotification != null && errorNotification.isShowing();
    }
}
