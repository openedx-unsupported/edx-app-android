package org.edx.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.view.View;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.databinding.ActivityDiscoveryLaunchBinding;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.SoftKeyboardUtil;

public class DiscoveryLaunchActivity extends PresenterActivity<DiscoveryLaunchPresenter, DiscoveryLaunchPresenter.ViewInterface> {

    private ActivityDiscoveryLaunchBinding binding;

    @NonNull
    @Override
    protected DiscoveryLaunchPresenter createPresenter(@Nullable Bundle savedInstanceState) {
        return new DiscoveryLaunchPresenter(environment.getLoginPrefs(), environment.getConfig().getCourseDiscoveryConfig());
    }

    @NonNull
    @Override
    protected DiscoveryLaunchPresenter.ViewInterface createView(@Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_discovery_launch);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.LAUNCH_ACTIVITY);
        AuthPanelUtils.setAuthPanelVisible(true, binding.authPanel, environment);
        return new DiscoveryLaunchPresenter.ViewInterface() {
            @Override
            public void setEnabledButtons(boolean courseDiscoveryEnabled) {
                if (courseDiscoveryEnabled) {
                    binding.svSearchCourses.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (query == null || query.trim().isEmpty())
                                return false;
                            SoftKeyboardUtil.hide(DiscoveryLaunchActivity.this);
                            environment.getRouter().showFindCourses(DiscoveryLaunchActivity.this, query);
                            // Empty the SearchView upon submit
                            binding.svSearchCourses.setQuery("", false);

                            final boolean isLoggedIn = environment.getLoginPrefs().getUsername() != null;
                            environment.getAnalyticsRegistry().trackCoursesSearch(query, isLoggedIn, BuildConfig.VERSION_NAME);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return false;
                        }
                    });
                } else {
                    binding.svSearchCourses.setVisibility(View.GONE);
                }
            }

            @Override
            public void navigateToMyCourses() {
                finish();
                environment.getRouter().showMainDashboard(DiscoveryLaunchActivity.this);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
        SoftKeyboardUtil.clearViewFocus(binding.svSearchCourses);
    }
}
