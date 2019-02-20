package org.edx.mobile.tta.ui.connect;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.connect.view_model.ConnectCommentsTabViewModel;

public class ConnectCommentsTab extends TaBaseFragment {

    private ConnectCommentsTabViewModel viewModel;

    private Content content;

    public static ConnectCommentsTab newInstance(Content content){
        ConnectCommentsTab tab = new ConnectCommentsTab();
        tab.content = content;
        return tab;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ConnectCommentsTabViewModel(getActivity(), this, content);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_comments_tab, viewModel)
                .getRoot();

        return view;
    }
}
