package org.edx.mobile.tta.ui.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.Analytic;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.Source;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.edx.mobile.tta.ui.library.view_model.LibraryTabViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.tta.utils.JsonUtil;
import org.edx.mobile.util.PermissionsUtil;

public class LibraryTab extends TaBaseFragment {
    private static final int RANK = 3;

    private CollectionConfigResponse cr;

    private Category category;

    private SearchPageOpenedListener searchPageOpenedListener;

    private LibraryTabViewModel viewModel;

    public static LibraryTab newInstance(CollectionConfigResponse cr, Category category,
                                         SearchPageOpenedListener searchPageOpenedListener){
        LibraryTab fragment = new LibraryTab();
        fragment.cr = cr;
        fragment.category = category;
        fragment.searchPageOpenedListener = searchPageOpenedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new LibraryTabViewModel(getActivity(), this, cr, category, searchPageOpenedListener);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_library_tab, viewModel)
                .getRoot();

        return view;
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        String nav = Nav.all.name();
        if (category != null && category.getSource_id() > 0 && cr != null){
            for (Source source: cr.getSource()){
                if (category.getSource_id() == source.getId()){
                    nav = source.getName();
                    break;
                }
            }
        }
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, nav));

        if (analytic != null) {
            analytic.addMxAnalytics_db(
                    category.getName() , Action.Nav, Nav.library.name(),
                    org.edx.mobile.tta.analytics.analytics_enums.Source.Mobile, null);
        }
    }

    /*@Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        switch (requestCode){
            case PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST:
                viewModel.showContentDashboard();
                break;
        }
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
