package org.edx.mobile.tta.ui.programs.schedule;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.library.LibraryFragment;
import org.edx.mobile.tta.ui.programs.schedule.view_model.ScheduleViewModel;

public class ScheduleFragment extends TaBaseFragment {
    public ScheduleViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ScheduleViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View rootView = binding(inflater, container, R.layout.t_fragment_schedules, viewModel).getRoot();
        ViewDataBinding binding = binding(inflater, container, R.layout.t_fragment_schedules, viewModel);
//        viewModel = new ScheduleViewModel(getActivity(), this, binding);

        return binding.getRoot();
    }
}
