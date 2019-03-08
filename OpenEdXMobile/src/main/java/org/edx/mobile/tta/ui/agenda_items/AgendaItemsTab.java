package org.edx.mobile.tta.ui.agenda_items;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;

import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.ui.agenda_items.view_model.AgendaItemsTabViewModel;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.util.PermissionsUtil;

import java.util.List;


public class AgendaItemsTab extends TaBaseFragment {
    private AgendaItemsTabViewModel viewModel;
    public AgendaItem item;
    private String toolbar;

    public static AgendaItemsTab newInstance(AgendaItem item,String toolbarData) {
        AgendaItemsTab fragment = new AgendaItemsTab();
        fragment.item = item;
        fragment.toolbar = toolbarData;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new AgendaItemsTabViewModel(getActivity(), this, item,toolbar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_agendaitems_tab, viewModel)
                .getRoot();
        return view;
    }

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        switch (requestCode){
            case PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST:
                viewModel.showContentDashboard();
                break;
        }
    }

}
