package org.edx.mobile.tta.ui.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.feed.view_model.FeedViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;

public class FeedFragment extends TaBaseFragment {
    public static final String TAG = FeedFragment.class.getCanonicalName();
    private static final int RANK = 2;

    private FeedViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new FeedViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding(inflater, container, R.layout.t_fragment_feed, viewModel)
                .getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.feed.name()));
    }
}
