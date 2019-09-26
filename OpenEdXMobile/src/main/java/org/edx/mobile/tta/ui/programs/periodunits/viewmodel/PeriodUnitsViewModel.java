package org.edx.mobile.tta.ui.programs.periodunits.viewmodel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowFilterDropDownBinding;
import org.edx.mobile.databinding.TRowUnitBinding;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.enums.ShowIn;
import org.edx.mobile.tta.data.enums.UnitStatusType;
import org.edx.mobile.tta.data.enums.UserRole;
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
import org.edx.mobile.tta.ui.programs.addunits.AddUnitsActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.view.Router;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class PeriodUnitsViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public UnitsAdapter unitsAdapter;
    public FiltersAdapter filtersAdapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableBoolean addUnitsVisible = new ObservableBoolean();
    public ObservableField<String> periodName = new ObservableField<>();

    private EnrolledCoursesResponse course;
    private long periodId;
    private List<Unit> units;
    private List<ProgramFilterTag> tags;
    private List<ProgramFilter> filters;
    private List<ProgramFilter> allFilters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    public PeriodUnitsViewModel(BaseVMActivity activity, long periodId, String periodName, EnrolledCoursesResponse course) {
        super(activity);

        this.course = course;
        this.periodId = periodId;
        this.periodName.set(periodName);
        units = new ArrayList<>();
        tags = new ArrayList<>();
        filters = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;
        changesMade = true;

        unitsAdapter = new UnitsAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);

        unitsAdapter.setItems(units);
        unitsAdapter.setItemClickListener((view, item) -> {

            switch (view.getId()){
                case R.id.tv_my_date:
                    showDatePicker(item);
                    break;
                default:
                    mActivity.showLoading();
                    mDataManager.getBlockComponent(item.getId(), mDataManager.getLoginPrefs().getProgramId(),
                            new OnResponseCallback<CourseComponent>() {
                                @Override
                                public void onSuccess(CourseComponent data) {
                                    mActivity.hideLoading();

                                    if (PeriodUnitsViewModel.this.course == null){
                                        mActivity.showLongSnack("You're not enrolled in the program");
                                        return;
                                    }

                                    if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {
                                        mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                                mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                                PeriodUnitsViewModel.this.course, data.getChildren().get(0).getId(),
                                                null, false);
                                    } else {
                                        mActivity.showLongSnack("This unit is empty");
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    mActivity.hideLoading();
                                    mActivity.showLongSnack(e.getLocalizedMessage());
                                }
                            });
            }

        });

        if (mDataManager.getLoginPrefs().getRole() != null &&
                mDataManager.getLoginPrefs().getRole().equalsIgnoreCase(UserRole.Instructor.name())){
            addUnitsVisible.set(true);
        } else {
            addUnitsVisible.set(false);
        }

        mActivity.showLoading();
        fetchFilters();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    private void showDatePicker(Unit unit){
        DateUtil.showDatePicker(mActivity, unit.getMyDate(), new OnResponseCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                mActivity.showLoading();
                mDataManager.setProposedDate(mDataManager.getLoginPrefs().getProgramId(),
                        mDataManager.getLoginPrefs().getSectionId(), data, unit.getPeriodId(), unit.getId(),
                        new OnResponseCallback<SuccessResponse>() {
                            @Override
                            public void onSuccess(SuccessResponse response) {
                                mActivity.hideLoading();
                                unit.setMyDate(data);
                                unitsAdapter.notifyItemChanged(unitsAdapter.getItemPosition(unit));
                            }

                            @Override
                            public void onFailure(Exception e) {
                                mActivity.hideLoading();
                                mActivity.showLongSnack(e.getLocalizedMessage());
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void fetchFilters() {

        mDataManager.getProgramFilters(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), ShowIn.periodunits.name(),
                new OnResponseCallback<List<ProgramFilter>>() {
            @Override
            public void onSuccess(List<ProgramFilter> data) {

                for (ProgramFilter filter: data){
                    if (filter.getInternalName().toLowerCase().contains("period")){
                        for (ProgramFilterTag tag: filter.getTags()){
                            if (tag.getId() == periodId){
                                tags.add(tag);
                                break;
                            }
                        }
                        break;
                    }
                }

                if (!data.isEmpty()) {
                    allFilters = data;
                    filtersVisible.set(true);
                    filtersAdapter.setItems(data);
                } else {
                    filtersVisible.set(false);
                }

                fetchData();
            }

            @Override
            public void onFailure(Exception e) {
                filtersVisible.set(false);
                fetchData();
            }
        });

    }

    private void fetchData(){

        if (changesMade){
            changesMade = false;
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

        mDataManager.getUnits(filters, mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(),mDataManager.getLoginPrefs().getRole(),
                periodId ,take, skip,
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

    public void addUnits(){
        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_PERIOD_NAME, periodName.get());
        parameters.putLong(Constants.KEY_PERIOD_ID, periodId);
        parameters.putSerializable(Router.EXTRA_COURSE_DATA, course);
        ActivityUtil.gotoPage(mActivity, AddUnitsActivity.class, parameters);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PeriodSavedEvent event) {
        if (event.getPeriodId() != periodId){
            return;
        }
        changesMade = true;
        allLoaded = false;
        mActivity.showLoading();
        fetchData();
    }

    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
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
                if (!model.getInternalName().toLowerCase().contains("period")) {
                    items.add(new DropDownFilterView.FilterItem(model.getDisplayName(), null,
                            true, R.color.primary_cyan, R.drawable.t_background_tag_hollow
                    ));
                }
                for (ProgramFilterTag tag : model.getTags()) {
                    items.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                            tags.contains(tag), R.color.white, R.drawable.t_background_tag_filled
                    ));
                }
                dropDownBinding.filterDropDown.setFilterItems(items);

                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {
                    if (prev != null && prev.getItem() != null){
                        tags.remove((ProgramFilterTag) prev.getItem());
                    }
                    if (item.getItem() != null){
                        ProgramFilterTag tag = (ProgramFilterTag) item.getItem();
                        if (model.getInternalName().toLowerCase().contains("period")){
                            periodName.set(tag.getDisplayName());
                            periodId = tag.getId();
                        }
                        tags.add(tag);
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
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Unit model,
                           @Nullable OnRecyclerItemClickListener<Unit> listener) {
            if (binding instanceof TRowUnitBinding) {
                TRowUnitBinding unitBinding = (TRowUnitBinding) binding;
                unitBinding.setUnit(model);

                unitBinding.unitCode.setText(model.getCode());
                unitBinding.layoutCheckbox.setVisibility(View.GONE);

                if (model.getMyDate() > 0){
                    unitBinding.tvMyDate.setText(DateUtil.getDisplayDate(model.getMyDate()));
                } else {
                    unitBinding.tvMyDate.setText(R.string.proposed_date);
                }

                String role = mDataManager.getLoginPrefs().getRole();
                if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name()) &&
                        !TextUtils.isEmpty(model.getStatus())){
                    try {
                        switch (UnitStatusType.valueOf(model.getStatus())){
                            case Completed:
                                unitBinding.statusIcon.setImageDrawable(
                                        ContextCompat.getDrawable(getContext(), R.drawable.t_icon_done));
                                unitBinding.statusIcon.setVisibility(View.VISIBLE);
                                break;
                            case InProgress:
                                unitBinding.statusIcon.setImageDrawable(
                                        ContextCompat.getDrawable(getContext(), R.drawable.t_icon_refresh));
                                unitBinding.statusIcon.setVisibility(View.VISIBLE);
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        unitBinding.statusIcon.setVisibility(View.GONE);
                    }
                } else {
                    unitBinding.statusIcon.setVisibility(View.GONE);
                }

                unitBinding.tvMyDate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                unitBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
