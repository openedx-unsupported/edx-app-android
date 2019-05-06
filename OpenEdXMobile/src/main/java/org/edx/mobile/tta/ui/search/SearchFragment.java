package org.edx.mobile.tta.ui.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.search.view_model.SearchViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.util.PermissionsUtil;

import java.util.List;

public class SearchFragment extends TaBaseFragment {

    public static final String TAG = SearchFragment.class.getCanonicalName();
    private int RANK = 2;

    private SearchViewModel viewModel;

    private Category category;
    private List<ContentList> contentLists;
    private ContentList selectedContentList;

    public static SearchFragment newInstance(Category category, List<ContentList> contentLists, ContentList selectedContentList){
        SearchFragment fragment = new SearchFragment();
        fragment.category = category;
        fragment.contentLists = contentLists;
        fragment.selectedContentList = selectedContentList;
        fragment.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new SearchViewModel(getActivity(), this, category, contentLists, selectedContentList);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_search, viewModel).getRoot();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.search.name()));
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
