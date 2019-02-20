package org.edx.mobile.tta.ui.connect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import org.edx.mobile.R;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.connect.view_model.ConnectDashboardViewModel;

public class ConnectDashboardActivity extends BaseVMActivity {

    private ConnectDashboardViewModel viewModel;

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ConnectDashboardViewModel(this, getIntent().getExtras().getParcelable(Constants.KEY_CONTENT));
        binding(R.layout.t_activity_connect_dashboard, viewModel);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager pager = findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(pager);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }
}
