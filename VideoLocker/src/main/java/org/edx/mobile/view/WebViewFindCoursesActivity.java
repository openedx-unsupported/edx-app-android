package org.edx.mobile.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.support.v7.widget.SearchView;

import org.edx.mobile.R;
import org.edx.mobile.base.FindCoursesBaseActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.util.Config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_find_courses)
public class WebViewFindCoursesActivity extends FindCoursesBaseActivity {

    private WebView webView;
    private SearchView searchView;
    boolean searchActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        environment.getSegment().trackScreenView(ISegment.Screens.FIND_COURSES);

        webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl(environment.getConfig().getCourseDiscoveryConfig().getCourseSearchUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        Config config =  environment.getConfig();
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
                searchView.clearFocus();
                webView.loadUrl(searchUrl);
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
                    searchActive = false;
                    searchItem.collapseActionView();
                    dismissKeyboard();
                    showDrawer(true);
                }  else {
                    searchActive = true;
                    showDrawer(false);
                }
            }
        });

        return result;
    }

    private void dismissKeyboard() {
        searchView.clearFocus();
        searchView.setQuery("", false);
    }

    //Closing the Navigation Drawer
    public void showDrawer(boolean showDrawer) {
        mDrawerToggle.setDrawerIndicatorEnabled(showDrawer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home && searchActive) {
            dismissKeyboard();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onOnline() {
        super.onOnline();
        if (!isWebViewLoaded()) {
            webView.reload();
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
