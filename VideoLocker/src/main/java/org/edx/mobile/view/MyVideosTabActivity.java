package org.edx.mobile.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseVideosDownloadStateActivity;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.view.adapters.StaticFragmentPagerAdapter;

public class MyVideosTabActivity extends BaseVideosDownloadStateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myvideos_tab);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

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
        PagerAdapter adapter = new StaticFragmentPagerAdapter(getSupportFragmentManager(),
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

}
