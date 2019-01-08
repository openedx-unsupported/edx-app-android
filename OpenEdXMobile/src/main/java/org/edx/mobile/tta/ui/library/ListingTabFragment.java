package org.edx.mobile.tta.ui.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.ConfigurationResponse;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.library.view_model.ListingTabViewModel;

import java.util.List;

public class ListingTabFragment extends TaBaseFragment {

    private ConfigurationResponse cr;

    private Category category;

    private List<Content> contents;

    public static ListingTabFragment newInstance(ConfigurationResponse cr, Category category, List<Content> contents){
        ListingTabFragment fragment = new ListingTabFragment();
        fragment.cr = cr;
        fragment.category = category;
        fragment.contents = contents;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_listing_tab, new ListingTabViewModel(
                getActivity(), this, cr, category, contents))
                .getRoot();

        return view;
    }
}
