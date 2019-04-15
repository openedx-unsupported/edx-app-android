package org.edx.mobile.tta.ui.agenda_items.view_model;

import android.content.Context;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;import org.edx.mobile.tta.data.model.agenda.AgendaItem;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.ui.agenda_items.AgendaItemTab;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import java.util.ArrayList;
import java.util.List;

public class AgendaListViewModel extends BaseViewModel {
    public AgendaItem agendaItem;
    public String toolabarData;
    private AgendaItem tabSelected;
    private List<Fragment> fragments;
    private List<String> titles;
    public ListingAgendaPagerAdapter adapter;
    public ViewPager viewPager;
    List<AgendaItem> items;
    private AgendaList agendaList;

    public AgendaListViewModel(Context context, TaBaseFragment fragment, String toolabarData, List<AgendaItem> items, AgendaItem tabSelected, AgendaList agendaList) {
        super(context, fragment);
        this.toolabarData = toolabarData;
        this.tabSelected = tabSelected;
        this.items = items;
        this.agendaList = agendaList;
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        adapter = new ListingAgendaPagerAdapter(mFragment.getChildFragmentManager());
        populateTabs();
    }

    private void populateTabs() {
        fragments.clear();
        titles.clear();
        for (AgendaItem item :items) {
            fragments.add(AgendaItemTab.newInstance(item,toolabarData, agendaList));
            titles.add(item.getSource_title());
        }
        try {
            adapter.setFragments(fragments,titles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class ListingAgendaPagerAdapter extends BasePagerAdapter {
        public ListingAgendaPagerAdapter(FragmentManager fm) {
            super(fm);

        }
    }
}
