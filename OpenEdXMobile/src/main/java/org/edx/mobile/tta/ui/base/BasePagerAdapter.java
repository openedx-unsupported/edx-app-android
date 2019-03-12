package org.edx.mobile.tta.ui.base;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import java.util.ArrayList;
import java.util.List;

public class BasePagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;
    private List<String> titles;
    private int pos = 0;

    public BasePagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }


    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    public void clear() {
        fragments.clear();
        titles.clear();
        notifyDataSetChanged();
    }

    public void addFragment(@NonNull Fragment fragment, @NonNull String title) {
        fragments.add(fragment);
        titles.add(title);
        notifyDataSetChanged();
    }

    public void addFragments(@NonNull List<Fragment> fragments, @NonNull List<String> titles) throws Exception {
        if (fragments.size() != titles.size()) {
            throw new Exception("Size of fragments and titles must be equal.");
        } else {
            this.fragments.addAll(fragments);
            this.titles.addAll(titles);
            notifyDataSetChanged();
        }
    }

    public void setFragments(@NonNull List<Fragment> fragments, @NonNull List<String> titles) throws Exception {
        if (fragments.size() != titles.size()) {
            throw new Exception("Size of fragments and titles must be equal.");
        } else {
            this.fragments.clear();
            this.titles.clear();
            this.fragments.addAll(fragments);
            this.titles.addAll(titles);
            notifyDataSetChanged();
        }
    }

    public void setFragmentsbyPos(List<Fragment> fragments, List<String> titles, String posi) throws Exception {
        if (fragments.size() != titles.size()) {
            throw new Exception("Size of fragments and titles must be equal.");
        } else {
            this.fragments.clear();
            this.titles.clear();
            this.fragments.addAll(fragments);
            this.titles.addAll(titles);
            for (String title : titles) {
                if (title.equalsIgnoreCase(posi)) {
                    pos++;
                }
            }
            notifyDataSetChanged();
        }
    }

}
