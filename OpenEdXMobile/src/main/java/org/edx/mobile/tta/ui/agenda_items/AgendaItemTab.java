package org.edx.mobile.tta.ui.agenda_items;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;

import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.ui.agenda_items.view_model.AgendaItemViewModel;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.tta.utils.JsonUtil;


public class AgendaItemTab extends TaBaseFragment {
    private static final int RANK = 4;

    private AgendaItemViewModel viewModel;
    public AgendaItem item;
    private String toolbarData;
    private AgendaList agendaList;

    public static AgendaItemTab newInstance(AgendaItem item, String toolbarData, AgendaList agendaList) {
        AgendaItemTab fragment = new AgendaItemTab();
        fragment.item = item;
        fragment.toolbarData = toolbarData;
        fragment.agendaList = agendaList;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new AgendaItemViewModel(getActivity(), this, item, toolbarData, agendaList);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_agenda_item_tab, viewModel)
                .getRoot();
        return view;
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, item.getSource_name()));

        Nav nav;
        if (toolbarData.equalsIgnoreCase(getString(R.string.state_wise_list))){
            nav = Nav.state_agenda;
        } else if (toolbarData.equalsIgnoreCase(getString(R.string.my_agenda))) {
            nav = Nav.my_agenda;
        } else {
            nav = Nav.download_agenda;
        }

        analytic.addMxAnalytics_db(item.getSource_title() , Action.Nav, nav.name(),
                org.edx.mobile.tta.analytics.analytics_enums.Source.Mobile, null);
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
