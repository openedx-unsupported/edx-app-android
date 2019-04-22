package org.edx.mobile.tta.ui.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.edx.mobile.tta.ui.library.view_model.LibraryViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.view.common.PageViewStateCallback;

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.library.name()));
        viewPager.post(() -> {
            try {
                PageViewStateCallback callback = (PageViewStateCallback) ((BasePagerAdapter) viewPager.getAdapter())
                        .getItem(viewModel.initialPosition.get());
                if (callback != null){
                    callback.onPageShow();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
