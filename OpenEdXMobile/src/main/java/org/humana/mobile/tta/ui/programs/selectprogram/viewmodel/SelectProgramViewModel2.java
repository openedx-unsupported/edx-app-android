package org.humana.mobile.tta.ui.programs.selectprogram.viewmodel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowSelectProgSectionBinding;
import org.humana.mobile.tta.data.constants.Constants;
import org.humana.mobile.tta.data.local.db.table.Program;
import org.humana.mobile.tta.data.local.db.table.Section;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.landing.LandingActivity;
import org.humana.mobile.tta.ui.programs.selectSection.SelectSectionActivity;
import org.humana.mobile.tta.utils.ActivityUtil;


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
    public ObservableBoolean emptyVisible = new ObservableBoolean();



    public SelectProgramViewModel2(BaseVMActivity activity) {
        super(activity);

        mActivity.showLoading();
        programId = mDataManager.getLoginPrefs().getProgramId();
        programs = new ArrayList<>();
        layoutManager = new LinearLayoutManager(mActivity);
        programsAdapter = new ProgramsAdapter(mActivity);
        programsAdapter.setItems(programs);
        selectPrograms = new ArrayList<>();

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
                    mDataManager.getLoginPrefs().setProgramId(programId);
                    mDataManager.getLoginPrefs().setParentId(item.getParent_id());
                    mDataManager.getLoginPrefs().setProgramTitle(item.getTitle());
                    mDataManager.getSections(mDataManager.getLoginPrefs().getProgramId(),
                            new OnResponseCallback<List<Section>>() {
                        @Override
                        public void onSuccess(List<Section> data) {

                            if (data.size() == 1) {
                                mDataManager.getLoginPrefs().setSectionId(data.get(0).getId());
                                mDataManager.getLoginPrefs().setRole(data.get(0).getRole());

                                Constants.isSingleRow = true;
                                ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                                mActivity.finish();

                            } else {
                                Bundle b = new Bundle();
                                b.putCharSequence("program", programId);
                                b.putBoolean("prevVisible",prevVisible);
                                Constants.isSinglePrg = false;
                                ActivityUtil.gotoPage(mActivity, SelectSectionActivity.class, b);
                                mActivity.finish();

                            }

                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });

                }
            } else {
                selectPrograms.add(item);
                itemView = view;
                programId = item.getId();
                view.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_blue_light));
                mDataManager.getLoginPrefs().setProgramId(programId);
                mDataManager.getLoginPrefs().setProgramTitle(item.getTitle());
                mDataManager.getLoginPrefs().setParentId(item.getParent_id());
                mDataManager.getSections(mDataManager.getLoginPrefs().getProgramId(),
                        new OnResponseCallback<List<Section>>() {
                            @Override
                            public void onSuccess(List<Section> data) {

                                if (data.size() == 1) {
                                    mDataManager.getLoginPrefs().setSectionId(data.get(0).getId());
                                    mDataManager.getLoginPrefs().setRole(data.get(0).getRole());

                                    Constants.isSingleRow = true;
                                    ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                                    mActivity.finish();

                                } else {
                                    Bundle b = new Bundle();
                                    b.putCharSequence("program", programId);
                                    b.putBoolean("prevVisible",prevVisible);
                                    Constants.isSinglePrg = false;
                                    ActivityUtil.gotoPage(mActivity, SelectSectionActivity.class, b);
                                    mActivity.finish();

                                }

                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });

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
                toggleEmptyVisibility();
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private void populatePrograms(List<Program> data) {
        boolean newItemsAdded = false;
        int n = 0;

        if (data.size() == 1){
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

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (programs == null || programs.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }


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


                Glide.with(mActivity)
                        .load(model.getImage())
                        .placeholder(ContextCompat.getDrawable(mActivity, R.drawable.circle_all_blue))
                        .into(itemBinding.image);

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

            }
        }
    }
}
