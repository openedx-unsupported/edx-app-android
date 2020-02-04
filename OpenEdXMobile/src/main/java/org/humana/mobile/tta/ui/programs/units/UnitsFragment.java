package org.humana.mobile.tta.ui.programs.units;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.lib.mxcalendar.models.Event;
import com.lib.mxcalendar.util.Builder;
import com.lib.mxcalendar.view.IMxCalenderListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TFragmentUnitsBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.library.LibraryFragment;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitsViewModel;
import org.humana.mobile.view.Router;
import org.humana.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UnitsFragment extends TaBaseFragment implements PageViewStateCallback{
    public static final String TAG = LibraryFragment.class.getCanonicalName();

    private UnitsViewModel viewModel;

    private EnrolledCoursesResponse course;
    private List<Event> eventList;
    private long periodId;
    private String periodName;
    TFragmentUnitsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            getDataFromBundle(savedInstanceState);
        }
        if (course == null && getArguments() != null) {
            getDataFromBundle(getArguments());
        }

        viewModel = new UnitsViewModel(getActivity(), this, course, periodName, periodId);
        eventList = new ArrayList<>();
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = binding(inflater, container, R.layout.t_fragment_units, viewModel).getRoot();
        binding = (TFragmentUnitsBinding) binding(inflater, container, R.layout.t_fragment_units, viewModel);

        binding.calendarView.init(
                new Builder()
                        .setDayNameColor(null)
                        .setHeaderColor(null)
                        .setDayNumberColor(null)
                        .setListner(viewModel)
                        .setTabletMode(isTabView()));

//        binding.switchUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (buttonView.isChecked()) {
//                viewModel.calVisible.set(true);
//                viewModel.frameVisible.set(false);
//            } else {
//                viewModel.calVisible.set(false);
//                viewModel.frameVisible.set(true);
//            }
//        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (course != null) {
            outState.putSerializable(Router.EXTRA_COURSE_DATA, course);
        }
        if (periodName != null) {
            outState.putString(Constants.KEY_PERIOD_NAME, periodName);
        }
    }

    private void getDataFromBundle(Bundle bundle) {
        if (bundle.containsKey(Router.EXTRA_COURSE_DATA)) {
            course = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_COURSE_DATA);
        }

        if (bundle.containsKey(Constants.KEY_PERIOD_ID)) {
            periodId = bundle.getLong(Constants.KEY_PERIOD_ID);
        }
        if (bundle.containsKey(Constants.KEY_PERIOD_NAME)) {
            periodName = bundle.getString(Constants.KEY_PERIOD_NAME);
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

    private Boolean isTabView() {
        return getResources().getBoolean(R.bool.isTablet);
    }


    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }




}
