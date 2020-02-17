package org.humana.mobile.tta.ui.programs.pendingUnits;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.humana.mobile.R;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.library.LibraryFragment;
import org.humana.mobile.tta.ui.programs.pendingUnits.viewModel.PendingUsersViewModel;

public class PendingUsersFragment extends TaBaseFragment{
    public static final String TAG = LibraryFragment.class.getCanonicalName();

    private PendingUsersViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new PendingUsersViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = binding(inflater, container, R.layout.t_fragment_pending_users, viewModel).getRoot();
        return rootView;
    }
    @Override
    public void onPageShow() {
        super.onPageShow();
        viewModel.isSeen.set(true);
        viewModel.isSeen.notifyChange();
        viewModel.fetchData();
    }
}
