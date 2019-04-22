package org.edx.mobile.tta.ui.agenda_items;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.ui.agenda_items.view_model.AgendaListViewModel;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.view.common.PageViewStateCallback;

import java.util.List;


public class AgendaListFragment extends TaBaseFragment {
    public static final String TAG = AgendaListFragment.class.getCanonicalName();
    private static final int RANK = 3;

    private AgendaListViewModel viewModel;
    private String toolbarData;
    private AgendaItem tabSelected;
    private Toolbar toolbar;
    private  List<AgendaItem> items;
    private AgendaList agendaList;

    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBreadcrumb();
        viewModel =  new AgendaListViewModel(getActivity(),this, toolbarData,items,tabSelected, agendaList);
    }

    public static AgendaListFragment newInstance(String toolabarData, List<AgendaItem> items, AgendaItem tabSelected, AgendaList agendaList){
        AgendaListFragment fragment = new AgendaListFragment();
        fragment.toolbarData = toolabarData;
        fragment.items = items;
        fragment.tabSelected = tabSelected;
        fragment.agendaList = agendaList;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_agenda_list, viewModel)
                .getRoot();
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        tabLayout = view.findViewById(R.id.listing_tab_layout);
        viewPager  = view.findViewById(R.id.listing_view_pager);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.post(() -> {
            tabLayout.getTabAt(items.indexOf(tabSelected)).select();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setBreadcrumb();

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

    private void setBreadcrumb() {
        Nav nav;
        if (toolbarData.equalsIgnoreCase(getString(R.string.state_wise_list))){
            nav = Nav.state_agenda;
        } else if (toolbarData.equalsIgnoreCase(getString(R.string.my_agenda))) {
            nav = Nav.my_agenda;
        } else {
            nav = Nav.download_agenda;
        }
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, nav.name()));

    }
}
