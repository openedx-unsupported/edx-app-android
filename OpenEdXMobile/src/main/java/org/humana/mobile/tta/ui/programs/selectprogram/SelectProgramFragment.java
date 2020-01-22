package org.humana.mobile.tta.ui.programs.selectprogram;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.humana.mobile.R;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.feed.FeedFragment;
import org.humana.mobile.tta.ui.programs.schedule.view_model.ScheduleViewModel;
import org.humana.mobile.tta.ui.programs.selectprogram.viewmodel.SelectProgramViewModel2;
import org.humana.mobile.view.Router;
import org.humana.mobile.view.common.PageViewStateCallback;

public class SelectProgramFragment extends TaBaseFragment implements PageViewStateCallback {
    public SelectProgramViewModel2 viewModel;
    public static final String TAG = FeedFragment.class.getCanonicalName();
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

        viewModel = new SelectProgramViewModel2(getActivity(), this, course);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewDataBinding binding = binding(inflater, container, R.layout.t_activity_select_program_section, viewModel);

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


}