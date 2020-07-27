package org.humana.mobile.tta.ui.programs.schedule.view_model;

import android.app.Dialog;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.ankit.mxrangepicker.date.DatePickerDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowFilterDropDownBinding;
import org.humana.mobile.databinding.TRowScheduleBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.model.download.PDFDownloadModel;
import org.humana.mobile.services.DownloadService;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.enums.ShowIn;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Period;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.data.model.program.SelectedFilter;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.PeriodSavedEvent;
import org.humana.mobile.tta.event.program.ProgramFilterSavedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
import org.humana.mobile.tta.ui.programs.addunits.AddUnitsActivity;
import org.humana.mobile.tta.ui.programs.periodunits.PeriodUnitsActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.tta.utils.AppUtil;
import org.humana.mobile.util.DateUtil;
import org.humana.mobile.view.Router;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class ScheduleViewModel extends BaseViewModel implements DatePickerDialog.OnDateSetListener {

    private static final int TAKE = 10;
    private static final int SKIP = 0;
    private List<SelectedFilter> selectedFilter;
    private EnrolledCoursesResponse course;
    private List<ProgramFilter> allFilters;
    public List<ProgramFilter> filters;
    private List<Period> periodList;
    private List<ProgramFilterTag> tags;
    private List<DropDownFilterView.FilterItem> langTags;
    private List<DropDownFilterView.FilterItem> sessionTags;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableBoolean fabVisible = new ObservableBoolean();
    public ObservableBoolean readMore = new ObservableBoolean();
    public ObservableBoolean progressVisible = new ObservableBoolean();
    public ObservableField<String> toolTipText = new ObservableField<>();
    public ObservableInt toolTipGravity = new ObservableInt();

    public ObservableField<String> toolTipAddUnitText = new ObservableField<>();
    public ObservableInt toolTipAddUnitGravity = new ObservableInt();
    public ObservableInt toolTipAddUnitPosition = new ObservableInt();

    public ObservableField<String> toolTipFilterText = new ObservableField<>();
    public ObservableInt toolTipFilterGravity = new ObservableInt();

    public ObservableField<Long> startDate = new ObservableField<>();
    public ObservableField<Long> endDate = new ObservableField<>();
    public ObservableField<Period> periodItem = new ObservableField<>();

    public FiltersAdapter filtersAdapter;
    public PeriodAdapter periodAdapter;
    public RecyclerView.LayoutManager gridLayoutManager;

    private boolean allLoaded;
    private boolean isFilterChange;
    private boolean changesMade;
    private int take, skip;
    private boolean isSelected;

    private long currentDate, lastDate;
    public List<ProgramFilterTag> selectedTags = new ArrayList<>();
    public Calendar calendar;
    public final String START_DATE = "startDate";
    public final String END_DATE = "endDate";
    CourseComponent courseComponent;
    String about_url;
    private List<Period> savedPeriods = new ArrayList<>();

    //   private Tracker mTracker;
//   private RuntimeApplication application;
    private FirebaseAnalytics mFirebaseAnalytics;


    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        else {
            this.skip++;
            fetchData();
//            getDownloadPeriodDesc(mDataManager.getLoginPrefs().getUsername());
            return true;
        }
    };
    private String lang;

    public ScheduleViewModel(Context context, TaBaseFragment fragment, EnrolledCoursesResponse course) {
        super(context, fragment);
        this.course = course;
        filters = new ArrayList<>();
        periodList = new ArrayList<>();
        tags = new ArrayList<>();
        mActivity.showLoading();
        isSelected = false;
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        currentDate = calendar.getTimeInMillis();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        lastDate = calendar.getTimeInMillis();
        readMore.set(false);
        about_url = "";
        selectedFilter = new ArrayList<>();
        isFilterChange = false;


        emptyVisible.set(false);
        progressVisible.set(false);

        filtersAdapter = new FiltersAdapter(mActivity);
        periodAdapter = new PeriodAdapter(mActivity);
        take = TAKE;
        skip = SKIP;
        allLoaded = false;
        changesMade = true;
        periodAdapter.setItems(periodList);
        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
            fabVisible.set(true);
        }


        // Google Analytic start here
//        mTracker = MainApplication.instance().getDefaultTracker();
//        Log.i("Schedule", "Schedule screen tracked ");
//        mTracker.setScreenName("Schedule");
//        mTracker.send(new HitBuilders.EventBuilder()
//                .setCategory("Action")
//                .setAction("Share")
//                .set(Fields.customDimension(1), "premiumUser")
//                .build());

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mActivity);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, mDataManager.getLoginPrefs().getUsername());
        bundle.putString(FirebaseAnalytics.Param.CONTENT, mDataManager.getLoginPrefs().getRole());
        bundle.putString(FirebaseAnalytics.Param.DESTINATION, "Schedule");
        bundle.putString("page_loaded","Schedule Screen");
        mFirebaseAnalytics.setCurrentScreen(mActivity,"Schedule","Fragment");
        mFirebaseAnalytics.setUserId(mDataManager.getLoginPrefs().getUsername());
//        mFirebaseAnalytics.setUserId(mDataManager.getLoginPrefs());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        // Google Analytic end here


        periodAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()) {
                case R.id.textview_add:
                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_PERIOD_NAME, item.getTitle());
                    parameters.putLong(Constants.KEY_PERIOD_ID, item.getId());
                    parameters.putLong("total_points", item.getTotal_points());
                    parameters.putSerializable(Router.EXTRA_COURSE_DATA, ScheduleViewModel.this.course);
                    ActivityUtil.gotoPage(mActivity, AddUnitsActivity.class, parameters);
                    break;
                case R.id.txt_start_date:

                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        periodItem.set(item);
                        rangePicker(item.getStartDate(), item.getEndDate(), true);
                    }
                    break;

                case R.id.txt_end_date:
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        periodItem.set(item);
                        rangePicker(item.getStartDate(), item.getEndDate(), false);
                    }
                    break;
                case R.id.iv_start_date:
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        periodItem.set(item);
                        rangePicker(item.getStartDate(), item.getEndDate(), true);
                    }
                    break;
                case R.id.iv_end_date:
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        periodItem.set(item);
                        rangePicker(item.getStartDate(), item.getEndDate(), false);
                    }
                    break;

                case R.id.ll_read_more:
                    if (AppUtil.isReadStoragePermissionGranted(mActivity)
                            && AppUtil.isWriteStoragePermissionGranted(mActivity)) {
                        if (item.getDownloadStatus() != null) {
                            if (!item.getDownloadStatus().equals("Downloaded")) {
                                item.setDownloadStatus(mActivity.getString(R.string.downloading));
                                periodAdapter.notifyItemChanged(periodAdapter.getItemPosition(item));
                                DownloadService.getDownloadService(mActivity,
                                        item.getAbout_url(),
                                        item.getTitle(),
                                        item.getId(),
                                        item);

                            } else {
                                Uri uri = Uri.parse(item.getAbout_url());
                                DownloadService.openDownloadedFile(uri, mActivity);
                            }
                        } else {
                            item.setDownloadStatus(mActivity.getString(R.string.downloading));
                            periodAdapter.notifyItemChanged(periodAdapter.getItemPosition(item));
                            DownloadService.getDownloadService(mActivity,
                                    item.getAbout_url(),
                                    item.getTitle(),
                                    item.getId(),
                                    item);
                        }
                    }

                    break;

                default:
                    Bundle parameters1 = new Bundle();
                    parameters1.putString(Constants.KEY_PERIOD_NAME, item.getTitle());
                    parameters1.putLong(Constants.KEY_PERIOD_ID, item.getId());
                    parameters1.putSerializable(Router.EXTRA_COURSE_DATA, ScheduleViewModel.this.course);
                    ActivityUtil.gotoPage(mActivity, PeriodUnitsActivity.class, parameters1);
            }
        });

        mActivity.showLoading();
        getFilters();
    }

    public void fetchData() {
        mActivity.showLoading();
        if (changesMade) {
            changesMade = false;
            skip = 0;
            periodAdapter.reset(true);
            setFilters();
        }
        getPeriods();
    }


    private void getDownloadPeriodDesc(String userName) {
        savedPeriods.clear();
        mDataManager.getPeriodDesc(userName,
                new OnResponseCallback<List<Period>>() {
                    @Override
                    public void onSuccess(List<Period> periods) {
                        for (Period period : periods) {
                            if (period.getDownloadStatus() != null) {
                                savedPeriods.add(period);
                            }
                        }


                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });

    }

    public void setToolTip() {
        if (fabVisible.get()) {
            toolTipGravity.set(Gravity.TOP);
            toolTipText.set("Create period from here");
        }

        toolTipFilterGravity.set(Gravity.BOTTOM);
        toolTipFilterText.set("Filter results..");

        toolTipAddUnitGravity.set(Gravity.TOP);
        toolTipAddUnitText.set("Add unit here");
        toolTipAddUnitPosition.set(0);

    }


    private void setFilters() {
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
                        selectedFilter = mDataManager.getSelectedFilters();
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
                            if (selected.getSelected_tag_item() != null) {
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
                            } else {
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

    @Override
    public void onResume() {
        super.onResume();
        boolean tabletSize = mActivity.getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            gridLayoutManager = new GridLayoutManager(mActivity, 2);
        } else {
            gridLayoutManager = new GridLayoutManager(mActivity, 1);
        }
        selectedFilter = mDataManager.getSelectedFilters();

        changesMade = true;
        if (isFilterChange) {
            getFilters();
        }
        getDownloadPeriodDesc(mDataManager.getLoginPrefs().getUsername());

    }

    public void getFilters() {
        langTags = new ArrayList<>();
        sessionTags = new ArrayList<>();
        mDataManager.getProgramFilters(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), ShowIn.schedule.name(), filters,
                new OnResponseCallback<List<ProgramFilter>>() {
                    @Override
                    public void onSuccess(List<ProgramFilter> data) {
                        langTags = new ArrayList<>();
                        if (!data.isEmpty()) {
                            allFilters = data;
                            filtersVisible.set(true);
                            filtersAdapter.setItems(data);
                            changesMade = true;
                            isFilterChange = false;
                            fetchData();

                            if (mDataManager.getLoginPrefs().getRole() != null) {
                                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {

                                    for (ProgramFilter filter : data) {

                                        if (filter.getInternalName().toLowerCase().contains("lang")) {
                                            langTags.clear();
                                            langTags.add(new DropDownFilterView.FilterItem(filter.getDisplayName(), null,
                                                    true, R.color.primary_cyan, R.drawable.t_background_tag_hollow));

                                            for (ProgramFilterTag tag : filter.getTags()) {
                                                langTags.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                                                        false, R.color.white, R.drawable.t_background_tag_filled));


                                            }
                                        }

                                    }

                                } else {
                                    fabVisible.set(false);
                                }
                            }

                        } else {
                            filtersVisible.set(false);
                            if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                                fabVisible.set(true);
                            }
                        }

                    }

                    @Override
                    public void onFailure(Exception e) {
                        filtersVisible.set(false);
                        fabVisible.set(false);
                        fetchData();
                    }
                });

    }


    private void getPeriods() {
        mActivity.showLoading();
        mDataManager.getPeriods(filters, mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), mDataManager.getLoginPrefs().getRole()
                , take, skip, 0, new OnResponseCallback<List<Period>>() {
                    @Override
                    public void onSuccess(List<Period> data) {
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        List<Period> ps = new ArrayList<>();
                        ps = data;
                        if (savedPeriods.size() != 0) {
                            for (Period savedPeriod : savedPeriods) {
                                for (int i = 0; i < data.size(); i++) {
                                    if (data.get(i).getId() == savedPeriod.getId() &&
                                            data.get(i).getAbout_url().equalsIgnoreCase(savedPeriod.getAbout_url())) {
                                        ps.remove(data.get(i));
                                        ps.add(i, savedPeriod);
                                        break;
                                    }
                                }
                            }
                            populatePeriods(ps);
                        } else {
                            populatePeriods(ps);
                        }
                        emptyVisible.set(false);
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
            periodAdapter.notifyItemRangeInserted(periodList.size() - (n - 1), n);
        }
        mActivity.hideLoading();
        toggleEmptyVisibility();

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PeriodSavedEvent event) {
        Period period = new Period();
        period.setId(event.getPeriodId());
        int position = periodAdapter.getItemPosition(period);
        if (position >= 0) {
            int totalPoints = 0;
            Period p = periodList.get(position);
            totalPoints = p.getTotal_points();
            p.setTotalCount(p.getTotalCount() + event.getUnitsCountChange());
            p.setTotal_points((int) event.getPointCountChange() + totalPoints);
            periodAdapter.notifyItemChanged(position);
        }
    }


    public void onEventMainThread(ProgramFilterSavedEvent event) {
        changesMade = true;
        allLoaded = false;
//        filters.clear();
        filters = event.getProgramFilters();
        if (event.getFetchFilters()) {
            isFilterChange = true;
        }
    }

    public void onEventMainThread(PDFDownloadModel model) {
        if (model.isDownload()) {
            progressVisible.set(false);
            periodAdapter.notifyDataSetChanged();
        }
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(CourseEnrolledEvent event) {
        this.course = event.getCourse();
    }

    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
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
        Button dialogButton = dialog.findViewById(R.id.submit_button);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
//        EditText dialogText =  dialog.findViewById(R.id.et_period_name);
        DropDownFilterView drop = dialog.findViewById(R.id.filter_drop_down);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        drop.setFilterItems(langTags);
        drop.setOnFilterItemListener((v, item, position, prev) -> {
            if (item.getItem() == null) {
                lang = null;
            } else {
                lang = ((ProgramFilterTag) item.getItem()).getInternalName();
            }
        });
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(v ->
        {

            if (lang == null) {
                mActivity.showLongSnack("Please select a language");
                return;
            }
//            String periodName = dialogText.getText().toString();
            createPeriods(lang);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();
    }

    private void createPeriods(String lang) {
        mActivity.showLoading();
        mDataManager.createPeriod(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), lang, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        if (data.getSuccess()) {
                            changesMade = true;
                            allLoaded = false;
                            mActivity.showLongSnack("Periods created successfully");
                            fetchData();
                        } else {
                            mActivity.hideLoading();
                            mActivity.showLongSnack("Periods with the selected language already exist");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });
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
                //String selectedTag = "";
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
                                if (item.getSelected_tag_item() != null) {
                                    if (item.getSelected_tag_item().equals(tag)) {
                                        dropDownBinding.filterDropDown.setSelection(item.getSelected_tag_item());
                                    }
                                    break;
                                } else {
                                    if (item.getSelected_tag().equals(model.getDisplayName())) {
                                        dropDownBinding.filterDropDown.setSelection(item.getDisplay_name());
                                    }

                                }
                            }
                        }
                    }
                }


                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {

                    SelectedFilter sf = new SelectedFilter();
                    sf.setInternal_name(model.getInternalName());
                    sf.setDisplay_name(model.getDisplayName());
                    sf.setSelected_tag_item((ProgramFilterTag) item.getItem());
                    sf.setSelected_tag(item.getName());

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
                                        if (selected.getSelected_tag_item() != null) {
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
                                        } else {
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
//                            .post(new ProgramFilterSavedEvent(filters,false));
                    getFilters();
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
                TRowScheduleBinding scheduleBinding = (TRowScheduleBinding) binding;
                scheduleBinding.setPeriod(model);

                scheduleBinding.textviewAdd.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
                    scheduleBinding.txtTotal.setText(model.getCompletedCount() + "/" +
                            model.getTotalCount() + " Units ");

                } else {
                    scheduleBinding.txtTotal.setText(model.getTotalCount() + " Units ");
                }

                if(mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())){
                        scheduleBinding.txtCompleted.setVisibility(View.VISIBLE);
                        scheduleBinding.txtCompleted.setText(model.getCompleted_points() + "/" +
                                model.getTotal_points() + " Points ");

                }else{
                    scheduleBinding.txtCompleted.setVisibility(View.INVISIBLE);
                }

                if (model.getStartDate() > 0) {
                    scheduleBinding.txtStartDate.setText(DateUtil.getDisplayDate(model.getStartDate()));
                } else {
                    scheduleBinding.txtStartDate.setText("start date");

                }
                if (model.getEndDate() > 0) {
                    scheduleBinding.txtEndDate.setText(DateUtil.getDisplayDate(model.getEndDate()));
                } else {
                    scheduleBinding.txtEndDate.setText("end date");
                }

                if (model.getStartDate() > 0) {
                    if (model.getEndDate() > model.getStartDate()) {
                        if (currentDate <= model.getStartDate() && lastDate >= model.getEndDate()) {
                            scheduleBinding.card.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.humana_current_period));
                        } else {
                            scheduleBinding.card.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.humana_card_background));
                        }
                    }
                } else {
                    scheduleBinding.card.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.humana_card_background));
                }


                if (model.getDesc() != null) {
                    if (model.getAbout_url() != null && !model.getAbout_url().equals("")) {
                        scheduleBinding.llReadMore.setVisibility(View.VISIBLE);

                    } else {
                        scheduleBinding.llReadMore.setVisibility(View.GONE);

                    }
                }
                if (model.getDownloadStatus() != null) {
                    if (model.getDownloadStatus()
                            .equalsIgnoreCase(mActivity.getString(R.string.downloaded))) {
                        scheduleBinding.txtReadMore.setBackground(
                                ContextCompat.getDrawable(mActivity, R.drawable.ic_after_dnd));
                        scheduleBinding.pbDownload.setVisibility(View.GONE);
                        scheduleBinding.llReadMore.setVisibility(View.VISIBLE);
                        scheduleBinding.tvReadMore.setVisibility(View.GONE);
                    } else if (model.getDownloadStatus().equals(mActivity.getString(R.string.downloading))) {
                        scheduleBinding.pbDownload.setVisibility(View.VISIBLE);
                        scheduleBinding.llReadMore.setVisibility(View.GONE);
                    }
                }

               /* if (getItemPosition(model) == 0) {
                    if (!mDataManager.getLoginPrefs().isScheduleTootipSeen()) {
                        new MxTooltip.Builder(mActivity)
                                .anchorView(scheduleBinding.textviewAdd)
                                .text(toolTipAddUnitText.get())
                                .gravity(toolTipAddUnitGravity.get())
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.down_arrow)
                                .build()
                                .show();

                        mDataManager.getLoginPrefs().setScheduleTootipSeen(true);
                    }
                }*/

                scheduleBinding.txtStartDate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
                scheduleBinding.ivStartDate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
                scheduleBinding.ivEndDate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
                scheduleBinding.txtEndDate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
                scheduleBinding.llReadMore.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
                scheduleBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

            }
        }

    }

    private void rangePicker(Long startDate, Long endDate, boolean isStart) {
        Calendar now = Calendar.getInstance();
        Calendar now2 = Calendar.getInstance();

        if (startDate != 0) {
            String startDateStr;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00 a", Locale.ENGLISH);
            startDateStr = df.format(startDate);
            Date mstartDate = null;
            try {
                mstartDate = df.parse(startDateStr);
                now.setTime(mstartDate);
                now.set(Calendar.HOUR_OF_DAY, 0);
                now.set(Calendar.MINUTE, 0);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (endDate != 0) {

            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH);
            String endDateStr = df1.format(endDate);
            Date mendDate = null;
            try {
                mendDate = df1.parse(endDateStr);
                now2.setTime(mendDate);
                now2.set(Calendar.HOUR_OF_DAY, 23);
                now2.set(Calendar.MINUTE, 59);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH),
                now2.get(Calendar.YEAR),
                now2.get(Calendar.MONTH),
                now2.get(Calendar.DAY_OF_MONTH),
                isStart
                // Inital day selection

        );

// If you're calling this from a support Fragment
        dpd.show(mActivity.getFragmentManager(), "Datepickerdialog");

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear,
                          int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {


        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        String str;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00 a", Locale.ENGLISH);
        str = df.format(c.getTime());
        try {

            Date date11 = df.parse(str);
            long epoch = date11.getTime();
            startDate.set(epoch);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.YEAR, yearEnd);
        c1.set(Calendar.MONTH, monthOfYearEnd);
        c1.set(Calendar.DAY_OF_MONTH, dayOfMonthEnd);
        c1.set(Calendar.HOUR_OF_DAY, 23);
        c1.set(Calendar.MINUTE, 59);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH);
        str = sdf.format(c1.getTime());
        try {

            Date date11 = sdf.parse(str);
            long epoch = date11.getTime();
            endDate.set(epoch);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (startDate.get() < endDate.get()) {
            updatePeriod();
        } else {
            mActivity.showLongSnack("wrong dates selected");
        }


    }

    public void setSessionFilter() {
        selectedFilter = mDataManager.getSelectedFilters();
        changesMade = true;
        allLoaded = false;
        mActivity.showLoading();
        getFilters();
        getDownloadPeriodDesc(mDataManager.getLoginPrefs().getUsername());
    }

    public void updatePeriod() {
        mActivity.showLoading();
        mDataManager.updatePeriods(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), String.valueOf(periodItem.get().getId()),
                periodItem.get().getTitle(), startDate.get(),
                endDate.get(),
                new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse response) {
                        mActivity.hideLoading();
                        periodItem.get().setStartDate(startDate.get());
                        periodItem.get().setEndDate(endDate.get());
                        periodAdapter.notifyItemChanged(periodAdapter.getItemPosition(periodItem.get()));

                        if (response.getSuccess()) {
                            mActivity.showLongSnack("Date set successfully");
                            fetchData();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });
    }

}
