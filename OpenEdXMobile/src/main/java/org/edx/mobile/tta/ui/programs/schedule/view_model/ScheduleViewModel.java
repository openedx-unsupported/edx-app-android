package org.edx.mobile.tta.ui.programs.schedule.view_model;

import android.app.Dialog;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowFilterDropDownBinding;
import org.edx.mobile.databinding.TRowScheduleBinding;
import org.edx.mobile.tta.data.local.db.table.Period;
import org.edx.mobile.tta.data.model.SuccessResponse;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.model.program.ProgramFilterTag;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.custom.DropDownFilterView;

import java.util.ArrayList;
import java.util.List;

public class ScheduleViewModel extends BaseViewModel {


    public List<ProgramFilter> allFilters;
    public List<ProgramFilter> filters;
    public List<Period> periodList;
    public List<ProgramFilterTag> tags;
    public List<DropDownFilterView.FilterItem> langTags;

    private static final int TAKE = 0;
    private static final int SKIP = 10;

    private boolean allLoaded;
    private boolean changesMade;
    private int take, skip;
    private String lang;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();

    public FiltersAdapter filtersAdapter;
    public PeriodAdapter periodAdapter;
    public RecyclerView.LayoutManager gridLayoutManager;

    public ScheduleViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        filtersAdapter = new FiltersAdapter(mActivity);
        periodAdapter = new PeriodAdapter(mActivity);
        take = TAKE;
        skip = SKIP;

        mActivity.showLoading();
        getFilters();
        fetchData();

        periodAdapter.setItems(periodList);


    }

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    private void fetchData() {

        if (changesMade) {
            skip = 0;
            periodAdapter.reset(true);
            setFilters();
        }

        getPeriods();

    }

    private void setFilters() {
        filters.clear();
        if (tags.isEmpty() || allFilters == null || allFilters.isEmpty()) {
            return;
        }

        for (ProgramFilter filter : allFilters) {

            List<ProgramFilterTag> selectedTags = new ArrayList<>();
            for (ProgramFilterTag tag : filter.getTags()) {
                if (tags.contains(tag)) {
                    selectedTags.add(tag);
                }
            }

            if (!selectedTags.isEmpty()) {
                ProgramFilter pf = new ProgramFilter();
                pf.setDisplayName(filter.getDisplayName());
                pf.setInternalName(filter.getInternalName());
                pf.setId(filter.getId());
                pf.setOrder(filter.getOrder());
                pf.setShowIn(filter.getShowIn());
                pf.setTags(selectedTags);

                filters.add(pf);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        gridLayoutManager = new GridLayoutManager(mActivity, 2);

    }

    public void getFilters() {
        langTags = new ArrayList<>();
        mDataManager.getProgramFilters(new OnResponseCallback<List<ProgramFilter>>() {
            @Override
            public void onSuccess(List<ProgramFilter> data) {
                List<ProgramFilter> removables = new ArrayList<>();
                for (ProgramFilter filter : data) {
                    if (filter.getShowIn() == null || filter.getShowIn().isEmpty() ||
                            !filter.getShowIn().contains("schedule")) {
                        removables.add(filter);
                    }
                }
                for (ProgramFilter filter : removables) {
                    data.remove(filter);
                }

                if (!data.isEmpty()) {
                    allFilters = data;
                    filtersVisible.set(true);
                    filtersAdapter.setItems(data);
                } else {
                    filtersVisible.set(false);
                }

                for (int i=0; i<= data.size(); i++){
                    if (data.get(i).getInternalName().equals("lang")){

                        for (ProgramFilterTag tag : data.get(i).getTags()) {
                            langTags.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                                    false, R.color.white, R.drawable.t_background_tag_filled
                            ));
                        }
                    }
                }


            }

            @Override
            public void onFailure(Exception e) {
                filtersVisible.set(false);
            }
        });

    }


    public void getPeriods() {

        mDataManager.getPeriods(filters, mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), take, skip, new OnResponseCallback<List<Period>>() {
                    @Override
                    public void onSuccess(List<Period> data) {
                        mActivity.hideLoading();
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        populatePeriods(data);
                        periodAdapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        periodAdapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });
    }

    private void populatePeriods(List<Period> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (Period period : data) {
            if (!periodList.contains(period)) {
                periodList.add(period);
                newItemsAdded = true;
                n++;
            }
        }

        if (newItemsAdded) {
            periodAdapter.notifyItemRangeInserted(periodList.size() - n, n);
        }

        toggleEmptyVisibility();
    }

    public class FiltersAdapter extends MxFiniteAdapter<ProgramFilter> {

        public FiltersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramFilter model,
                           @Nullable OnRecyclerItemClickListener<ProgramFilter> listener) {
            if (binding instanceof TRowFilterDropDownBinding) {
                TRowFilterDropDownBinding dropDownBinding = (TRowFilterDropDownBinding) binding;

                List<DropDownFilterView.FilterItem> items = new ArrayList<>();
                items.add(new DropDownFilterView.FilterItem(model.getDisplayName(), null,
                        true, R.color.primary_cyan, R.drawable.t_background_tag_hollow
                ));
                for (ProgramFilterTag tag : model.getTags()) {
                    items.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                            false, R.color.white, R.drawable.t_background_tag_filled
                    ));
                }
                dropDownBinding.filterDropDown.setFilterItems(items);

                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {
                    if (prev != null && prev.getItem() != null) {
                        tags.remove((ProgramFilterTag) prev.getItem());
                    }
                    if (item.getItem() != null) {
                        tags.add((ProgramFilterTag) item.getItem());
                    }

                    changesMade = true;
                    allLoaded = false;
                    mActivity.showLoading();
                    getPeriods();
                });
            }
        }
    }

    public class PeriodAdapter extends MxInfiniteAdapter<Period> {

        public PeriodAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Period model,
                           @Nullable OnRecyclerItemClickListener<Period> listener) {
            if (binding instanceof TRowScheduleBinding) {
                if (model.getCompletedCount() != 0) {
                    TRowScheduleBinding itemBinding = (TRowScheduleBinding) binding;
                    itemBinding.txtCompleted.setText(String.valueOf(model.getCompletedCount()));
                }
            }
        }
    }

    private void toggleEmptyVisibility() {
        if (periodList == null || periodList.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public void addPeriod() {

        final Dialog dialog = new Dialog(mActivity);
        dialog.setContentView(R.layout.t_alert_add_period);
        Button dialogButton = (Button) dialog.findViewById(R.id.submit_button);
        DropDownFilterView drop = dialog.findViewById(R.id.filter_drop_down);
        drop.setFilterItems(langTags);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(v ->
        {
            createPeriods(lang);
            dialog.dismiss();
        });
        dialog.show();
    }

    public void createPeriods(String lang) {
        mDataManager.createPeriod(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), lang, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        getPeriods();
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
        });
    }
}
