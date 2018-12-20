package org.edx.mobile.tta.ui.listing.view_model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.listing.ListingTabFragment;

import java.util.ArrayList;
import java.util.List;

public class ListingViewModel extends BaseViewModel {

    private List<Fragment> fragments;
    private String[] titles;
    public ListingPagerAdapter adapter;

    public ListingViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        fragments = new ArrayList<>();
        fragments.add(new ListingTabFragment());
        fragments.add(new ListingTabFragment());
        fragments.add(new ListingTabFragment());
        fragments.add(new ListingTabFragment());
        fragments.add(new ListingTabFragment());
        titles = new String[]{
                mActivity.getString(R.string.all),
                mActivity.getString(R.string.course),
                mActivity.getString(R.string.chatshala),
                mActivity.getString(R.string.toolkit),
                mActivity.getString(R.string.hois)
        };
        adapter = new ListingPagerAdapter(mFragment.getChildFragmentManager());

    }

    public class ListingPagerAdapter extends FragmentStatePagerAdapter {

        public ListingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
