package org.edx.mobile.view;

import org.edx.mobile.R;

public class WebViewDiscoverProgramsFragment extends WebViewDiscoverFragment {
    @Override
    protected String getSearchUrl() {
        return environment.getConfig().getDiscoveryConfig().getProgramDiscoveryConfig().getBaseUrl();
    }

    @Override
    protected int getQueryHint() {
        return R.string.search_for_programs;
    }

    @Override
    protected boolean isSearchEnabled() {
        return environment.getConfig().getDiscoveryConfig().getProgramDiscoveryConfig().isSearchEnabled();
    }
}
