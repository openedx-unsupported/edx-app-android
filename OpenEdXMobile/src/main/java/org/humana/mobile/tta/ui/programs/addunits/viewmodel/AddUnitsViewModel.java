package org.humana.mobile.tta.ui.programs.addunits.viewmodel;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowFilterDropDownBinding;
import org.humana.mobile.databinding.TRowUnitBinding;
import org.humana.mobile.event.NetworkConnectivityChangeEvent;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.enums.ShowIn;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.data.model.program.SelectedFilter;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.PeriodSavedEvent;
import org.humana.mobile.tta.event.program.ProgramFilterSavedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
import org.humana.mobile.util.DateUtil;
import org.humana.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class AddUnitsViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public UnitsAdapter unitsAdapter;
    public FiltersAdapter filtersAdapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableField<String> periodName = new ObservableField<>();

    private EnrolledCoursesResponse course;
    private EnrolledCoursesResponse parentCourse;
    private long periodId;
    private List<Unit> units;
    private List<Unit> selectedOriginal;
    private List<Unit> unselectedOriginal;
    private List<Unit> selected;
    private List<Unit> removed;
    private List<Unit> added;
    private Map<String, Long> proposedDateModified;
    private Map<String, Long> proposedDateAdded;
    private List<ProgramFilterTag> tags;
    private List<ProgramFilter> allFilters;
    private List<ProgramFilter> filters;
    private int take, skip;
    private boolean allLoaded;
    private boolean changesMade;
    private boolean isUnitModePeriod;
    private Long selectedDate;
    public ObservableField<String> searchText = new ObservableField<>("");
    private List<SelectedFilter> selectedFilter;

    private FirebaseAnalytics mFirebaseAnalytics;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    public AddUnitsViewModel(BaseVMActivity activity, long periodId, String periodName,
                             EnrolledCoursesResponse course,
                             Long selectedDate) {
        super(activity);

        this.course = course;
        this.periodId = periodId;
        this.periodName.set(periodName);
        this.selectedDate = selectedDate;
        emptyVisible.set(false);
        units = new ArrayList<>();
        selectedOriginal = new ArrayList<>();
        unselectedOriginal = new ArrayList<>();
        selected = new ArrayList<>();
        removed = new ArrayList<>();
        added = new ArrayList<>();
        proposedDateModified = new HashMap<>();
        proposedDateAdded = new HashMap<>();
        tags = new ArrayList<>();
        filters = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;
        changesMade = true;
        isUnitModePeriod = true;

        selectedFilter=new ArrayList<>();
        selectedFilter= mDataManager.getSelectedFilters();

        unitsAdapter = new UnitsAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);

        unitsAdapter.setItems(units);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mActivity);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, mDataManager.getLoginPrefs().getUsername());
        bundle.putString(FirebaseAnalytics.Param.CONTENT, mDataManager.getLoginPrefs().getRole());
        bundle.putString(FirebaseAnalytics.Param.DESTINATION, "Add units");
        mFirebaseAnalytics.setCurrentScreen(mActivity,"Add units","Fragment");
        mFirebaseAnalytics.setUserId(mDataManager.getLoginPrefs().getUsername());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        unitsAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.layout_checkbox:
                case R.id.checkbox:

                    if (selected.contains(item)){
                        selected.remove(item);
                    } else {
                        if (selectedDate!=0){
                            item.setMyDate(selectedDate);
                            proposedDateAdded.put(item.getId(), selectedDate);
                            selected.add(item);
                        }
                        {
                            selected.add(item);
                        }
                    }

                    if (selectedOriginal.contains(item)) {
                        if (removed.contains(item)){
                            removed.remove(item);
                            proposedDateModified.remove(item.getId());
//                            item.setMyDate(0);
                        }else {
                            removed.add(item);
                        }
                    } else {
                        if (added.contains(item)){
                            added.remove(item);
                            proposedDateAdded.remove(item.getId());
//                            item.setMyDate(0);
                        }else {
                            added.add(item);
                        }
                    }

                    unitsAdapter.notifyItemChanged(unitsAdapter.getItemPosition(item));

                    break;
                case R.id.tv_my_date:
                    String title;
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())){
                        title = mActivity.getString(R.string.proposed_date);
                    }else {
                        title = mActivity.getString(R.string.my_date);
                    }
                    showDatePicker(item, title);
                    break;
                default:

                    mActivity.showLoading();
                    if (item.isPublish()) {
                        boolean ssp = selectedOriginal.contains(item);
                        EnrolledCoursesResponse c;
                        if (ssp) {
                            c = course;
                        } else {
                            c = parentCourse;
                        }

                        if (c == null) {

                            String courseId;
                            if (ssp) {
                                courseId = mDataManager.getLoginPrefs().getProgramId();
                            } else {
                                courseId = mDataManager.getLoginPrefs().getParentId();
                            }
                            mDataManager.enrolInCourse(courseId, new OnResponseCallback<ResponseBody>() {
                                @Override
                                public void onSuccess(ResponseBody responseBody) {

                                    mDataManager.getenrolledCourseByOrg(Constants.KEY_HUMANA, new OnResponseCallback<List<EnrolledCoursesResponse>>() {
                                        @Override
                                        public void onSuccess(List<EnrolledCoursesResponse> data) {
                                            if (courseId != null) {
                                                for (EnrolledCoursesResponse response : data) {
                                                    if (response.getCourse().getId().trim().toLowerCase()
                                                            .equals(courseId.trim().toLowerCase())) {
                                                        if (ssp) {
                                                            AddUnitsViewModel.this.course = response;
                                                            EventBus.getDefault().post(new CourseEnrolledEvent(response));
                                                        } else {
                                                            AddUnitsViewModel.this.parentCourse = response;
                                                        }
                                                        getBlockComponent(item);
                                                        break;
                                                    }
                                                }
                                            } else {
                                                mActivity.hideLoading();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            mActivity.hideLoading();
                                            mActivity.showLongSnack("You're not enrolled in the program");
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    mActivity.hideLoading();
                                    mActivity.showLongSnack("enroll failure");
                                }
                            });

                        } else {
                            getBlockComponent(item);
                        }

                    } else{
                        mActivity.showShortSnack(mActivity.getString(R.string.unit_not_published));
                    }

            }
        });

        mActivity.showLoading();
        fetchFilters();
//        fetchData();
    }

    private void getBlockComponent(Unit unit) {

        mDataManager.getBlockComponent(unit.getId(), mDataManager.getLoginPrefs().getProgramId(),
                new OnResponseCallback<CourseComponent>() {
                    @Override
                    public void onSuccess(CourseComponent data) {
                        mActivity.hideLoading();

                        EnrolledCoursesResponse c;
                        if (selectedOriginal.contains(unit)) {
                            c = course;
                        } else {
                            c = parentCourse;
                        }

                        if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {
                            mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                    mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                    c, data.getChildren().get(0).getId(),
                                    null, false);
                        } else {
                            mActivity.showLongSnack(mActivity.getString(R.string.empty_unit));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    public void onEventMainThread(ProgramFilterSavedEvent event) {
        changesMade = true;
        allLoaded = false;
        if (event!=null) {
            filters.clear();
            filters.addAll(event.getProgramFilters());
        }
    }

    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (NetworkUtil.isConnected(mActivity)) {

        } else {
            mActivity.showIndefiniteSnack(mActivity.getString(R.string.offline_text));
        }
    }

    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    private void showDatePicker(Unit unit, String title){
        DateUtil.showDatePicker(mActivity, unit.getMyDate(),title, new OnResponseCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                unit.setMyDate(data);
                unitsAdapter.notifyItemChanged(unitsAdapter.getItemPosition(unit));

                if (selectedOriginal.contains(unit)){
                    proposedDateModified.put(unit.getId(), data);
                } else {
                    proposedDateAdded.put(unit.getId(), data);
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void fetchFilters() {
        mDataManager.getProgramFilters(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), ShowIn.addunits.name(),filters,
                new OnResponseCallback<List<ProgramFilter>>() {
                    @Override
                    public void onSuccess(List<ProgramFilter> data) {
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
                    }
                });

    }

    public void fetchData(){
        mActivity.showLoading();
        if (changesMade){
            units.clear();
            selectedOriginal.clear();
            unselectedOriginal.clear();
            selected.clear();
            removed.clear();
            added.clear();

            changesMade = false;
            isUnitModePeriod = true;
            skip = 0;
            unitsAdapter.reset(true);
            setUnitFilters();
        }

        fetchUnits();

    }

    private void setUnitFilters(){
        if (filters != null)
            filters.clear();
        if (allFilters == null || allFilters.isEmpty()) {
            return;
        }
        if (selectedFilter.isEmpty()) {
            for (ProgramFilter filter : allFilters) {
                for (ProgramFilterTag tag : filter.getTags()) {
                    if (tag.getSelected()) {
                        SelectedFilter sf = new SelectedFilter();
                        sf.setInternal_name(filter.getInternalName());
                        sf.setDisplay_name(filter.getDisplayName());
                        sf.setSelected_tag(tag.getDisplayName());
                        sf.setSelected_tag_item(tag);
                        mDataManager.updateSelectedFilters(sf);
                        selectedFilter.addAll(mDataManager.getSelectedFilters());
                        break;
                    }
                }
            }


        }
        for (SelectedFilter selected : selectedFilter) {
            for (ProgramFilter filter : allFilters) {
                List<ProgramFilterTag> selectedTags = new ArrayList<>();
                if (selected.getInternal_name().equalsIgnoreCase(filter.getInternalName())) {
                    for (ProgramFilterTag tag : filter.getTags()) {
                        if (selected.getSelected_tag() != null) {
                            if (selected.getSelected_tag_item()!=null) {
                                if (selected.getSelected_tag_item().equals(tag)) {
                                    selectedTags.add(tag);
                                    ProgramFilter pf = new ProgramFilter();
                                    pf.setDisplayName(filter.getDisplayName());
                                    pf.setInternalName(filter.getInternalName());
                                    pf.setId(filter.getId());
                                    pf.setOrder(filter.getOrder());
                                    pf.setShowIn(filter.getShowIn());
                                    pf.setTags(selectedTags);
                                    if (!filters.contains(pf)) {
                                        filters.add(pf);
                                    }
                                    break;
                                }
                            }else {
                                if (selected.getSelected_tag().equals(tag.getDisplayName())) {
                                    selectedTags.add(tag);
                                    ProgramFilter pf = new ProgramFilter();
                                    pf.setDisplayName(filter.getDisplayName());
                                    pf.setInternalName(filter.getInternalName());
                                    pf.setId(filter.getId());
                                    pf.setOrder(filter.getOrder());
                                    pf.setShowIn(filter.getShowIn());
                                    pf.setTags(selectedTags);
                                    if (!filters.contains(pf)) {
                                        filters.add(pf);
                                    }

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void fetchUnits() {

        if (isUnitModePeriod) {
            mDataManager.getUnits(filters, searchText.get(),mDataManager.getLoginPrefs().getProgramId(),
                    mDataManager.getLoginPrefs().getSectionId(),mDataManager.getLoginPrefs().getRole(),"",
                    periodId ,take, skip,0L,0L,
                    new OnResponseCallback<List<Unit>>() {
                        @Override
                        public void onSuccess(List<Unit> data) {
                            if (data.size() < take) {
                                isUnitModePeriod = false;
                                skip = -1;
                            }

                            for (Unit unit: data){
                                if (!selectedOriginal.contains(unit)){
                                    selectedOriginal.add(unit);
                                }
                            }
                            for (Unit unit: data){
                                if (!selected.contains(unit)){
                                    selected.add(unit);
                                }
                            }
                            populateUnits(data);
//                            unitsAdapter.setLoadingDone();


                        }

                        @Override
                        public void onFailure(Exception e) {
                            mActivity.hideLoading();
                            isUnitModePeriod = false;
//                            unitsAdapter.setLoadingDone();
                            skip = 0;
                            fetchUnits();
//                            toggleEmptyVisibility();

                        }
                    });

        } else {
            if (unitsAdapter.getItemCount()==0){
                mActivity.showLoading();
            }else {
                mActivity.hideLoading();
            }
            mDataManager.getAllUnits(filters, mDataManager.getLoginPrefs().getProgramId(),
                    mDataManager.getLoginPrefs().getSectionId(), searchText.get(), periodId, take, skip,
                    new OnResponseCallback<List<Unit>>() {
                        @Override
                        public void onSuccess(List<Unit> data) {
                            if (data.size() < take) {
                                allLoaded = true;
                            }

                            for (Unit unit: data){
                                if (!unselectedOriginal.contains(unit) && !selected.contains(unit)){
                                    unselectedOriginal.add(unit);
                                }
                            }
                            populateUnits(data);
                            unitsAdapter.setLoadingDone();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            allLoaded = true;
                            unitsAdapter.setLoadingDone();
                            toggleEmptyVisibility();
                            mActivity.hideLoading();
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
        mActivity.hideLoading();
        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (units == null || units.isEmpty()) {
            emptyVisible.set(true);
            mActivity.hideLoading();

        } else {
            emptyVisible.set(false);
            mActivity.hideLoading();

        }
    }

    public void savePeriod(){
        mActivity.showLoading();

        List<String> addedIds = new ArrayList<>();
        for (Unit unit: added){
            addedIds.add(unit.getId());
        }

        List<String> removedIds = new ArrayList<>();
        for (Unit unit: removed){
            removedIds.add(unit.getId());
        }

        mDataManager.savePeriod(periodId, addedIds, removedIds, proposedDateModified, proposedDateAdded,
                new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        mActivity.hideLoading();
                        mActivity.showLongToast(mActivity.getString(R.string.period_saved_successed));
                        EventBus.getDefault().post(
                                new PeriodSavedEvent(periodId, added.size() - removed.size()));
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
                            tag.getSelected(), R.color.white, R.drawable.t_background_tag_filled
                    ));
                }
                dropDownBinding.filterDropDown.setFilterItems(items);

                if (selectedFilter != null) {
                    for (ProgramFilterTag tag : model.getTags()) {
                        for (SelectedFilter item : selectedFilter) {
                            if (item.getInternal_name().equalsIgnoreCase(model.getInternalName())) {
                                if (item.getSelected_tag_item()!=null) {
                                    if (item.getSelected_tag_item().equals(tag)) {
                                        dropDownBinding.filterDropDown.setSelection(item.getSelected_tag_item());
                                    }
                                    break;
                                }else {
                                    if (item.getSelected_tag().equals(model.getDisplayName())) {
                                        dropDownBinding.filterDropDown.setSelection(item.getDisplay_name());
                                    }

                                }
                            }
                        }
                    }
                }

                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {
//                    if (prev != null && prev.getItem() != null){
//                        tags.remove((ProgramFilterTag) prev.getItem());
//                    }
//                    if (item.getItem() != null){
//                        tags.add((ProgramFilterTag) item.getItem());
//                    }

                    SelectedFilter sf = new SelectedFilter();
                    sf.setInternal_name(model.getInternalName());
                    sf.setDisplay_name(model.getDisplayName());
                    sf.setSelected_tag(item.getName());
                    sf.setSelected_tag_item((ProgramFilterTag) item.getItem());
                    mDataManager.updateSelectedFilters(sf);

                    changesMade = true;
                    allLoaded = false;
                    mActivity.showLoading();
                    selectedFilter = mDataManager.getSelectedFilters();
                    filters.clear();
                    for (SelectedFilter selected : selectedFilter) {
                        for (ProgramFilter filter : allFilters) {
                            List<ProgramFilterTag> selectedTags = new ArrayList<>();
                            if (selected.getInternal_name().equalsIgnoreCase(filter.getInternalName())) {
                                for (ProgramFilterTag tag : filter.getTags()) {
                                    if (selected.getSelected_tag() != null) {
                                        if (selected.getSelected_tag_item()!=null) {
                                            if (selected.getSelected_tag_item().equals(tag)) {
                                                selectedTags.add(tag);
                                                ProgramFilter pf = new ProgramFilter();
                                                pf.setDisplayName(filter.getDisplayName());
                                                pf.setInternalName(filter.getInternalName());
                                                pf.setId(filter.getId());
                                                pf.setOrder(filter.getOrder());
                                                pf.setShowIn(filter.getShowIn());
                                                pf.setTags(selectedTags);
                                                if (!filters.contains(pf)) {
                                                    filters.add(pf);
                                                }
                                                break;
                                            }
                                        }else {
                                            if (selected.getSelected_tag().equals(tag.getDisplayName())) {
                                                selectedTags.add(tag);
                                                ProgramFilter pf = new ProgramFilter();
                                                pf.setDisplayName(filter.getDisplayName());
                                                pf.setInternalName(filter.getInternalName());
                                                pf.setId(filter.getId());
                                                pf.setOrder(filter.getOrder());
                                                pf.setShowIn(filter.getShowIn());
                                                pf.setTags(selectedTags);
                                                if (!filters.contains(pf)) {
                                                    filters.add(pf);
                                                }
                                                break;
                                            } else if (selected.getSelected_tag().equals(filter.getDisplayName())) {
//                                                selectedTags.add(tag);
                                                ProgramFilter pf = new ProgramFilter();
                                                pf.setDisplayName(filter.getDisplayName());
                                                pf.setInternalName(filter.getInternalName());
                                                pf.setId(filter.getId());
                                                pf.setOrder(filter.getOrder());
                                                pf.setShowIn(filter.getShowIn());
                                                pf.setTags(selectedTags);
                                                if (!filters.contains(pf)) {
                                                    filters.add(pf);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
//                    EventBus.getDefault()
//                            .post(new ProgramFilterSavedEvent(filters, true));
                    fetchFilters();
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

                unitBinding.checkbox.setVisibility(View.VISIBLE);
                if (selected.contains(model)){
                    unitBinding.checkbox.setChecked(true);

                    if (model.getMyDate() > 0){
                        unitBinding.tvMyDate.setText(DateUtil.getDisplayDate(model.getMyDate()));
                    } else {
                        unitBinding.tvMyDate.setText(R.string.change_date);
                    }
                    unitBinding.tvMyDate.setVisibility(View.VISIBLE);
                } else {
                    unitBinding.checkbox.setChecked(false);
                    unitBinding.tvMyDate.setVisibility(View.INVISIBLE);
                }

                unitBinding.unitCode.setText(model.getTitle());
                unitBinding.unitTitle.setText(model.getCode() + "  |  " + model.getType() + " | "
                        + model.getUnitHour() + " "+ mActivity.getResources().getString(R.string.point_txt));
                try {
                    if (!model.getStatus().equals("") || model.getStatus() != null) {
                        if (model.getStatusDate() > 0) {
                            if ( DateUtil.getDisplayDate(model.getStatusDate()).equals("")) {
                                unitBinding.tvStaffDate.setText(model.getStatus() + " : "
                                        + DateUtil.getDisplayDate(model.getStatusDate()));
                                unitBinding.tvStaffDate.setVisibility(View.VISIBLE);
                            }unitBinding.tvStaffDate.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        unitBinding.tvStaffDate.setVisibility(View.INVISIBLE);
                    }
                    unitBinding.tvDescription.setText(model.getDesc());
                    String role = mDataManager.getLoginPrefs().getRole();
                    if (role != null && role.trim().equalsIgnoreCase(UserRole.Student.name())) {
                        if (model.getStaffDate() > 0) {
                            unitBinding.tvSubmittedDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                            unitBinding.tvSubmittedDate.setVisibility(View.VISIBLE);
                        } else {
                            unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);
                        }
                    }else {
                        unitBinding.tvSubmittedDate.setVisibility(View.INVISIBLE);
                    }
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                        if (!model.getStatus().equals("")) {
                            unitBinding.tvComment.setText(model.getStatus() + " comments : " + model.getComment());
                            if(model.getStatus().equals("Submitted")){
                                unitBinding.tvComment.setVisibility(View.GONE);
                            }else {
                                unitBinding.tvComment.setVisibility(View.VISIBLE);
                            }
                        }
                        else {
                            unitBinding.tvComment.setVisibility(View.GONE);
                        }
                    }else {
                        unitBinding.tvComment.setVisibility(View.GONE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (model.getType().toLowerCase().equals(mActivity.getString(R.string.course).toLowerCase())){
                    if (mDataManager.getLoginPrefs().getRole() != null
                            && mDataManager.getLoginPrefs().getRole()
                            .equals(UserRole.Student.name())){
                        unitBinding.tvMyDate.setEnabled(false);
                    }else {
                        unitBinding.tvMyDate.setEnabled(true);
                    }
                }else {
                    unitBinding.tvMyDate.setEnabled(true);
                }

                String role = mDataManager.getLoginPrefs().getRole();
                if (role != null && role.equals(UserRole.Student.name())){
                    unitBinding.tvMyDate.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.student_icon,
                            0,0,0);
                }else {
                    unitBinding.tvMyDate.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.teacher_icon,
                            0,0,0);
                }

                switch (model.getStatus()) {
                    case "Submitted":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.pending));
                        break;
                    case "Approved":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_green));
                        break;
                    case "Return":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_red));
                        break;
                    case "":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.humana_card_background));
                        break;
                    case "None":
                        unitBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.humana_card_background));
                        break;
                }
                unitBinding.checkbox.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                unitBinding.checkbox.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

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

    public TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            searchText.set(s.toString());

        }
    };

    public void searchFilter(){
        mActivity.hideLoading();
        isUnitModePeriod = false;
        unitsAdapter.setLoadingDone();
        skip = 0;
        changesMade = true;
        fetchData();
    }
}
