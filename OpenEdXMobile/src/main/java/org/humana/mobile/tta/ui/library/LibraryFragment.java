package org.humana.mobile.tta.ui.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.humana.mobile.R;
import org.humana.mobile.tta.analytics.analytics_enums.Nav;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.humana.mobile.tta.ui.library.view_model.LibraryViewModel;
import org.humana.mobile.tta.utils.BreadcrumbUtil;

public class LibraryFragment extends TaBaseFragment {
    public static final String TAG = LibraryFragment.class.getCanonicalName();
    private static final int RANK = 2;
    private LibraryViewModel viewModel;

    private SearchPageOpenedListener searchPageOpenedListener;
    private ViewPager viewPager;

    public static LibraryFragment newInstance(SearchPageOpenedListener searchPageOpenedListener){
        LibraryFragment fragment = new LibraryFragment();
        fragment.searchPageOpenedListener = searchPageOpenedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.library.name()));
        viewModel = new LibraryViewModel(getActivity(), this, searchPageOpenedListener);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_library, viewModel)
                .getRoot();
        TabLayout tabLayout = view.findViewById(R.id.listing_tab_layout);

        viewPager = view.findViewById(R.id.listing_view_pager);
        viewPager.setOffscreenPageLimit(5);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.library.name()));
//        viewPager.post(() -> {
//            try {
//                PageViewStateCallback callback = (PageViewStateCallback) ((BasePagerAdapter) viewPager.getAdapter())
//                        .getItem(viewModel.initialPosition.get());
//                if (callback != null){
//                    callback.onPageShow();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
