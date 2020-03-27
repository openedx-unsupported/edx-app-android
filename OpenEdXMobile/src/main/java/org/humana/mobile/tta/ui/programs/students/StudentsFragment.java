package org.humana.mobile.tta.ui.programs.students;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.humana.mobile.R;
import org.humana.mobile.tta.ui.programs.students.view_model.StudentsViewModel;
import org.humana.mobile.tta.ui.base.TaBaseFragment;

public class StudentsFragment extends TaBaseFragment {
    private StudentsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new StudentsViewModel(getActivity(), this);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = binding(inflater, container, R.layout.t_fragment_students, viewModel).getRoot();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        viewModel.isSeen.set(true);
        viewModel.isSeen.notifyChange();
        viewModel.changesMade = true;
        viewModel.fetchData();
    }
}
