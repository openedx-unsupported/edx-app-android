package org.humana.mobile.tta.ui.programs.units;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.humana.mobile.R;
import org.humana.mobile.databinding.FragCalendarBottomSheetBinding;
import org.humana.mobile.databinding.TFragmentUnitsBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.ui.base.TaBaseBottomsheetFragment;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.library.LibraryFragment;
import org.humana.mobile.tta.ui.programs.units.view_model.CalendarBottomSheetViewModel;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitCalendarViewModel;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitsViewModel;
import org.humana.mobile.view.Router;

import java.util.ArrayList;
import java.util.List;

public class ActivityCalendarBottomSheet extends TaBaseBottomsheetFragment {

    private CalendarBottomSheetViewModel viewModel;

    private long periodId;
    private String periodName;
    private EnrolledCoursesResponse course;
    private BottomSheetBehavior behavior;
    private Long selectedDate, startDateTime, endDateTime;
    FragCalendarBottomSheetBinding binding;
    private int sheetState;

    @SuppressLint("ValidFragment")
    public ActivityCalendarBottomSheet(Long selectedDate, Long startDateTime, Long endDateTime, Long periodId, String periodName) {
        this.selectedDate = selectedDate;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.periodId = periodId;
        this.periodName = periodName;
    }

    public ActivityCalendarBottomSheet() {
    }

    public static ActivityCalendarBottomSheet newInstance() {
        return new ActivityCalendarBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            getDataFromBundle(savedInstanceState);
        }
        if (course == null && getArguments() != null){
            getDataFromBundle(getArguments());
        }
        savedInstanceState = getActivity().getIntent().getExtras();
//        selectedDate = savedInstanceState.getLong("selectedDate", 0L);
        viewModel = new CalendarBottomSheetViewModel(getActivity(),this, course,selectedDate,
                startDateTime, endDateTime, periodId, periodName);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = binding(inflater, container, R.layout.frag_calendar_bottom_sheet, viewModel).getRoot();
        binding = (FragCalendarBottomSheetBinding) binding(inflater, container, R.layout.frag_calendar_bottom_sheet, viewModel);
        sheetState = 0;


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
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = (FrameLayout) d.findViewById(android.support.design.R.id.design_bottom_sheet);
                BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
                behaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN)
                        {
                            sheetState = newState;
                            dismiss();
                        }


                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
            }
        });


        return d;
    }
}
