package org.humana.mobile.tta.ui.programs.units.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
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


    public PeriodListingViewModel(BaseVMActivity activity, EnrolledCoursesResponse course) {
        super(activity);
        this.course = course;
        emptyVisible.set(false);
        layoutManager = new LinearLayoutManager(mActivity);
        take =10;
        skip = 0;
        filters = new ArrayList<>();

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
                    parameters.putSerializable(Router.EXTRA_COURSE_DATA, PeriodListingViewModel.this.course);
                    ActivityUtil.gotoPage(mActivity, AddUnitsActivity.class, parameters);
                    mActivity.finish();
            }
        });

    }

    private void getPeriods() {
        mDataManager.getPeriods(filters, mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), mDataManager.getLoginPrefs().getRole()
                , take, skip, new OnResponseCallback<List<Period>>() {
                    @Override
                    public void onSuccess(List<Period> data) {
                        mActivity.hideLoading();
//                        populatePeriods(data);
                        periodAdapter.setItems(data);
                        periodAdapter.setLoadingDone();
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
    private void populatePeriods(List<Period> data) {
        boolean newItemsAdded = false;
        List<Period> periods = new ArrayList<>();
        int n = 0;
        for (Period period : data) {
            if (!periods.contains(period)) {
                periods.add(period);
                newItemsAdded = true;
                n++;
            }
        }
        if (newItemsAdded) {
            periodAdapter.notifyItemRangeInserted(periods.size() - n, n);
        }

        toggleEmptyVisibility();
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