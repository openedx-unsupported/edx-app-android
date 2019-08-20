package org.edx.mobile.tta.ui.programs.addunits.viewmodel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowFilterDropDownBinding;
import org.edx.mobile.databinding.TRowUnitBinding;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.tta.data.local.db.table.Period;
import org.edx.mobile.tta.data.local.db.table.Unit;
import org.edx.mobile.tta.data.model.SuccessResponse;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.model.program.ProgramFilterTag;
import org.edx.mobile.tta.event.program.PeriodSavedEvent;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.custom.DropDownFilterView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class AddUnitsViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public UnitsAdapter unitsAdapter;
    public FiltersAdapter filtersAdapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();

    public Period period;
    private ProgramFilter periodFilter;
    private List<Unit> units;
    private List<Unit> selectedUnits;
    private List<ProgramFilterTag> tags;
    private List<ProgramFilter> allFilters;
    private List<ProgramFilter> filters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;
    private boolean isUnitModePeriod;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    public AddUnitsViewModel(BaseVMActivity activity, Period period) {
        super(activity);
        this.period = period;
        units = new ArrayList<>();
        selectedUnits = new ArrayList<>();
        tags = new ArrayList<>();
        filters = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;
        changesMade = true;
        isUnitModePeriod = true;

        unitsAdapter = new UnitsAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);

        unitsAdapter.setItems(units);
        unitsAdapter.setItemClickListener((view, item) -> {
            if (selectedUnits.contains(item)){
                selectedUnits.remove(item);
            } else {
                selectedUnits.add(item);
            }

            unitsAdapter.notifyItemChanged(unitsAdapter.getItemPosition(item));
        });

        mActivity.showLoading();
        fetchFilters();
        fetchData();
    }

    private void fetchFilters() {

        mDataManager.getProgramFilters(new OnResponseCallback<List<ProgramFilter>>() {
            @Override
            public void onSuccess(List<ProgramFilter> data) {
                List<ProgramFilter> removables = new ArrayList<>();
                for (ProgramFilter filter: data){
                    if (filter.getShowIn() == null || filter.getShowIn().isEmpty() ||
                            !filter.getShowIn().contains("period")){
                        removables.add(filter);
                    }
                    if (filter.getInternalName().equalsIgnoreCase("periods")){
                        ProgramFilterTag tag = new ProgramFilterTag();
                        tag.setDisplayName(period.getTitle());
                        tag.setInternalName(period.getCode());
                        tag.setId(period.getId());

                        List<ProgramFilterTag> tags = new ArrayList<>();
                        tags.add(tag);
                        filter.setTags(tags);

                        periodFilter = filter;
                    }
                }
                for (ProgramFilter filter: removables){
                    data.remove(filter);
                }

                if (!data.isEmpty()) {
                    allFilters = data;
                    filtersVisible.set(true);
                    filtersAdapter.setItems(data);
                } else {
                    filtersVisible.set(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                filtersVisible.set(false);
            }
        });

    }

    private void fetchData(){

        if (changesMade){
            isUnitModePeriod = true;
            skip = 0;
            unitsAdapter.reset(true);
            setUnitFilters();
        }

        fetchUnits();

    }

    private void setUnitFilters(){
        filters.clear();
        if (tags.isEmpty() || allFilters == null || allFilters.isEmpty()){
            return;
        }

        for (ProgramFilter filter: allFilters){

            List<ProgramFilterTag> selectedTags = new ArrayList<>();
            for (ProgramFilterTag tag: filter.getTags()){
                if (tags.contains(tag)){
                    selectedTags.add(tag);
                }
            }

            if (!selectedTags.isEmpty()){
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

    private void fetchUnits() {
        if (periodFilter != null) {
            if (isUnitModePeriod){
                if (!filters.contains(periodFilter)) {
                    filters.add(periodFilter);
                }
            } else {
                filters.remove(periodFilter);
            }
        }

        if (isUnitModePeriod) {

            mDataManager.getUnits(filters, mDataManager.getLoginPrefs().getProgramId(),
                    mDataManager.getLoginPrefs().getSectionId(), take, skip,
                    new OnResponseCallback<List<Unit>>() {
                        @Override
                        public void onSuccess(List<Unit> data) {
                            mActivity.hideLoading();
                            if (data.size() < take) {
                                isUnitModePeriod = false;
                            }

                            for (Unit unit: data){
                                if (!selectedUnits.contains(unit)){
                                    selectedUnits.add(unit);
                                }
                            }

                            populateUnits(data);
                            unitsAdapter.setLoadingDone();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            mActivity.hideLoading();
                            isUnitModePeriod = false;
//                            unitsAdapter.setLoadingDone();
//                            toggleEmptyVisibility();
                            fetchUnits();
                        }
                    });

        } else {

            mDataManager.getAllUnits(filters, mDataManager.getLoginPrefs().getProgramId(),
                    mDataManager.getLoginPrefs().getSectionId(), null, take, skip,
                    new OnResponseCallback<List<Unit>>() {
                        @Override
                        public void onSuccess(List<Unit> data) {
                            mActivity.hideLoading();
                            if (data.size() < take) {
                                allLoaded = true;
                            }
                            populateUnits(data);
                            unitsAdapter.setLoadingDone();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            mActivity.hideLoading();
                            allLoaded = true;
                            unitsAdapter.setLoadingDone();
                            toggleEmptyVisibility();
                        }
                    });

        }

    }

    private void populateUnits(List<Unit> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (Unit unit : data) {
            if (!units.contains(unit)) {
                units.add(unit);
                newItemsAdded = true;
                n++;
            }
        }

        if (newItemsAdded) {
            unitsAdapter.notifyItemRangeInserted(units.size() - n, n);
        }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (units == null || units.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public void savePeriod(){
        mActivity.showLoading();

        List<String> ids = new ArrayList<>();
        for (Unit unit: selectedUnits){
            ids.add(unit.getId());
        }
        mDataManager.savePeriod(period.getId(), ids, new OnResponseCallback<SuccessResponse>() {
            @Override
            public void onSuccess(SuccessResponse data) {
                mActivity.hideLoading();
                mActivity.showLongToast("Period saved successfully");
                period.setTotalCount(selectedUnits.size());
                EventBus.getDefault().post(new PeriodSavedEvent(period));
                mActivity.onBackPressed();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    public class FiltersAdapter extends MxFiniteAdapter<ProgramFilter> {
        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
        public FiltersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramFilter model, @Nullable OnRecyclerItemClickListener<ProgramFilter> listener) {
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
                    if (prev != null && prev.getItem() != null){
                        tags.remove((ProgramFilterTag) prev.getItem());
                    }
                    if (item.getItem() != null){
                        tags.add((ProgramFilterTag) item.getItem());
                    }

                    changesMade = true;
                    allLoaded = false;
                    mActivity.showLoading();
                    fetchData();
                });
            }
        }
    }

    public class UnitsAdapter extends MxInfiniteAdapter<Unit> {
        public UnitsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Unit model, @Nullable OnRecyclerItemClickListener<Unit> listener) {
            if (binding instanceof TRowUnitBinding) {
                TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                unitBinding.setUnit(model);

                unitBinding.checkbox.setVisibility(View.VISIBLE);
                if (selectedUnits.contains(model)){
                    unitBinding.checkbox.setChecked(true);
                } else {
                    unitBinding.checkbox.setChecked(false);
                }

                unitBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
