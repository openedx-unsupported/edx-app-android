package org.edx.mobile.view;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import android.widget.ImageView;
import android.widget.TextView;


public interface ToolbarCallbacks {
    @Nullable
    SearchView getSearchView();

    @Nullable
    TextView getTitleView();

    @Nullable
    ImageView getProfileView();
}
