package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TabWidget;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseVideosDownloadStateActivity;
import org.edx.mobile.module.analytics.ISegment;

public class MyVideosTabActivity extends BaseVideosDownloadStateActivity {

    private static final String TAB_ALL = "all", TAB_RECENT = "recent";

    private View offlineBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myvideos_tab);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        offlineBar = findViewById(R.id.offline_bar);

        environment.getSegment().trackScreenView(ISegment.Screens.MY_VIDEOS);

        // now init the tabs
        initializeTabs();
    }

    private void initializeTabs() {
        FragmentTabHost tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.tabcontent);

        tabHost.addTab(
                tabHost.newTabSpec(TAB_ALL).setIndicator(getText(R.string.my_all_videos)),
                MyAllVideosFragment.class, null);
        tabHost.addTab(
                tabHost.newTabSpec(TAB_RECENT).setIndicator(getText(R.string.my_recent_videos)),
                MyRecentVideosFragment.class, null);

        TabWidget widget = tabHost.getTabWidget();
        for (int i = 0; i < widget.getChildCount(); i++) {
            View child = widget.getChildAt(i);
            final TextView tv = (TextView) child.findViewById(
                    android.R.id.title);
            tv.setTextColor(ContextCompat.getColorStateList(
                    this, R.color.tab_selector));
            tv.setAllCaps(true);

            child.setBackgroundResource(R.drawable.tab_indicator);
        }
        tabHost.setCurrentTab(0);
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
