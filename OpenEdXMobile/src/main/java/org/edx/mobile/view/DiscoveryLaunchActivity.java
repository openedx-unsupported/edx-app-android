package org.edx.mobile.view;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.databinding.ActivityDiscoveryLaunchBinding;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.SoftKeyboardUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiscoveryLaunchActivity extends PresenterActivity<DiscoveryLaunchPresenter, DiscoveryLaunchPresenter.ViewInterface> {

    private static final String QUERY_PARAM = "query_param";
    private static final String FOCUSED_VIEW = "focused_view";

    private ActivityDiscoveryLaunchBinding binding;

    @NonNull
    @Override
    protected DiscoveryLaunchPresenter createPresenter(@Nullable Bundle savedInstanceState) {
        return new DiscoveryLaunchPresenter(environment.getLoginPrefs(), environment);
    }

    @NonNull
    @Override
    protected DiscoveryLaunchPresenter.ViewInterface createView(@Nullable Bundle savedInstanceState) {
        binding = ActivityDiscoveryLaunchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.LAUNCH_ACTIVITY);
        AuthPanelUtils.setAuthPanelVisible(true, binding.authPanel, environment);
        return new DiscoveryLaunchPresenter.ViewInterface() {
            @Override
            public void setEnabledButtons(boolean courseDiscoveryEnabled, boolean programDiscoveryEnabled) {
                if (courseDiscoveryEnabled) {
                    // Update the text based on Program discovery enabled
                    if (programDiscoveryEnabled) {
                        binding.tvLaunchText.setText(getString(R.string.launch_text_courses_and_program));
                    }
                    binding.svSearchCourses.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (query == null || query.trim().isEmpty())
                                return false;
                            SoftKeyboardUtil.hide(DiscoveryLaunchActivity.this);
                            environment.getRouter().showFindCourses(DiscoveryLaunchActivity.this, query);
                            // Empty the SearchView upon submit
                            binding.svSearchCourses.setQuery("", false);

                            final boolean isLoggedIn = environment.getLoginPrefs().isUserLoggedIn();
                            environment.getAnalyticsRegistry().trackCoursesSearch(query, isLoggedIn, BuildConfig.VERSION_NAME);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return false;
                        }
                    });
                    binding.tvExploreAllCourses.setOnClickListener(v -> {
                        environment.getRouter().showFindCourses(DiscoveryLaunchActivity.this, "");
                        environment.getAnalyticsRegistry().trackExploreAllCoursesTapped(BuildConfig.VERSION_NAME);
                    });
                } else {
                    binding.tvSearchTitle.setVisibility(View.GONE);
                    binding.svSearchCourses.setVisibility(View.GONE);
                    binding.tvExploreAllCourses.setVisibility(View.GONE);
                }
            }

            @Override
            public void navigateToMainDashboard() {
                finish();
                environment.getRouter().showMainDashboard(DiscoveryLaunchActivity.this);
            }
        };
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY_PARAM, binding.svSearchCourses.getQuery().toString());
        outState.putBoolean(FOCUSED_VIEW, binding.svSearchCourses.hasFocus());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String queryString = savedInstanceState.getString(QUERY_PARAM, "");
        binding.svSearchCourses.setQuery(queryString, false);
        if (savedInstanceState.getBoolean(FOCUSED_VIEW, false)) {
            binding.svSearchCourses.postDelayed(() -> {
                binding.svSearchCourses.requestFocus();
                SoftKeyboardUtil.hide(this);
            }, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
        SoftKeyboardUtil.clearViewFocus(binding.svSearchCourses);
    }
}
