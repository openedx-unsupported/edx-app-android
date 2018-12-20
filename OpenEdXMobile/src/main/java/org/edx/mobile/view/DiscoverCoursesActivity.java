package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.view.dialog.NativeFindCoursesFragment;

public class DiscoverCoursesActivity extends BaseSingleFragmentActivity implements ToolbarCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
        setTitle(R.string.label_discover);
    }

    @Override
    public void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @Override
    protected int getToolbarLayoutId() {
        return R.layout.toolbar_with_profile_button;
    }

    @Override
    public Fragment getFirstFragment() {
        final BaseFragment fragment = environment.getConfig().getCourseDiscoveryConfig().isWebviewCourseDiscoveryEnabled()
                ? new WebViewDiscoverCoursesFragment() : new NativeFindCoursesFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        final View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            final TextView titleView = getTitleView();
            if (titleView != null) {
                titleView.setText(title);
            }
        }
        super.setTitle(title);
    }

    @Override
    @Nullable
    public SearchView getSearchView() {
        final View searchView = findViewById(R.id.toolbar_search_view);
        if (searchView != null && searchView instanceof SearchView) {
            return (SearchView) searchView;
        }
        return null;
    }

    @Override
    @Nullable
    public TextView getTitleView() {
        final View titleView = findViewById(R.id.toolbar_title_view);
        if (titleView != null && titleView instanceof TextView) {
            return (TextView) titleView;
        }
        return null;
    }

    @Nullable
    @Override
    public ImageView getProfileView() {
        return null;
    }
}
