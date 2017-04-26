package org.edx.mobile.view.my_videos;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseVideosDownloadStateActivity;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.view.adapters.StaticFragmentPagerAdapter;

public class MyVideosActivity extends BaseVideosDownloadStateActivity {

    private StaticFragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myvideos_tab);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setIcon(android.R.color.transparent);
        }

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        // now init the tabs
        initializeTabs();

        // Full-screen video in landscape.
        if (isLandscape()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setActionBarVisible(false);
        }
    }

    private void initializeTabs() {
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        adapter = new StaticFragmentPagerAdapter(getSupportFragmentManager(),
                new StaticFragmentPagerAdapter.Item(MyAllVideosFragment.class,
                        getText(R.string.my_all_videos)),
                new StaticFragmentPagerAdapter.Item(MyRecentVideosFragment.class,
                        getText(R.string.my_recent_videos))
        );
        pager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setTabsFromPagerAdapter(adapter);
            tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(pager));
            pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    final Analytics.OnEventListener listener =
                            (Analytics.OnEventListener) adapter.getFragment(position);
                    listener.fireScreenEvent();
                }
            });
        }
    }
}
