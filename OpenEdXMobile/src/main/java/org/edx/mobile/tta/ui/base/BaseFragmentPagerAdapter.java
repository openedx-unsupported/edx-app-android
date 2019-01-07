package org.edx.mobile.tta.ui.base;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BaseFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;

    private List<String> titles;

    public BaseFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments == null ? 0 : fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    public void add(Fragment fragment, String title){
        fragments.add(fragment);
        titles.add(title);
        notifyDataSetChanged();
    }

    /**
     * Size of fragments and titles must be same.
     * If not, it may cause IndexOutOfBoundException.
     * @param fragments
     * @param titles
     */
    public void addAll(List<Fragment> fragments, List<String> titles){
        this.fragments.addAll(fragments);
        this.titles.addAll(titles);
        notifyDataSetChanged();
    }

    public void clear(){
        fragments.clear();
        titles.clear();
    }
}
