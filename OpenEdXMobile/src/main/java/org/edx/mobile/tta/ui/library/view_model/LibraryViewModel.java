package org.edx.mobile.tta.ui.library.view_model;

import android.content.Context;
import android.databinding.ObservableInt;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.edx.mobile.tta.ui.library.LibraryTab;
import org.edx.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LibraryViewModel extends BaseViewModel {

    private List<Fragment> fragments;
    private List<String> titles;
    public ListingPagerAdapter adapter;

    private CollectionConfigResponse cr;
    private List<Category> categories;
    private SearchPageOpenedListener searchPageOpenedListener;

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

    public LibraryViewModel(Context context, TaBaseFragment fragment, SearchPageOpenedListener searchPageOpenedListener) {
        super(context, fragment);

        categories = new ArrayList<>();
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        this.searchPageOpenedListener = searchPageOpenedListener;

        adapter = new ListingPagerAdapter(mFragment.getChildFragmentManager());

        getData();

    }

    private void getData(){
        mActivity.showLoading();

        mDataManager.getCollectionConfig(new OnResponseCallback<CollectionConfigResponse>() {
            @Override
            public void onSuccess(CollectionConfigResponse data) {
//                mActivity.hideLoading();
                cr = data;

                if (cr != null) {
                    categories.clear();
                    categories.addAll(cr.getCategory());
                    Collections.sort(categories);
                }

                populateTabs();

            }

            @Override
            public void onFailure(Exception e) {
//                mActivity.hideLoading();
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });

    }

    private void populateTabs(){
        fragments.clear();
        titles.clear();
        for (Category category: categories){
            fragments.add(LibraryTab.newInstance(cr, category, searchPageOpenedListener));
            titles.add(category.getName());
        }

        try {
            adapter.setFragments(fragments, titles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialPosition.set(0);

        if (!categories.isEmpty()){
            PageViewStateCallback callback = (PageViewStateCallback) fragments.get(0);
            if (callback != null){
                callback.onPageShow();
            }
        }

    }

    public class ListingPagerAdapter extends BasePagerAdapter {
        public ListingPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }
}
