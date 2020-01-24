package org.humana.mobile.tta.ui.programs.units;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TFragmentUnitsBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitsViewModel;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.library.LibraryFragment;
import org.humana.mobile.view.Router;
import org.humana.mobile.view.common.PageViewStateCallback;

public class UnitsFragment extends TaBaseFragment implements PageViewStateCallback {
    public static final String TAG = LibraryFragment.class.getCanonicalName();

    private UnitsViewModel viewModel;

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

        viewModel = new UnitsViewModel(getActivity(), this, course);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = binding(inflater, container, R.layout.t_fragment_units, viewModel).getRoot();
        return rootView;
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

    @Override
    public void onPageShow() {
       viewModel.setSessionFilter();

    }
}
