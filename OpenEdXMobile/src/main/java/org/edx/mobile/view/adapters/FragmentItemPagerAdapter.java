package org.edx.mobile.view.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.edx.mobile.model.FragmentItemModel;

import java.util.List;

public class FragmentItemPagerAdapter extends FragmentStateAdapter {

    @NonNull
    private List<FragmentItemModel> fragmentItems;

    public FragmentItemPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                    @NonNull List<FragmentItemModel> fragmentItems) {
        super(fragmentActivity);
        this.fragmentItems = fragmentItems;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentItems.get(position).generateFragment();
    }

    @Override
    public int getItemCount() {
        return fragmentItems.size();
    }
}
