package org.humana.mobile.tta.ui.programs.notifications.viewModel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowNotificationBinding;
import org.humana.mobile.event.NetworkConnectivityChangeEvent;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.Notification;
import org.humana.mobile.tta.data.NotificationResponse;
import org.humana.mobile.tta.data.enums.SourceType;
import org.humana.mobile.tta.data.local.db.table.Content;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.EventNotificationCount;
import org.humana.mobile.tta.event.ContentStatusReceivedEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.humana.mobile.tta.ui.course.CourseDashboardActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.DateUtil;
import org.humana.mobile.util.NetworkUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class NotificationViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    private List<Notification> notifications;

    public NotificationsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableBoolean offlineVisible = new ObservableBoolean();

    private int take, skip;
    private boolean allLoaded;

    private String courseId, unitId;
    EnrolledCoursesResponse coursesResponse;
    SimpleDateFormat dateFormat;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        skip++;
        if (!offlineVisible.get()) {
            fetchNotifications();
        }
        return true;
    };


    public NotificationViewModel(BaseVMActivity activity) {
        super(activity);
        notifications = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        layoutManager = new LinearLayoutManager(mActivity);

        adapter = new NotificationsAdapter(mActivity);
        adapter.setItems(notifications);


        offlineVisible.set(false);
        registerEventBus();

        dateFormat = new SimpleDateFormat("dd mmm, yyyy", Locale.ENGLISH);

        adapter.setItemClickListener((view, item) -> {

            if (item.getActionParentId() != null){
                mActivity.showLoading();
                courseId = item.getActionParentId();
                unitId = item.getActionId();
                getEnrolledCourse(item);
            }
        });

        mActivity.showLoading();
        fetchNotifications();
    }

    private void fetchNotifications() {

        mDataManager.getNotifications(take, skip, mDataManager.getLoginPrefs().getProgramId(),
                new OnResponseCallback<NotificationResponse>() {
                    @Override
                    public void onSuccess(NotificationResponse data) {
                        mActivity.hideLoading();
                        if (data.getNotifications().size() < take){
                            allLoaded = true;
                        }
                        populateNotifications(data.getNotifications());
                        adapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        mActivity.hideLoading();
                        allLoaded = true;
                        adapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });

    }

    private void populateNotifications(List<Notification> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (Notification notification: data){
            if (!unitAlreadyAdded(notification)) {
                notifications.add(notification);
                newItemsAdded = true;
                n++;
            }
        }

        if (newItemsAdded) {
            adapter.notifyItemRangeInserted(notifications.size() - n, n);
        }
        toggleEmptyVisibility();
    }

    private boolean unitAlreadyAdded(Notification notification) {
        for (Notification u : notifications) {
            if (u.getId().equals(notification.getId())) {
                return true;
            }
        }
        return false;
    }

    private void toggleEmptyVisibility(){
        if (notifications == null || notifications.isEmpty()){
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public void showContentDashboard(Content selectedContent){

        Bundle parameters = new Bundle();
        parameters.putParcelable(Constants.KEY_CONTENT, selectedContent);
        if (selectedContent.getSource().getType().equalsIgnoreCase(SourceType.course.name()) ||
                selectedContent.getSource().getType().equalsIgnoreCase(SourceType.edx.name())) {
            ActivityUtil.gotoPage(mActivity, CourseDashboardActivity.class, parameters);
        } else {
            ActivityUtil.gotoPage(mActivity, ConnectDashboardActivity.class, parameters);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
//        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    public class NotificationsAdapter extends MxInfiniteAdapter<Notification> {
        public NotificationsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Notification model, @Nullable OnRecyclerItemClickListener<Notification> listener) {
            if (binding instanceof TRowNotificationBinding){
                TRowNotificationBinding notificationBinding = (TRowNotificationBinding) binding;
                notificationBinding.setViewModel(model);
                if (model.getScheduleDate() > 0){
                    notificationBinding.notificationDate.setText(DateUtil
                            .getDisplayDateTime(model.getScheduleDate()));
                }

                if (model.isSeen()!=null) {
                    if (!model.isSeen()) {
                        notificationBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(
                                mActivity, R.color.humana_current_period));
                    }else {
                        notificationBinding.cvUnit.setCardBackgroundColor(ContextCompat.getColor(
                                mActivity, R.color.humana_card_background));
                    }
                }

                notificationBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (NetworkUtil.isConnected(mActivity)) {
            offlineVisible.set(false);
            fetchNotifications();
        } else {
            mActivity.hideLoading();
            offlineVisible.set(true);
        }
    }
    @SuppressWarnings("unused")
    public void onEventMainThread(EventNotificationCount event) {
       if (event.isCountChanged()){
           allLoaded = false;
           fetchNotifications();
       }
    }

    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    private void getEnrolledCourse(Notification notification) {

        mDataManager.enrolInCourse(courseId, new OnResponseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody responseBody) {
                mDataManager.getenrolledCourseByOrg("Humana", new OnResponseCallback<List<EnrolledCoursesResponse>>() {
                    @Override
                    public void onSuccess(List<EnrolledCoursesResponse> data) {

                        if (courseId != null) {
                            for (EnrolledCoursesResponse item : data) {
                                if (item.getCourse().getId().equals(courseId)) {
                                    coursesResponse = item;
                                    break;
                                }
                            }
                            enrollCourse(coursesResponse, courseId, unitId);
                            notifyNotificationRead(String.valueOf(notification.getId()), notification);

                            mActivity.hideLoading();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        e.printStackTrace();
                    }
                });

            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                mActivity.hideLoading();
                mActivity.showLongSnack("enroll failure");
            }
        });

    }

    private void enrollCourse(EnrolledCoursesResponse item, String action_parent_id, String action_id) {
        mDataManager.enrolInCourse(action_parent_id,
                new OnResponseCallback<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        mDataManager.getBlockComponent(action_id, action_parent_id,
                                new OnResponseCallback<CourseComponent>() {
                                    @Override
                                    public void onSuccess(CourseComponent data) {
                                        if (data.isContainer() && data.getChildren() != null && !data.getChildren().isEmpty()) {

                                            mDataManager.getEdxEnvironment().getRouter().showCourseContainerOutline(
                                                    mActivity, Constants.REQUEST_SHOW_COURSE_UNIT_DETAIL,
                                                    coursesResponse, data.getChildren().get(0).getId(),
                                                    null, false);

                                            mActivity.hideLoading();

                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {

                                        e.printStackTrace();
                                        mActivity.hideLoading();
                                    }
                                });
                    }


                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        mActivity.hideLoading();
                    }
                });
    }
    
    private void notifyNotificationRead(String id, Notification item){
        mActivity.showLoading();
        mDataManager.readNotification(mDataManager.getLoginPrefs().getProgramId(),
                id, new OnResponseCallback<SuccessResponse>() {
            @Override
            public void onSuccess(SuccessResponse response) {
                item.setSeen(true);
                adapter.notifyItemChanged(adapter.getItemPosition(item));
                mActivity.hideLoading();
                EventBus.getDefault().post(new EventNotificationCount(true));
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                mActivity.showShortToast(mActivity.getString(R.string.notify_read_request));
                mActivity.hideLoading();
            }
        });
    }
}
