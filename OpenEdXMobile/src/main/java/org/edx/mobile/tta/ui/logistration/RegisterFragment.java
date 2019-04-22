package org.edx.mobile.tta.ui.logistration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.logistration.view_model.RegisterViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;

public class RegisterFragment extends TaBaseFragment {
    private static final int RANK = 1;

    private RegisterViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new RegisterViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding(inflater, container, R.layout.t_fragment_register, viewModel)
                .getRoot();
    }

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        viewModel.generateOTP();
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.signup.name()));
    }
}
