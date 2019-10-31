package org.humana.mobile.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.humana.mobile.BuildConfig;
import org.humana.mobile.R;
import org.humana.mobile.base.BaseWebViewFindCoursesActivity;
import org.humana.mobile.module.analytics.Analytics;
import org.humana.mobile.util.Config;

import java.util.HashMap;
import java.util.Map;

import roboguice.inject.ContentView;

import static org.humana.mobile.util.UrlUtil.QUERY_PARAM_SEARCH;
import static org.humana.mobile.util.UrlUtil.buildUrlWithQueryParams;

/**
 * This activity will be refactored to fragment in future. See Jira story LEARNER-3842 for more details.
 */
@ContentView(R.layout.activity_find_courses)
public class WebViewFindCoursesActivity extends BaseWebViewFindCoursesActivity {
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for search query in extras
        String searchQueryExtra = null;
        if (getIntent().getExtras() != null) {
            searchQueryExtra = getIntent().getStringExtra(Router.EXTRA_SEARCH_QUERY);
        }

        if (searchQueryExtra != null) {
            initSearch(searchQueryExtra);
        } else {
            loadUrl(getInitialUrl());
        }

        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
    }

    @Override
    public void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @NonNull
    protected String getInitialUrl() {
        return environment.getConfig().getCourseDiscoveryConfig().getCourseSearchUrl();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        Config config = environment.getConfig();
        if (!config.getCourseDiscoveryConfig().isWebCourseSearchEnabled()) {
            //bail out if the search bar is not enabled
            return result;
        }

        getMenuInflater().inflate(R.menu.find_courses, menu);
        // Get the SearchView and set the searchable configuration
        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        searchView = (SearchView) searchItem.getActionView();

        Resources resources = getResources();
        searchView.setQueryHint(resources.getString(R.string.search_for_courses));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.trim().isEmpty())
                    return false;
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
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    searchView.onActionViewCollapsed();
                }
            }
        });

        return result;
    }

    private void initSearch(@NonNull String query) {
        final String baseUrl = environment.getConfig().getCourseDiscoveryConfig().getCourseSearchUrl();
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put(QUERY_PARAM_SEARCH, query);
        loadUrl(buildUrlWithQueryParams(logger, baseUrl, queryParams));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home && searchView != null && searchView.hasFocus()) {
            searchView.onActionViewCollapsed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
