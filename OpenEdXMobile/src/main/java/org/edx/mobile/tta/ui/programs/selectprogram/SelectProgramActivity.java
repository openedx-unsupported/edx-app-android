package org.edx.mobile.tta.ui.programs.selectprogram;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.programs.selectprogram.viewmodel.SelectProgramViewModel;

public class SelectProgramActivity extends BaseVMActivity {

    private SelectProgramViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new SelectProgramViewModel(this);
        binding(R.layout.t_activity_select_program, viewModel);
    }
}
