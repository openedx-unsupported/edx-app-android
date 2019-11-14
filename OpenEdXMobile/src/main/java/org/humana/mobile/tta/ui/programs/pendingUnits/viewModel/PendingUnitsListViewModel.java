package org.humana.mobile.tta.ui.programs.pendingUnits.viewModel;

import android.app.Dialog;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowPendingUnitsBinding;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
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
    public float rating = 0;

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

    public void approveUnits(String unitId, String remarks, int rating) {

        mDataManager.approveUnit(unitId,
                userName.get(), remarks, rating, new OnResponseCallback<SuccessResponse>() {
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

    public void rejectUnits(String unitId, String remarks, int rating) {
        mDataManager.rejectUnit(unitId,
                userName.get(), remarks, rating, new OnResponseCallback<SuccessResponse>() {
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
                itemBinding.textUnitName.setText(model.getCode() + "  |  " + model.getType() + " | "
                        + model.getUnitHour() + " hrs");
                itemBinding.textSubmissionDate.setVisibility(View.GONE);
                if (!DateUtil.getDisplayDate(model.getMyDate()).equals("01 Jan 1970")) {
                    itemBinding.textDate.setText("Submitted On : " + DateUtil.getDisplayDate(model.getMyDate()));
                }if(!DateUtil.getDisplayDate(model.getStaffDate()).equals("01 Jan 1970")) {
                    itemBinding.textProposedDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                }
                itemBinding.textDesc.setText(model.getDesc());
                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())){
                    itemBinding.llApproval.setVisibility(View.GONE);
                }else {
                    itemBinding.llApproval.setVisibility(View.VISIBLE);
                }
                itemBinding.btnApprove.setOnClickListener(v -> {
                        approveReturn(model.getUnit_id());
                });

                itemBinding.btnReject.setOnClickListener(v -> {
                    approveReturn(model.getUnit_id());
                });


            }
        }
    }

    public void approveReturn(String unitId){
        final Dialog dialog = new Dialog(mActivity);
        dialog.setContentView(R.layout.dialog_approve_return_unit);
        Button btnApprove = (Button) dialog.findViewById(R.id.btn_approve);
        Button btnReturn = (Button) dialog.findViewById(R.id.btn_return);
        EditText etRemarks = dialog.findViewById(R.id.et_remarks);
        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
//        EditText dialogText =  dialog.findViewById(R.id.et_period_name);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);



        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar rb, float rating, boolean fromUser) {
                rating = rb.getRating();
                if (rating==1){
                    Toast.makeText(mActivity, "poor", Toast.LENGTH_SHORT).show();
                }else if (rating==2){
                    Toast.makeText(mActivity, "fair", Toast.LENGTH_SHORT).show();
                }else if (rating==3){
                    Toast.makeText(mActivity, "good", Toast.LENGTH_SHORT).show();
                }else if (rating==4){
                    Toast.makeText(mActivity, "very good", Toast.LENGTH_SHORT).show();
                }else if (rating==5){
                    Toast.makeText(mActivity, "excellent", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // if button is clicked, close the custom dialog

        btnApprove.setOnClickListener(v -> {
            String remarks = etRemarks.getText().toString();
            dialog.dismiss();
            approveUnits(unitId, remarks, (int) rating);
        });

        btnReturn.setOnClickListener(v -> {
            String remarks = etRemarks.getText().toString();
            dialog.dismiss();
            rejectUnits(unitId,remarks, (int) rating);
        });
        dialog.setCancelable(true);
        dialog.show();
    }
}
