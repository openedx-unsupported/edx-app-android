package org.humana.mobile.tta.ui.programs.pendingUnits.viewModel;

import android.app.Dialog;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowPendingUnitsBinding;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.PeriodSavedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class PendingUnitsListViewModel extends BaseViewModel {

    private static final int TAKE = 10;
    private static final int SKIP = 0;

    private boolean allLoaded;
    private boolean changesMade;
    private int take, skip;
    public ObservableField<String> userName = new ObservableField<>();
    private List<Unit> unitsList;

    public RecyclerView.LayoutManager layoutManager;

    public UnitsAdapter unitsAdapter;
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public float rating = 0;

    private EnrolledCoursesResponse course;
    private EnrolledCoursesResponse parentCourse;
    private String blockid;


    public PendingUnitsListViewModel(BaseVMActivity activity, EnrolledCoursesResponse course) {
        super(activity);
        this.course = course;

        Bundle bundle = mActivity.getIntent().getExtras();
        assert bundle != null;
        userName.set(bundle.getString("username"));
        layoutManager = new LinearLayoutManager(mActivity);
        unitsAdapter = new UnitsAdapter(mActivity);
        unitsList = new ArrayList<>();
        changesMade = true;
        take = TAKE;

        skip = SKIP;

        mActivity.showLoading();
        unitsAdapter.setItems(unitsList);
//        Constants.UNIT_ID = "";
//        Constants.USERNAME = "";
        fetchData();



        unitsAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()) {
                case R.id.tv_my_date:
                    showDatePicker(item);
                    break;
                default:
                    mActivity.showLoading();
                    boolean ssp = unitsList.contains(item);
                    EnrolledCoursesResponse c;
                    if (ssp) {
                        c = course;
                    } else {
                        c = parentCourse;
                    }

                    if (c == null) {

                        String courseId;
                        if (ssp) {
                            courseId = mDataManager.getLoginPrefs().getProgramId();
                        } else {
                            courseId = mDataManager.getLoginPrefs().getParentId();
                        }
                        mDataManager.enrolInCourse(courseId, new OnResponseCallback<ResponseBody>() {
                            @Override
                            public void onSuccess(ResponseBody responseBody) {

                                mDataManager.getenrolledCourseByOrg("Humana", new OnResponseCallback<List<EnrolledCoursesResponse>>() {
                                    @Override
                                    public void onSuccess(List<EnrolledCoursesResponse> data) {
                                        if (courseId != null) {
                                            for (EnrolledCoursesResponse response : data) {
                                                if (response.getCourse().getId().trim().toLowerCase()
                                                        .equals(courseId.trim().toLowerCase())) {
                                                    if (ssp) {
                                                        PendingUnitsListViewModel.this.course = response;
                                                        EventBus.getDefault().post(new CourseEnrolledEvent(response));
                                                    } else {
                                                        PendingUnitsListViewModel.this.parentCourse = response;
                                                    }
                                                    getBlockComponent(item);
                                                    break;
                                                }
                                            }
                                            mActivity.hideLoading();
                                        } else {
                                            mActivity.hideLoading();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        mActivity.hideLoading();
                                        mActivity.showLongSnack("enroll org failure");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                mActivity.hideLoading();
                                mActivity.showLongSnack("enroll failure");
                            }
                        });

                    } else {
                        getBlockComponent(item);
                    }

            }
        });
    }

    private void enrollCourse(Unit item){
        boolean ssp = unitsList.contains(item);
        EnrolledCoursesResponse c;
        if (ssp) {
            c = course;
        } else {
            c = parentCourse;
        }

        if (c == null) {

            String courseId;
            if (ssp) {
                courseId = mDataManager.getLoginPrefs().getProgramId();
            } else {
                courseId = mDataManager.getLoginPrefs().getParentId();
            }
            mDataManager.enrolInCourse(courseId, new OnResponseCallback<ResponseBody>() {
                @Override
                public void onSuccess(ResponseBody responseBody) {

                    mDataManager.getenrolledCourseByOrg("Humana", new OnResponseCallback<List<EnrolledCoursesResponse>>() {
                        @Override
                        public void onSuccess(List<EnrolledCoursesResponse> data) {
                            if (courseId != null) {
                                for (EnrolledCoursesResponse response : data) {
                                    if (response.getCourse().getId().trim().toLowerCase()
                                            .equals(courseId.trim().toLowerCase())) {
                                        if (ssp) {
                                            PendingUnitsListViewModel.this.course = response;
                                            EventBus.getDefault().post(new CourseEnrolledEvent(response));
                                        } else {
                                            PendingUnitsListViewModel.this.parentCourse = response;
                                        }
                                        getBlockComponent(item);
                                        break;
                                    }
                                }
                                mActivity.hideLoading();
                            } else {
                                mActivity.hideLoading();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            mActivity.hideLoading();
                            mActivity.showLongSnack("enroll org failure");
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    mActivity.hideLoading();
                    mActivity.showLongSnack("enroll failure");
                }
            });

        } else {
            getBlockComponent(item);
        }
    }

    private void showDatePicker(Unit unit) {
        DateUtil.showDatePicker(mActivity, unit.getMyDate(), new OnResponseCallback<Long>() {
            @Override
            public void onSuccess(Long data) {
                mActivity.showLoading();
                mDataManager.setProposedDate(mDataManager.getLoginPrefs().getProgramId(),
                        mDataManager.getLoginPrefs().getSectionId(), data, unit.getPeriodId(), unit.getId(),
                        new OnResponseCallback<SuccessResponse>() {
                            @Override
                            public void onSuccess(SuccessResponse response) {
                                mActivity.hideLoading();
                                unit.setMyDate(data);
                                unitsAdapter.notifyItemChanged(unitsAdapter.getItemPosition(unit));
                                if (response.getSuccess()) {
                                    mActivity.showLongSnack("Proposed date set successfully");
                                    EventBus.getDefault().post(unitsList);
                                }

                            }

                            @Override
                            public void onFailure(Exception e) {
                                mActivity.hideLoading();
                                mActivity.showLongSnack(e.getLocalizedMessage());
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    private void fetchData() {

        if (changesMade) {
            skip = 0;
            unitsAdapter.reset(true);
            changesMade = false;
        }
        fetchUnits();
    }

    public void fetchUnits() {
        mDataManager.getPendingUnits(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(),
                userName.get(),
                take, skip, new OnResponseCallback<List<Unit>>() {
                    @Override
                    public void onSuccess(List<Unit> data) {
                        mActivity.hideLoading();
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        populateUnits(data);
                        unitsAdapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        unitsAdapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });
    }

    public void getUserUnitResponse() {
//        String role;
//        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
//            role= "student";
//        }else {
//            role = "staff";
//        }

        mDataManager.setSpecificSession("student",
                userName.get(), "mx_humana_lms/api/" +
                        mDataManager.getLoginPrefs().getProgramId()+"/masquerade","",
                new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse response) {
                        if (response.getSuccess()){
//                            Bundle bundle = new Bundle();
//                            bundle.putString("BlockId", blockid);
//                            ActivityUtil.gotoPage(getActivity(), PendingUnitWebviewActivity.class,bundle);
//                            enrollCourse(item);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.showShortSnack("course enroll error");
                    }
                });
    }

    public void approveUnits(String unitId, String remarks, int rating) {

        mDataManager.approveUnit(unitId,
                userName.get(), remarks, rating, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        mActivity.hideLoading();
                        changesMade = true;
                        fetchData();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        unitsAdapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });
    }

    public void rejectUnits(String unitId, String remarks, int rating) {
        mDataManager.rejectUnit(unitId,
                userName.get(), remarks, rating, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        mActivity.hideLoading();
                        changesMade = true;
                        fetchData();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        unitsAdapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });
    }

    private void populateUnits(List<Unit> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (Unit unit : data) {
            if (!unitsList.contains(unit)) {
                unitsList.add(unit);
                newItemsAdded = true;
                n++;
            }
        }


        if (newItemsAdded) {
            unitsAdapter.notifyItemRangeInserted(unitsList.size() - n, n);
        }

        toggleEmptyVisibility();
    }

    private void getBlockComponent(Unit unit) {

        mDataManager.enrolInCourse(mDataManager.getLoginPrefs().getProgramId(),
                new OnResponseCallback<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        mDataManager.getBlockComponent(unit.getUnit_id(), mDataManager.getLoginPrefs().getProgramId(),
                                new OnResponseCallback<CourseComponent>() {
                                    @Override
                                    public void onSuccess(CourseComponent data) {
                                        mActivity.hideLoading();
                                        Constants.UNIT_ID = unit.getUnit_id();
                                        Constants.USERNAME = userName.get();

//                                        blockid = data.getBlockId();
//                                        getUserUnitResponse();
                                        if (PendingUnitsListViewModel.this.course == null) {
                                            mActivity.showLongSnack("You're not enrolled in the program");
                                            return;
                                        }

                                        if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {
                                            mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                                    mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                                    PendingUnitsListViewModel.this.course, data.getChildren().get(0).getId(),
                                                    null, false);
                                        } else {
                                            mActivity.showLongSnack("This unit is empty");
                                        }


//                                        getUserUnitResponse();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        mActivity.hideLoading();
                                        mActivity.showLongSnack(e.getLocalizedMessage());
                                    }
                                });
                    }


                    @Override
                    public void onFailure(Exception e) {
                        mActivity.showLongSnack("error during unit enroll");
                    }
                });

    }

    private void toggleEmptyVisibility() {
        if (unitsList == null || unitsList.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }
    @SuppressWarnings("unused")
    public void onEventMainThread(PeriodSavedEvent event) {
        changesMade = true;
        allLoaded = false;
        fetchData();
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(List<Unit> unit) {
//        filters.clear();
        changesMade = true;
        allLoaded = false;
        fetchData();
//        ProgramFilter pf = new ProgramFilter();
//        pf.setDisplayName(user.username);
//        pf.setInternalName(user.name);
//        pf.setId(user.name);
//        pf.setOrder(user.completedHours);
//        pf.setShowIn(new ArrayList<String>());
//        pf.setTags(tags);
//        allFilters.add(pf);
//        filtersAdapter.notifyItemChanged(3, 4);


    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CourseEnrolledEvent event) {
        this.course = event.getCourse();
    }

    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    public class UnitsAdapter extends MxInfiniteAdapter<Unit> {

        public UnitsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Unit model,
                           @Nullable OnRecyclerItemClickListener<Unit> listener) {
            if (binding instanceof TRowPendingUnitsBinding) {
                TRowPendingUnitsBinding itemBinding = (TRowPendingUnitsBinding) binding;
                itemBinding.textUnitName.setText(model.getCode() + "  |  " + model.getType() + " | "
                        + model.getUnitHour() + " "+mActivity.getResources().getString(R.string.point_txt));
                itemBinding.textPeriodName.setText(model.getTitle());
                itemBinding.textSubmissionDate.setVisibility(View.GONE);
                if (!DateUtil.getDisplayDate(model.getMyDate()).equals("01 Jan 1970")) {
                    itemBinding.textDate.setText("Submitted On : " + DateUtil.getDisplayDate(model.getMyDate()));
                }if(!DateUtil.getDisplayDate(model.getStaffDate()).equals("01 Jan 1970")) {
                    itemBinding.textProposedDate.setText(DateUtil.getDisplayDate(model.getStaffDate()));
                }
                itemBinding.textDesc.setText(model.getDesc());
                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())){
                    itemBinding.llApproval.setVisibility(View.GONE);
                }else {
                    itemBinding.llApproval.setVisibility(View.VISIBLE);
                }
                itemBinding.btnApprove.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
//                        approveReturn(model.getUnit_id());
                });

                itemBinding.btnReject.setOnClickListener(v -> {
//                    approveReturn(model.getUnit_id());
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onItemClick(v, model);
                        }
                    }
                });
//                itemBinding.llCard.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        ActivityUtil.gotoPage(mActivity, PendingUnitWebviewActivity.class);
//                    }
//                });



            }
        }
    }

    public void approveReturn(String unitId){
        final Dialog dialog = new Dialog(mActivity);
        dialog.setContentView(R.layout.dialog_approve_return_unit);
        Button btnApprove = (Button) dialog.findViewById(R.id.btn_approve);
        Button btnReturn = (Button) dialog.findViewById(R.id.btn_return);
        EditText etRemarks = dialog.findViewById(R.id.et_remarks);
        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
//        EditText dialogText =  dialog.findViewById(R.id.et_period_name);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);



        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar rb, float rating, boolean fromUser) {
                rating = rb.getRating();
                if (rating==1){
                    Toast.makeText(mActivity, "poor", Toast.LENGTH_SHORT).show();
                }else if (rating==2){
                    Toast.makeText(mActivity, "fair", Toast.LENGTH_SHORT).show();
                }else if (rating==3){
                    Toast.makeText(mActivity, "good", Toast.LENGTH_SHORT).show();
                }else if (rating==4){
                    Toast.makeText(mActivity, "very good", Toast.LENGTH_SHORT).show();
                }else if (rating==5){
                    Toast.makeText(mActivity, "excellent", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // if button is clicked, close the custom dialog

        btnApprove.setOnClickListener(v -> {
            String remarks = etRemarks.getText().toString();
            dialog.dismiss();
            approveUnits(unitId, remarks, (int) rating);
        });

        btnReturn.setOnClickListener(v -> {
            String remarks = etRemarks.getText().toString();
            dialog.dismiss();
            rejectUnits(unitId,remarks, (int) rating);
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        changesMade = true;
        fetchData();
        Constants.UNIT_ID = "";
        Constants.USERNAME = "";
//        if (!Constants.UNIT_ID.equals("") || Constants.UNIT_ID !=null){
//            fetchData();
//        }
    }
}
