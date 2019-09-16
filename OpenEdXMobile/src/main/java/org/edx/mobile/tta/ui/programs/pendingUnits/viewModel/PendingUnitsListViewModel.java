package org.edx.mobile.tta.ui.programs.pendingUnits.viewModel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowPendingUnitsBinding;
import org.edx.mobile.databinding.TRowPendingUserGridBinding;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.tta.data.model.SuccessResponse;
import org.edx.mobile.tta.data.model.program.ProgramUser;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class PendingUnitsListViewModel extends BaseViewModel {

    private static final int TAKE = 0;
    private static final int SKIP = 10;

    private boolean allLoaded;
    private boolean changesMade;
    private int take, skip;
    public String userName;
    private List<CourseComponent> unitsList;

    public RecyclerView.LayoutManager layoutManager;

    public UnitsAdapter unitsAdapter;
    public ObservableBoolean emptyVisible = new ObservableBoolean();

    public PendingUnitsListViewModel(BaseVMActivity activity) {
        super(activity);

        layoutManager = new LinearLayoutManager(mActivity);
        unitsAdapter = new UnitsAdapter(mActivity);
        unitsList = new ArrayList<>();
        take = TAKE;

        skip = SKIP;

        mActivity.showLoading();


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
        }
        fetchUnits();
    }

    public void fetchUnits() {
        mDataManager.getPendingUnits(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(),
                userName,
                take, skip, new OnResponseCallback<List<CourseComponent>>() {
                    @Override
                    public void onSuccess(List<CourseComponent> data) {
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
                userName, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        mActivity.hideLoading();
                        fetchUnits();
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
                userName, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        mActivity.hideLoading();
                        fetchUnits();
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

    private void populateUnits(List<CourseComponent> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (CourseComponent unit : data) {
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


    public class UnitsAdapter extends MxInfiniteAdapter<CourseComponent> {

        public UnitsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull CourseComponent model,
                           @Nullable OnRecyclerItemClickListener<CourseComponent> listener) {
            if (binding instanceof TRowPendingUnitsBinding) {
                TRowPendingUnitsBinding itemBinding = (TRowPendingUnitsBinding) binding;
                itemBinding.textUnitName.setText(model.getDisplayName());
                if (mDataManager.getLoginPrefs().getRole().equals("student")){
                    itemBinding.llApproval.setVisibility(View.GONE);
                }else {
                    itemBinding.llApproval.setVisibility(View.VISIBLE);
                }
                itemBinding.btnApprove.setOnClickListener(v -> {
                        approveUnits(model.getId());
                });

                itemBinding.btnReject.setOnClickListener(v -> {
                    rejectUnits(model.getId());
                });


            }
        }
    }
}
