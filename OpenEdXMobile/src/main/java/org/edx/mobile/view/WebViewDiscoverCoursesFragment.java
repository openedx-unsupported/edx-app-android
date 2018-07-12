package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.webkit.URLUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentWebviewCourseDiscoveryBinding;
import org.edx.mobile.event.MainDashboardRefreshEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.model.SubjectModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.view.adapters.PopularSubjectsAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.util.UrlUtil.QUERY_PARAM_SEARCH;
import static org.edx.mobile.util.UrlUtil.QUERY_PARAM_SUBJECT;
import static org.edx.mobile.util.UrlUtil.buildUrlWithQueryParams;

public class WebViewDiscoverCoursesFragment extends BaseWebViewDiscoverFragment {
    private static final int VIEW_SUBJECTS_REQUEST_CODE = 999;

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

        errorNotification = new FullScreenErrorNotification(binding.llContent);

        loadUrl(getInitialUrl());
        if (shouldShowSubjectDiscovery()) {
            initSubjects();
        }
        EventBus.getDefault().register(this);
    }

    private void initSubjects() {
        final String subjectItemsJson;
        try {
            subjectItemsJson = FileUtil.loadTextFileFromResources(getContext(), R.raw.subjects);
            final Type type = new TypeToken<List<SubjectModel>>() {
            }.getType();
            final List<SubjectModel> subjectModels = new Gson().fromJson(subjectItemsJson, type);
            final List<SubjectModel> popularSubjects = new ArrayList<>();
            for (SubjectModel subject : subjectModels) {
                if (subject.type == SubjectModel.Type.POPULAR) {
                    popularSubjects.add(subject);
                }
            }

            final PopularSubjectsAdapter adapter = new PopularSubjectsAdapter(popularSubjects, new PopularSubjectsAdapter.ClickListener() {
                @Override
                public void onSubjectClick(View view) {
                    final int position = binding.rvSubjects.getChildAdapterPosition(view);
                    final String baseUrl = getInitialUrl();
                    final String subjectFilter = popularSubjects.get(position).filter;
                    final Map<String, String> queryParams = new HashMap<>();
                    queryParams.put(QUERY_PARAM_SUBJECT, subjectFilter);
                    loadUrl(buildUrlWithQueryParams(logger, baseUrl, queryParams));
                    setSubjectLayoutVisibility(View.GONE);
                    environment.getAnalyticsRegistry().trackSubjectClicked(subjectFilter);
                }

                @Override
                public void onViewAllSubjectsClick() {
                    environment.getRouter().showSubjectsActivityForResult(WebViewDiscoverCoursesFragment.this,
                            VIEW_SUBJECTS_REQUEST_CODE);
                    environment.getAnalyticsRegistry().trackSubjectClicked(Analytics.Values.VIEW_ALL_SUBJECTS);
                }
            });

            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL, false);
            binding.rvSubjects.setLayoutManager(linearLayoutManager);
            binding.rvSubjects.setAdapter(adapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                final String baseUrl = getInitialUrl();
                final Map<String, String> queryParams = new HashMap<>();
                queryParams.put(QUERY_PARAM_SEARCH, query);
                searchView.onActionViewCollapsed();
                loadUrl(buildUrlWithQueryParams(logger, baseUrl, queryParams));
                final boolean isLoggedIn = environment.getLoginPrefs().getUsername() != null;
                environment.getAnalyticsRegistry().trackCoursesSearch(query, isLoggedIn, BuildConfig.VERSION_NAME);
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
        return URLUtil.isValidUrl(binding.webview.getUrl()) ?
                binding.webview.getUrl() :
                environment.getConfig().getCourseDiscoveryConfig().getCourseSearchUrl();
    }

    @Override
    public void onWebViewLoadProgressChanged(int progress) {
        if (progress == 100) {
            if (binding.webview.getUrl().contains(QUERY_PARAM_SUBJECT)) {
                // It means that WebView just loaded subject related content
                setSubjectLayoutVisibility(View.GONE);
                binding.webview.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            } else {
                setSubjectLayoutVisibility(View.VISIBLE);
            }
        }
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
        }
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return errorNotification != null && errorNotification.isShowing();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case VIEW_SUBJECTS_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    final String subjectFilter = data.getStringExtra(Router.EXTRA_SUBJECT_FILTER);
                    final String baseUrl = getInitialUrl();
                    final Map<String, String> queryParams = new HashMap<>();
                    queryParams.put(QUERY_PARAM_SUBJECT, subjectFilter);
                    loadUrl(buildUrlWithQueryParams(logger, baseUrl, queryParams));
                    setSubjectLayoutVisibility(View.GONE);
                }
                break;
        }
    }

    private boolean shouldShowSubjectDiscovery() {
        return environment.getConfig().getCourseDiscoveryConfig().isSubjectDiscoveryEnabled() &&
                getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE;
    }

    private void setSubjectLayoutVisibility(int visibility) {
        if (shouldShowSubjectDiscovery()) {
            ViewAnimationUtil.fadeViewTo(binding.llSubjectContent, visibility);
        } else {
            binding.llSubjectContent.setVisibility(View.GONE);
        }
    }
}
