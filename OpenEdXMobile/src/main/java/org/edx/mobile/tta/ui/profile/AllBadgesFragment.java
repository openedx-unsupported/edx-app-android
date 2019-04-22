package org.edx.mobile.tta.ui.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.ui.base.TaBaseFragment;

import org.edx.mobile.tta.ui.profile.view_model.AllBadgesViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;


public class AllBadgesFragment extends TaBaseFragment {
    public static final String TAG = AllBadgesFragment.class.getCanonicalName();
    private static final int RANK = 3;

    private AllBadgesViewModel allBadgesViewModel;
    private Toolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allBadgesViewModel = new AllBadgesViewModel(getActivity(), this);
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.badges.name()));

        analytic.addMxAnalytics_db(null, Action.ViewBadges, Nav.profile.name(), Source.Mobile, null);
    }

    public static AllBadgesFragment newInstance() {
        AllBadgesFragment fragment = new AllBadgesFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_allbadges, allBadgesViewModel)
                .getRoot();
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.badges.name()));
    }

}
