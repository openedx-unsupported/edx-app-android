package org.edx.mobile.tta.ui.programs.schedule.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.CommonFilterItemBinding;

import org.edx.mobile.databinding.TRowScheduleBinding;
import org.edx.mobile.tta.data.local.db.table.Period;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.model.program.ProgramFilterTag;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.custom.DropDownFilterView;

import java.util.ArrayList;
import java.util.List;

public class ScheduleViewModel extends BaseViewModel {

    public List<DropDownFilterView.FilterItem> typeFilters;
    public List<DropDownFilterView.FilterItem> sessionFilters;
    public List<ProgramFilter> filterList;
    public List<Period> periodList;
    public List<ProgramFilterTag> mlist;
    public DropDownFilterView.OnFilterClickListener typeListener = (v, item, position, prev) -> {

    };

    public DropDownFilterView.OnFilterClickListener sessionListener = (v, item, position, prev) -> {

    };

    public FiltersAdapter filtersAdapter;
    public PeriodAdapter periodAdapter;
    public RecyclerView.LayoutManager layoutManager;
    public RecyclerView.LayoutManager gridLayoutManager;

    public ScheduleViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        filtersAdapter = new FiltersAdapter(mActivity);
        periodAdapter = new PeriodAdapter(mActivity);

//        setFilters();
//        fetchSchedules();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        gridLayoutManager = new GridLayoutManager(mActivity, 2);

    }

    public void getFilters(){
        filterList = new ArrayList<>();
        List<String> or  = new ArrayList<>();
        or.add("fsdcs");
        mlist = new ArrayList<>();
        ProgramFilterTag tags = new ProgramFilterTag();
        tags.setDisplayName("Type");
        tags.setId(1L);
        tags.setInternalName("dsadsa");
        tags.setOrder(344L);
        mlist.add(tags);

        ProgramFilter pf = new ProgramFilter();
        for (int i = 0; i<5 ; i++) {
            pf.setDisplayName("Type");
            pf.setId(1L);
            pf.setInternalName("Type");
            pf.setOrder(2L);
            pf.setTags(mlist);
            pf.setShowIn(or);
            filterList.add(pf);
        }

//        mDataManager.getProgramFilters(new OnResponseCallback<List<ProgramFilter>>() {
//            @Override
//            public void onSuccess(List<ProgramFilter> data) {
//                filterList = data;
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//
//            }
//        });

        filtersAdapter.setItems(filterList);
    }


    public void getPeriods(){
        periodList = new ArrayList<>();
        Period p = new Period();
        p.setCode("1221");
        p.setTitle("Period 1");
        p.setId(2L);
        p.setTotalCount(4L);
        p.setWeeks(4L);
        p.setUsername("Ankit");
        periodList.add(p);
//        mDataManager.getPeriods(filterList, "", "", 0, 0, new OnResponseCallback<List<Period>>() {
//            @Override
//            public void onSuccess(List<Period> data) {
//                periodList = data;
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//
//            }
//        });
        periodAdapter.setItems(periodList);
    }
    /*private void setFilters(){

        typeFilters = new ArrayList<>();
        typeFilters.add(new DropDownFilterView.FilterItem("Type", null, true,
                R.color.gray_5, R.drawable.t_background_tag_hollow));
        typeFilters.add(new DropDownFilterView.FilterItem("Study Task", null, false,
                R.color.white, R.drawable.t_background_tag_filled));
        typeFilters.add(new DropDownFilterView.FilterItem("Experience", null, false,
                R.color.white, R.drawable.t_background_tag_filled));
        typeFilters.add(new DropDownFilterView.FilterItem("Course", null, false,
                R.color.white, R.drawable.t_background_tag_filled));

        sessionFilters = new ArrayList<>();
        sessionFilters.add(new DropDownFilterView.FilterItem("Session", null, true,
                R.color.gray_5, R.drawable.t_background_tag_hollow));
        sessionFilters.add(new DropDownFilterView.FilterItem("Year 1", null, false,
                R.color.white, R.drawable.t_background_tag_filled));
        sessionFilters.add(new DropDownFilterView.FilterItem("Year 2", null, false,
                R.color.white, R.drawable.t_background_tag_filled));

    }

    private void fetchSchedules() {
        List<Content> contents = new ArrayList<>();
        for (int i = 0; i < 20; i++){
            Content content = new Content();
            content.setName("Schedule - " + (i+1));
            content.setIcon("http://www.humana-india.org/images/GSN_1618_GOLD.JPG");
            contents.add(content);
        }
        adapter.setItems(contents);
    }*/

    public class FiltersAdapter extends MxFiniteAdapter<ProgramFilter> {
        public FiltersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramFilter model,
                           @Nullable OnRecyclerItemClickListener<ProgramFilter> listener) {
            if (binding instanceof CommonFilterItemBinding){
                CommonFilterItemBinding itemBinding = (CommonFilterItemBinding) binding;
                typeFilters = new ArrayList<>();
                typeFilters.add(new DropDownFilterView.FilterItem("Type", null, true,
                        R.color.gray_5, R.drawable.t_background_tag_hollow));
                typeFilters.add(new DropDownFilterView.FilterItem("periods", null, false,
                        R.color.gray_5, R.drawable.t_background_tag_hollow));
                itemBinding.filterViewCommon.setFilterItems(typeFilters);


            }

        }

    }

    public class PeriodAdapter extends MxInfiniteAdapter<Period>{

        public PeriodAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Period model,
                           @Nullable OnRecyclerItemClickListener<Period> listener) {
            if (binding instanceof TRowScheduleBinding){

            }
        }
    }
}
