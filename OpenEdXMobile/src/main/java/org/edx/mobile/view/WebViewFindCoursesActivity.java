package org.edx.mobile.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseWebViewFindCoursesActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.Config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import roboguice.inject.ContentView;

/**
 * This activity will be refactored to fragment in future. See Jira story LEARNER-3842 for more details.
 */
@ContentView(R.layout.activity_find_courses)
public class WebViewFindCoursesActivity extends BaseWebViewFindCoursesActivity {
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (environment.getLoginPrefs().getUsername() != null) {
            if (!environment.getConfig().isTabsLayoutEnabled()) {
                addDrawer();
            }
        } else {
            blockDrawerFromOpening();
        }
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
        loadUrl(getInitialUrl());
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
                    searchView.onActionViewCollapsed();
                }
                enableDrawerMenuButton(!queryTextFocused);
            }
        });

        return result;
    }

    public void enableDrawerMenuButton(boolean showDrawer) {
        if (mDrawerToggle == null) {
            return;
        }
        mDrawerToggle.setDrawerIndicatorEnabled(showDrawer);
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
}
