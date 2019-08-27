package org.edx.mobile.tta.ui.programs.selectSection;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.programs.selectSection.viewModel.SelectSectionViewModel;

public class SelectSectionActivity extends BaseVMActivity {
    SelectSectionViewModel viewModel;
    String programId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new SelectSectionViewModel(this);
//        Intent in = getIntent();
        assert savedInstanceState != null;
        savedInstanceState = getIntent().getExtras();
        programId = savedInstanceState.getString("program");
        viewModel.programId.set(programId);
        binding(R.layout.t_activity_select_section, viewModel);
    }
}
