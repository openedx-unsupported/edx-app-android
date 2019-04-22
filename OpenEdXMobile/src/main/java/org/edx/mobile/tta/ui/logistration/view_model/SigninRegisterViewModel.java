package org.edx.mobile.tta.ui.logistration.view_model;

import android.databinding.ObservableInt;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.logistration.RegisterFragment;
import org.edx.mobile.tta.ui.logistration.SigninFragment;
import org.edx.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.List;

public class SigninRegisterViewModel extends BaseViewModel {

    public SigninRegisterAdapter adapter;

    public List<Fragment> fragments;

    public List<String> titles;

    public ObservableInt initialPosition = new ObservableInt();

    public ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            initialPosition.set(i);
            PageViewStateCallback callback = (PageViewStateCallback) fragments.get(i);
            if (callback != null){
                callback.onPageShow();
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public SigninRegisterViewModel(BaseVMActivity activity) {
        super(activity);
        fragments = new ArrayList<>();
        titles = new ArrayList<>();

        fragments.add(new SigninFragment());
        titles.add(activity.getString(R.string.sign_in));

        fragments.add(new RegisterFragment());
        titles.add(activity.getString(R.string.register));

        adapter = new SigninRegisterAdapter(mActivity.getSupportFragmentManager());
        try {
            adapter.addFragments(fragments, titles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialPosition.set(0);

        PageViewStateCallback callback = (PageViewStateCallback) fragments.get(0);
        if (callback != null){
            callback.onPageShow();
        }
    }

    public class SigninRegisterAdapter extends BasePagerAdapter {
        public SigninRegisterAdapter(FragmentManager fm) {
            super(fm);
        }
    }

    public void toggleTab(){
        initialPosition.set((initialPosition.get() + 1) % 2);
    }
}
