package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;

import java.util.List;


public abstract class BaseTabActivity extends BaseFragmentActivity {

    protected FragmentTabHost tabHost;
    protected FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        if (tabHost == null) {
            throw new NoTabLayoutElementsException("Unable to find tabhost in layout");
        }
        if (findViewById(android.R.id.tabcontent) == null) {
            throw new NoTabLayoutElementsException("Unable to find tabcontent in layout");
        }
        setUpTabs();
        tabHost.setCurrentTab(getDefaultTab());
    }

    private void setUpTabs() {
        List<TabModel> tabs = tabsToAdd();

        fragmentManager = getSupportFragmentManager();
        tabHost.setup(this, fragmentManager, android.R.id.tabcontent);
        for (int i = 0; i < tabs.size(); i ++){
            TabModel tab = tabs.get(i);
            tabHost.addTab(
                    tabHost.newTabSpec(tab.getTag()).setIndicator(tab.getName(), null),
                    tab.getFragmentClass(),
                    tab.getFragmentArgs());
        }

        TabWidget widget = tabHost.getTabWidget();

        for (int i = 0; i < widget.getChildCount(); i++) {
            final TextView tv = (TextView) widget.getChildAt(i).findViewById(
                    android.R.id.title);
            tv.setTextColor(this.getResources().getColorStateList(
                    R.color.tab_selector));
            tv.setSingleLine(true);
            tv.setAllCaps(true);
        }
    }

    protected Fragment getFragmentByTag(String tag) {
        if (fragmentManager != null) {
            return fragmentManager.findFragmentByTag(tag);
        }
        else {
            return null;
        }
    }

    public class TabModel {
        private String name;
        private Class fragmentClass;
        private Bundle fragmentArgs;
        private String tag;

        public String getName() {
            return name;
        }

        public Class getFragmentClass() {
            return fragmentClass;
        }

        public Bundle getFragmentArgs() {
            return fragmentArgs;
        }

        public String getTag() {
            return tag;
        }

        public TabModel(String name, Class fragmentClass, Bundle fragmentArgs, String tag) {
            this.name = name;
            this.fragmentClass = fragmentClass;
            this.fragmentArgs = fragmentArgs;
            this.tag = tag;
        }
    }

    private class NoTabLayoutElementsException extends RuntimeException {
        public NoTabLayoutElementsException(String detailMessage) {
            super(detailMessage);
        }
    }

    protected abstract List<TabModel> tabsToAdd();

    protected  abstract int getDefaultTab();
}
