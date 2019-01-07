package org.edx.mobile.tta.ui.listing.view_model;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.ConfigurationResponse;
import org.edx.mobile.tta.interfaces.OnResponseListener;
import org.edx.mobile.tta.ui.base.BaseFragmentPagerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.listing.ListingTabFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListingViewModel extends BaseViewModel {

    private List<Fragment> fragments;
    private List<String> titles;
    public ListingPagerAdapter adapter;

    private ConfigurationResponse cr;
    private List<Category> categories;
    private List<Content> contents;

    private boolean configRecieved = false;
    private boolean contentRecieved = false;

    public ListingViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        categories = new ArrayList<>();
        contents = new ArrayList<>();
        fragments = new ArrayList<>();
        titles = new ArrayList<>();

        adapter = new ListingPagerAdapter(mFragment.getChildFragmentManager());

        getData();

    }

    private void getData(){
        mActivity.show();

        mDataManager.getConfiguration(new OnResponseListener<ConfigurationResponse>() {
            @Override
            public void onSuccess(ConfigurationResponse data) {
                mActivity.hide();
                cr = data;

                if (cr != null) {
                    categories.clear();
                    categories.addAll(cr.getCategory());
                    Collections.sort(categories);
                }

                configRecieved = true;
                if (contentRecieved){
                    populateTabs();
                }

            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hide();
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });

        mDataManager.getContents(new OnResponseListener<List<Content>>() {
            @Override
            public void onSuccess(List<Content> data) {
                mActivity.hide();
                contents = data;

                contentRecieved = true;
                if (configRecieved){
                    populateTabs();
                }
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hide();
                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });

    }

    private void populateTabs(){

        fragments.clear();
        titles.clear();
        for (Category category: categories){
            fragments.add(ListingTabFragment.newInstance(cr, category, contents));
            titles.add(category.getName());
        }

        adapter.clear();
        adapter.addAll(fragments, titles);

    }

    public class ListingPagerAdapter extends BaseFragmentPagerAdapter {
        public ListingPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }
}
