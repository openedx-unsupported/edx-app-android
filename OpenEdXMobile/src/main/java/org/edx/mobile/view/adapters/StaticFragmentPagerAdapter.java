package org.edx.mobile.view.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import org.edx.mobile.model.FragmentItemModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link FragmentPagerAdapter} that takes a static list
 * of Fragments as the items.
 */
public class StaticFragmentPagerAdapter extends FragmentPagerAdapter {
    @NonNull
    private List<FragmentItemModel> items = Collections.emptyList();
    @NonNull
    private final Map<Integer, Fragment> positionToFragment = new HashMap<>();

    private FragmentLifecycleCallbacks fragmentLifecycleCallbacks;
    public interface FragmentLifecycleCallbacks {
        void onFragmentInstantiate();
    }

    public StaticFragmentPagerAdapter(@NonNull FragmentManager manager, FragmentLifecycleCallbacks fragmentLifecycleCallbacks, @NonNull FragmentItemModel... items) {
        super(manager);
        this.fragmentLifecycleCallbacks = fragmentLifecycleCallbacks;
        setItems(Arrays.asList(items));
    }

    public void setItems(@NonNull List<FragmentItemModel> items) {
        this.items = new LinkedList<>(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Fragment getItem(int position) {
        return items.get(position).generateFragment();
    }

    public Fragment getFragment(int position) {
        return positionToFragment.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return items.get(position).getTitle();
    }

    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        final Fragment fragment = (Fragment)super.instantiateItem(container, position);
        positionToFragment.put(position, fragment);
        if (fragmentLifecycleCallbacks != null) {
            fragmentLifecycleCallbacks.onFragmentInstantiate();
        }
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        positionToFragment.remove(position);
    }
}
