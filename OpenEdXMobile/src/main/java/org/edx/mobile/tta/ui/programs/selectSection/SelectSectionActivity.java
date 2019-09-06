package org.edx.mobile.tta.ui.programs.selectSection;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.programs.selectSection.viewModel.SelectSectionViewModel;

public class SelectSectionActivity extends BaseVMActivity {
    SelectSectionViewModel viewModel;
    String programId, progName;
    Boolean prevVisible;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new SelectSectionViewModel(this);
//        Intent in = getIntent();
        /*assert savedInstanceState != null;
        savedInstanceState = getIntent().getExtras();
        programId = savedInstanceState.getString("program");
        prevVisible = savedInstanceState.getBoolean("prevVisible");
        progName = savedInstanceState.getString("progName");
        viewModel.fabPrevVisibility.set(prevVisible);
        viewModel.programId.set(programId);*/
        binding(R.layout.t_activity_select_section, viewModel);
    }
}
