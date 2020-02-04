package org.humana.mobile.tta.ui.programs.units.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.databinding.RowPeriodListingBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.local.db.table.Period;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.programs.addunits.AddUnitsActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.view.Router;

import java.util.ArrayList;
import java.util.List;

public class PeriodListingViewModel extends BaseViewModel {
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public PeriodAdapter periodAdapter;
    public LinearLayoutManager layoutManager;

    private List<Period> periods;
    public List<ProgramFilter> filters;
    private int take, skip;
    private  EnrolledCoursesResponse course;
    private final String SELECTED_DATE= "selected_date";
    private long seletedDate;
    public ObservableBoolean rowVisible = new ObservableBoolean();

    public PeriodListingViewModel(BaseVMActivity activity, EnrolledCoursesResponse course, Long selectedDate) {
        super(activity);
        this.course = course;
        emptyVisible.set(false);
        rowVisible.set(false);
        layoutManager = new LinearLayoutManager(mActivity);
        take =24;
        skip = 0;
        filters = new ArrayList<>();
        this.seletedDate = selectedDate;

        periods = new ArrayList<>();
        periodAdapter = new PeriodAdapter(mActivity);
        periodAdapter.setItems(periods);
        getPeriods();

        periodAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()) {
                default:
                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_PERIOD_NAME, item.getTitle());
                    parameters.putLong(Constants.KEY_PERIOD_ID, item.getId());
                    parameters.putLong(SELECTED_DATE, selectedDate);
                    parameters.putSerializable(Router.EXTRA_COURSE_DATA, PeriodListingViewModel.this.course);
                    ActivityUtil.gotoPage(mActivity, AddUnitsActivity.class, parameters);
                    mActivity.finish();
            }
        });

        mActivity.showLoading();

    }

    private void getPeriods() {
        mDataManager.getPeriods(filters, mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), mDataManager.getLoginPrefs().getRole()
                , take, skip,0, new OnResponseCallback<List<Period>>() {
                    @Override
                    public void onSuccess(List<Period> data) {
//                        periodAdapter.setItems(data);
                        List<Period> periods = new ArrayList<>();
                        periodAdapter.setLoadingDone();
                        for (Period period : data){
                            if (seletedDate >= period.getStartDate()
                            && seletedDate <= period.getEndDate()){
                                periods.add(period);
                                periodAdapter.setItems(periods);
                            }
                        }
                        mActivity.hideLoading();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        periodAdapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });
    }

    private void toggleEmptyVisibility() {
        if (periods == null || periods.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public class PeriodAdapter extends MxInfiniteAdapter<Period> {

        public PeriodAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Period model,
                           @Nullable OnRecyclerItemClickListener<Period> listener) {
            if (binding instanceof RowPeriodListingBinding) {
                RowPeriodListingBinding scheduleBinding = (RowPeriodListingBinding) binding;
                scheduleBinding.setPeriod(model);

                scheduleBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }

    }
}
