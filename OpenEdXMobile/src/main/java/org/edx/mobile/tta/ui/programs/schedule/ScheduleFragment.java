package org.edx.mobile.tta.ui.programs.schedule;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.library.LibraryFragment;
import org.edx.mobile.tta.ui.programs.schedule.view_model.ScheduleViewModel;
import org.edx.mobile.view.Router;

public class ScheduleFragment extends TaBaseFragment {
    public ScheduleViewModel viewModel;

    private EnrolledCoursesResponse course;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            getDataFromBundle(savedInstanceState);
        }
        if (course == null && getArguments() != null){
            getDataFromBundle(getArguments());
        }

        viewModel = new ScheduleViewModel(getActivity(), this, course);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View rootView = binding(inflater, container, R.layout.t_fragment_schedules, viewModel).getRoot();
        ViewDataBinding binding = binding(inflater, container, R.layout.t_fragment_schedules, viewModel);
//        viewModel = new ScheduleViewModel(getActivity(), this, binding);

        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (course != null){
            outState.putSerializable(Router.EXTRA_COURSE_DATA, course);
        }
    }

    private void getDataFromBundle(Bundle bundle){
        if (bundle.containsKey(Router.EXTRA_COURSE_DATA)){
            course = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_COURSE_DATA);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
