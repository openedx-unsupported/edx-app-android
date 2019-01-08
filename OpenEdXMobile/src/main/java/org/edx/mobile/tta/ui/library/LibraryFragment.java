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
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.library.view_model.LibraryViewModel;

public class LibraryFragment extends TaBaseFragment {
    public static final String TAG = LibraryFragment.class.getCanonicalName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_listing, new LibraryViewModel(getActivity(), this))
                .getRoot();

        TabLayout tabLayout = view.findViewById(R.id.listing_tab_layout);
        ViewPager viewPager = view.findViewById(R.id.listing_view_pager);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }
}
