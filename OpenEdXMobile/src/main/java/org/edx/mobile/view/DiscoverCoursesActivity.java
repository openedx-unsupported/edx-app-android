package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.dialog.NativeFindCoursesFragment;

public class DiscoverCoursesActivity extends BaseSingleFragmentActivity implements MainDashboardToolbarCallbacks {
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
    }

    @Override
    public void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @Override
    public Fragment getFirstFragment() {
        final BaseFragment fragment = environment.getConfig().getCourseDiscoveryConfig().isWebviewCourseDiscoveryEnabled()
                ? new WebViewDiscoverCoursesFragment() : new NativeFindCoursesFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final boolean result = super.onCreateOptionsMenu(menu);

        final Config config = environment.getConfig();
        if (!config.getCourseDiscoveryConfig().isWebCourseSearchEnabled()) {
            //bail out if the search bar is not enabled
            return result;
        }

        getMenuInflater().inflate(R.menu.find_courses, menu);
        // Get the SearchView and set the searchable configuration
        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        searchView = (SearchView) searchItem.getActionView();
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home && searchView != null && searchView.hasFocus()) {
            searchView.onActionViewCollapsed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public SearchView getSearchView() {
        return searchView;
    }

    @Nullable
    @Override
    public TextView getTitleView() {
        return null;
    }

    @Nullable
    @Override
    public ImageView getProfileView() {
        return null;
    }
}
