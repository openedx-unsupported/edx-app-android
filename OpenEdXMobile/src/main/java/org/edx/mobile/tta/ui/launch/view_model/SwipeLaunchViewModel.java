package org.edx.mobile.tta.ui.launch.view_model;

import android.databinding.ObservableField;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.ui.launch.LaunchFragment;
import org.edx.mobile.tta.ui.launch.SwipeLaunchActivity;
import org.edx.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.view.SplashActivity;

import java.util.ArrayList;
import java.util.List;

public class SwipeLaunchViewModel extends BaseViewModel {

    public SectionsPagerAdapter adapter;
    public List<Fragment> fragments;
    public ObservableField<Boolean> fabVisible = new ObservableField<>(false);

    public ObservableField<ViewPager.OnPageChangeListener> listener = new ObservableField<>(
            new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    if (i == fragments.size()-1){
                        fabVisible.set(true);
                    } else {
                        fabVisible.set(false);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            }
    );

    public SwipeLaunchViewModel(BaseVMActivity activity) {
        super(activity);
        fragments = new ArrayList<>();
        fragments.add(LaunchFragment.newInstance(R.drawable.slider1, ""));
        fragments.add(LaunchFragment.newInstance(R.drawable.slider2, ""));
        fragments.add(LaunchFragment.newInstance(R.drawable.slider3, ""));
        adapter = new SectionsPagerAdapter(mActivity.getSupportFragmentManager());
    }

    public void next() {
        mActivity.finish();

        if (mDataManager.getEdxEnvironment().getUserPrefs().getProfile() != null) {
            //environment.getRouter().showMainDashboard(SplashActivity.this);
            ActivityUtil.gotoPage(mActivity, LandingActivity.class);
        }
        else {
            mDataManager.getEdxEnvironment().getRouter().showLaunchScreen(mActivity);
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
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
    }
}
