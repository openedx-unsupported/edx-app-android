package org.edx.mobile.tta.ui.agenda_items;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.ui.agenda_items.view_model.AgendaItemModel;
import org.edx.mobile.tta.ui.base.TaBaseFragment;

import java.util.List;


public class AgendaItemsAct extends TaBaseFragment {
    public static final String TAG = AgendaItemsAct.class.getCanonicalName();
    private AgendaItemModel viewModel;
    private String toolabarData;
    private AgendaItem tabSelected;
    private Toolbar toolbar;
    private  List<AgendaItem> items;
    private AgendaList agendaList;

    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel =  new AgendaItemModel(getActivity(),this, toolabarData,items,tabSelected, agendaList);
    }

    public static AgendaItemsAct newInstance(String toolabarData, List<AgendaItem> items, AgendaItem tabSelected, AgendaList agendaList){
        AgendaItemsAct fragment = new AgendaItemsAct();
        fragment.toolabarData = toolabarData;
        fragment.items = items;
        fragment.tabSelected = tabSelected;
        fragment.agendaList = agendaList;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_activity_statelistagenda, viewModel)
                .getRoot();
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        tabLayout = view.findViewById(R.id.listing_tab_layout);
        viewPager  = view.findViewById(R.id.listing_view_pager);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.post(() -> {
            tabLayout.getTabAt(items.indexOf(tabSelected)).select();
        });

        return view;
    }

}
