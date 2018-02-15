package org.edx.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentWebviewCourseDiscoveryBinding;
import org.edx.mobile.event.MainDashboardRefreshEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.greenrobot.event.EventBus;

public class WebViewDiscoverCoursesFragment extends BaseWebViewDiscoverFragment {
    private FragmentWebviewCourseDiscoveryBinding binding;
    private SearchView searchView;
    private MainDashboardToolbarCallbacks toolbarCallbacks;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_webview_course_discovery, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUrl(getInitialUrl());
        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarCallbacks = (MainDashboardToolbarCallbacks) getActivity();
        initSearchView();
    }

    private void initSearchView() {
        searchView = toolbarCallbacks.getSearchView();
        searchView.setQueryHint(getResources().getString(R.string.search_for_courses));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                String baseUrl = environment.getConfig().getCourseDiscoveryConfig().getCourseSearchUrl();
                String searchUrl = buildQuery(baseUrl, query, logger);
                searchView.onActionViewCollapsed();
                loadUrl(searchUrl);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    toolbarCallbacks.getTitleView().setVisibility(View.VISIBLE);
                    searchView.onActionViewCollapsed();
                } else {
                    toolbarCallbacks.getTitleView().setVisibility(View.GONE);
                }
            }
        });
    }

    @NonNull
    protected String getInitialUrl() {
        return environment.getConfig().getCourseDiscoveryConfig().getCourseSearchUrl();
    }

    public static String buildQuery(@NonNull String baseUrl, @NonNull String query, @NonNull Logger logger) {
        String encodedQuery = null;
        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        }
        String searchTerm = "search_query=" + encodedQuery;

        String searchUrl;
        if (baseUrl.contains("?")) {
            searchUrl = baseUrl + "&" + searchTerm;
        } else {
            searchUrl = baseUrl + "?" + searchTerm;
        }
        return searchUrl;
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (searchView != null) {
            searchView.setVisibility(isVisibleToUser ? View.VISIBLE : View.GONE);
            searchView.setIconified(!isVisibleToUser);
        }
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return errorNotification != null && errorNotification.isShowing();
    }
}
