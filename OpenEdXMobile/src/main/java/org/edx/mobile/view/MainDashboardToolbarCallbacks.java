package org.edx.mobile.view;

import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.widget.ImageView;
import android.widget.TextView;


public interface MainDashboardToolbarCallbacks {
    @Nullable
    SearchView getSearchView();

    @Nullable
    TextView getTitleView();

    @Nullable
    ImageView getProfileView();
}
