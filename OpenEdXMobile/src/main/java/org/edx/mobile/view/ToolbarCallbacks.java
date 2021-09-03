package org.edx.mobile.view;

import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

public interface ToolbarCallbacks {
    @Nullable
    SearchView getSearchView();

    @Nullable
    TextView getTitleView();
}
