package org.edx.mobile.tta.ui.course;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.course.view_model.CourseDashboardViewModel;

public class CourseDashboardFragment extends TaBaseFragment {
    public static final String TAG = CourseDashboardFragment.class.getCanonicalName();

    private Content course;
    private CourseDashboardViewModel viewModel;

    public static CourseDashboardFragment newInstance(Content course){
        CourseDashboardFragment fragment = new CourseDashboardFragment();
        fragment.course = course;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new CourseDashboardViewModel(getActivity(), this, course);
        View view = binding(inflater, container, R.layout.t_fragment_course_dashboard, viewModel)
                .getRoot();

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getCourseData();
    }
}
