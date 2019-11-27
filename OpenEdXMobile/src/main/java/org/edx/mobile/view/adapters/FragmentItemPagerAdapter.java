package org.edx.mobile.view.adapters;

import android.support.annotation.NonNull;
import androidx.core.app.Fragment;
import androidx.core.app.FragmentManager;
import androidx.core.app.FragmentStatePagerAdapter;

import org.edx.mobile.model.FragmentItemModel;

import java.util.List;

public class FragmentItemPagerAdapter extends FragmentStatePagerAdapter {
    @NonNull
    private List<FragmentItemModel> fragmentItems;

    public FragmentItemPagerAdapter(@NonNull FragmentManager fm,
                                    @NonNull List<FragmentItemModel> fragmentItems) {
        super(fm);
        this.fragmentItems = fragmentItems;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentItems.get(position).generateFragment();
    }

    @Override
    public int getCount() {
        return fragmentItems.size();
    }
}
