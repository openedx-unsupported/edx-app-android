package org.edx.mobile.tta.ui.logistration.view_model;

import android.databinding.ObservableInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.logistration.RegisterFragment;
import org.edx.mobile.tta.ui.logistration.SigninFragment;

import java.util.ArrayList;
import java.util.List;

public class SigninRegisterViewModel extends BaseViewModel {

    public SigninRegisterAdapter adapter;

    public List<Fragment> fragments;

    public String[] titles;

    public ObservableInt initialPosition = new ObservableInt();

    public ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            initialPosition.set(i);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public SigninRegisterViewModel(BaseVMActivity activity) {
        super(activity);
        fragments = new ArrayList<>();
        fragments.add(new SigninFragment());
        fragments.add(new RegisterFragment());
        titles = new String[]{
                activity.getString(R.string.sign_in),
                activity.getString(R.string.register)
        };
        adapter = new SigninRegisterAdapter(mActivity.getSupportFragmentManager());
        initialPosition.set(0);
    }

    public class SigninRegisterAdapter extends FragmentStatePagerAdapter{

        public SigninRegisterAdapter(FragmentManager fm) {
            super(fm);
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
            return titles[position];
        }
    }

    public void toggleTab(){
        initialPosition.set((initialPosition.get() + 1) % 2);
    }
}
