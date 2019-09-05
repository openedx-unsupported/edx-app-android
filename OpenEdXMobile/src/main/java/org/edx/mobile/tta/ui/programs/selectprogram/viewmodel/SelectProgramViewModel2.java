package org.edx.mobile.tta.ui.programs.selectprogram.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowSelectProgSectionBinding;
import org.edx.mobile.tta.data.local.db.table.Program;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.programs.selectSection.SelectSectionActivity;
import org.edx.mobile.tta.utils.ActivityUtil;


import java.util.ArrayList;
import java.util.List;

public class SelectProgramViewModel2 extends BaseViewModel {
    public List<Program> programs;
    public List<Program> selectPrograms;
    private String programId;
    private static View itemView;
    public ProgramsAdapter programsAdapter;
    public RecyclerView.LayoutManager layoutManager;
    public Boolean prevVisible = false;
    public ObservableBoolean isPrev = new ObservableBoolean();



    public SelectProgramViewModel2(BaseVMActivity activity) {
        super(activity);

        mActivity.showLoading();
        programId = mDataManager.getLoginPrefs().getProgramId();
        programs = new ArrayList<>();
        layoutManager = new LinearLayoutManager(mActivity);
        programsAdapter = new ProgramsAdapter(mActivity);
        programsAdapter.setItems(programs);

        fetchPrograms();

        programsAdapter.setItemClickListener((view, item) -> {
            if (selectPrograms.size() > 0) {
                if (view == itemView) {
                    selectPrograms.clear();
                    itemView.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.white));
                } else {
                    selectPrograms.clear();
                    selectPrograms.add(item);
                    itemView.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.white));
                    view.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_blue_light));
                    itemView = view;
                    programId = item.getId();
                }
            } else {
                selectPrograms.add(item);
                itemView = view;
                programId = item.getId();
                view.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_blue_light));
            }

        });
    }

    public void fetchPrograms() {
        mActivity.showLoading();
        mDataManager.getPrograms(new OnResponseCallback<List<Program>>() {
            @Override
            public void onSuccess(List<Program> data) {
                mActivity.hideLoading();
                populatePrograms(data);
                programsAdapter.setLoadingDone();

            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();

//        fetchPrograms();
    }

    private void populatePrograms(List<Program> data) {
        boolean newItemsAdded = false;
        int n = 0;

        if (data.size()==1){
            programId = data.get(0).getId();
            prevVisible = true;
            mDataManager.getLoginPrefs().setProgramTitle(data.get(0).getTitle());
            Bundle b = new Bundle();
            b.putCharSequence("program", programId);
            b.putBoolean("prevVisible",prevVisible);
            ActivityUtil.gotoPage(mActivity, SelectSectionActivity.class, b);
            mDataManager.getLoginPrefs().setProgramId(programId);
            mActivity.finish();
        }else {
            for (Program user : data) {
                if (!programs.contains(user)) {
                    programs.add(user);
                    newItemsAdded = true;
                    n++;
                }
            }


            if (newItemsAdded) {
                programsAdapter.notifyItemRangeInserted(programs.size() - n, n);
            }
        }

//        toggleEmptyVisibility();
    }

//    private void toggleEmptyVisibility() {
//        if (programs == null || programs.isEmpty()) {
//            emptyVisible.set(true);
//        } else {
//            emptyVisible.set(false);
//        }
//    }


    public class ProgramsAdapter extends MxInfiniteAdapter<Program> {

        public ProgramsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Program model,
                           @Nullable OnRecyclerItemClickListener<Program> listener) {
            if (binding instanceof TRowSelectProgSectionBinding) {
                TRowSelectProgSectionBinding itemBinding = (TRowSelectProgSectionBinding) binding;
                itemBinding.textPrograms.setText(model.getTitle());

                itemBinding.llProg.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                if (isPrev.get()) {
                    if (model.getId().equals(mDataManager.getLoginPrefs().getProgramId())) {
                        itemView = itemBinding.llProg;
                        itemBinding.llProg.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_blue_light));
                    }
                }


                itemBinding.llProg.setOnClickListener(v -> {
                    programId = model.getId();
//                    itemBinding.llProg.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.white));
                    mDataManager.getLoginPrefs().setProgramId(programId);
                    mDataManager.getLoginPrefs().setProgramTitle(model.getTitle());
//                    itemBinding.llProg.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_blue_light));
                    Bundle b = new Bundle();
                    b.putCharSequence("program", programId);
                    b.putBoolean("prevVisible",prevVisible);
                    ActivityUtil.gotoPage(mActivity, SelectSectionActivity.class, b);
                    mActivity.finish();
                });

            }
        }
    }
}
