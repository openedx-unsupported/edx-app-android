package org.edx.mobile.view.my_videos;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseVideosDownloadStateActivity;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.view.adapters.StaticFragmentPagerAdapter;

public class MyVideosActivity extends BaseVideosDownloadStateActivity {

    private View offlineBar;
    private StaticFragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myvideos_tab);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        offlineBar = findViewById(R.id.offline_bar);

        environment.getSegment().trackScreenView(ISegment.Screens.MY_VIDEOS);

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
            pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        }
    }

    @Override
    protected void onOffline() {
        super.onOffline();
        offlineBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onOnline() {
        super.onOnline();
        offlineBar.setVisibility(View.GONE);
    }
}
