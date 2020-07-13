package org.edx.mobile.view.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

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
public class StaticFragmentPagerAdapter extends FragmentStateAdapter {

    @NonNull
    private List<FragmentItemModel> items = Collections.emptyList();
    @NonNull
    private final Map<Integer, Fragment> positionToFragment = new HashMap<>();

    private FragmentLifecycleCallbacks fragmentLifecycleCallbacks;

    public interface FragmentLifecycleCallbacks {
        void onFragmentInstantiate();
    }

    public StaticFragmentPagerAdapter(@NonNull Fragment fragment, FragmentLifecycleCallbacks fragmentLifecycleCallbacks, @NonNull FragmentItemModel... items) {
        super(fragment);
        this.fragmentLifecycleCallbacks = fragmentLifecycleCallbacks;
        setItems(Arrays.asList(items));
    }

    public void setItems(@NonNull List<FragmentItemModel> items) {
        this.items = new LinkedList<>(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = items.get(position).generateFragment();
        positionToFragment.put(position, fragment);
        if (fragmentLifecycleCallbacks != null) {
            fragmentLifecycleCallbacks.onFragmentInstantiate();
        }
        return fragment;
    }

    public Fragment getFragment(int position) {
        return positionToFragment.get(position);
    }

    public CharSequence getPageTitle(int position) {
        return items.get(position).getTitle();
    }
}
