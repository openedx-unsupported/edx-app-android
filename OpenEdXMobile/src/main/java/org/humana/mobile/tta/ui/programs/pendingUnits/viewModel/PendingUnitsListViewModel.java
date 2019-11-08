package org.humana.mobile.tta.ui.programs.pendingUnits.viewModel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.databinding.TRowPendingUnitsBinding;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class PendingUnitsListViewModel extends BaseViewModel {

    private static final int TAKE = 10;
    private static final int SKIP = 0;

    private boolean allLoaded;
    private boolean changesMade;
    private int take, skip;
    public ObservableField<String> userName = new ObservableField<>();
    private List<Unit> unitsList;

    public RecyclerView.LayoutManager layoutManager;

    public UnitsAdapter unitsAdapter;
    public ObservableBoolean emptyVisible = new ObservableBoolean();

    public PendingUnitsListViewModel(BaseVMActivity activity) {
        super(activity);

        Bundle bundle = mActivity.getIntent().getExtras();
        assert bundle != null;
        userName.set(bundle.getString("username"));
        layoutManager = new LinearLayoutManager(mActivity);
        unitsAdapter = new UnitsAdapter(mActivity);
        unitsList = new ArrayList<>();
        changesMade = true;
        take = TAKE;

        skip = SKIP;

        mActivity.showLoading();
        unitsAdapter.setItems(unitsList);
        fetchData();



//        unitsAdapter.setItemClickListener((view, item) -> {
//            if(item != null){
//                String unitId = item.getCourseId();
//                approveUnits(unitId);
//            }
//        });
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
            unitsAdapter.reset(true);
            changesMade = false;
        }
        fetchUnits();
    }

    public void fetchUnits() {
        mDataManager.getPendingUnits(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(),
                userName.get(),
                take, skip, new OnResponseCallback<List<Unit>>() {
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

    public void approveUnits(String unitId) {

        mDataManager.approveUnit(unitId,
                userName.get(), new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        mActivity.hideLoading();
                        changesMade = true;
                        fetchData();
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

    public void rejectUnits(String unitId) {
        mDataManager.rejectUnit(unitId,
                userName.get(), new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        mActivity.hideLoading();
                        changesMade = true;
                        fetchData();
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
            if (!unitsList.contains(unit)) {
                unitsList.add(unit);
                newItemsAdded = true;
                n++;
            }
        }


        if (newItemsAdded) {
            unitsAdapter.notifyItemRangeInserted(unitsList.size() - n, n);
        }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (unitsList == null || unitsList.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }


    public class UnitsAdapter extends MxInfiniteAdapter<Unit> {

        public UnitsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Unit model,
                           @Nullable OnRecyclerItemClickListener<Unit> listener) {
            if (binding instanceof TRowPendingUnitsBinding) {
                TRowPendingUnitsBinding itemBinding = (TRowPendingUnitsBinding) binding;
                itemBinding.textUnitName.setText(model.getTitle());
                itemBinding.textSubmissionDate.setText("Submitted On : ");
                itemBinding.textDate.setText(DateUtil.getDisplayDate(model.getMyDate()));
                itemBinding.textProposedDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())){
                    itemBinding.llApproval.setVisibility(View.GONE);
                }else {
                    itemBinding.llApproval.setVisibility(View.VISIBLE);
                }
                itemBinding.btnApprove.setOnClickListener(v -> {
                        approveUnits(model.getUnit_id());
                });

                itemBinding.btnReject.setOnClickListener(v -> {
                    rejectUnits(model.getUnit_id());
                });


            }
        }
    }
}
