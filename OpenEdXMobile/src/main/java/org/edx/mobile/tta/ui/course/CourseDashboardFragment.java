package org.edx.mobile.tta.ui.course;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

    private Toolbar toolbar;

    public static CourseDashboardFragment newInstance(Content course){
        CourseDashboardFragment fragment = new CourseDashboardFragment();
        fragment.course = course;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new CourseDashboardViewModel(getActivity(), this, course);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_course_dashboard, viewModel)
                .getRoot();

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }
}
