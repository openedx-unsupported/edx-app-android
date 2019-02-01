package org.edx.mobile.tta.ui.course;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.course.view_model.CourseMaterialViewModel;

public class CourseMaterialTab extends TaBaseFragment {

    private CourseMaterialViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new CourseMaterialViewModel(getActivity(), this);
        View view = binding(inflater, container, R.layout.t_fragment_course_material_tab, viewModel).getRoot();

        return view;
    }
}
