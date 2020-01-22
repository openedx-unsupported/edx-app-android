package org.humana.mobile.tta.ui.programs.selectprogram;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;

import org.humana.mobile.R;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.programs.selectprogram.viewmodel.ProgramViewModel;
import org.humana.mobile.tta.ui.programs.selectprogram.viewmodel.SelectProgramViewModel2;

public class SelectProgramActivity extends BaseVMActivity {

    private ProgramViewModel viewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ProgramViewModel(this);
        savedInstanceState = new Bundle();
//        if (savedInstanceState != null) {
//            savedInstanceState = getIntent().getExtras();
//            if (savedInstanceState != null) {
//                boolean prev = savedInstanceState.getBoolean("isPrev", false);
//                viewModel.isPrev.set(prev);
//            }
//        }
//

        binding(R.layout.t_activity_program, viewModel);
        BottomNavigationView view = findViewById(R.id.dashboard_bottom_nav);
        view.setItemIconTintList(null);

    }


}
