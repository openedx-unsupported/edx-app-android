package org.edx.mobile.tta.ui.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.profile.view_model.ChangePasswordViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;

public class ChangePasswordFragment extends TaBaseFragment {
    public static final String TAG = ChangePasswordFragment.class.getCanonicalName();
    private static final int RANK = 3;

    private ChangePasswordViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ChangePasswordViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_change_password, viewModel).getRoot();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            getActivity().onBackPressed();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.change_password.name()));
    }
}
