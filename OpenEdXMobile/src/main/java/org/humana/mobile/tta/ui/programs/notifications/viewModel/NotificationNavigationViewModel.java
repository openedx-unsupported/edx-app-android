package org.humana.mobile.tta.ui.programs.notifications.viewModel;

import com.lib.mxcalendar.models.Event;

import org.humana.mobile.R;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.Notification;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.EventNotificationCount;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitsViewModel;

import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class NotificationNavigationViewModel extends BaseViewModel {

    private String courseId, unitId;
    EnrolledCoursesResponse coursesResponse;
    private String notificationId, actionUserName;

    public NotificationNavigationViewModel(BaseVMActivity activity, String courseId, String unitId,
                                           String notificationId, String actionUserName) {
        super(activity);

        this.courseId = courseId;
        this.unitId = unitId;
        this.notificationId = notificationId;
//        this.actionUserName = actionUserName;

        getEnrolledCourse();
        if (mDataManager.getLoginPrefs().getNotificationSeen().equals(Constants.NOTIFICATION_RECIEVED)) {
            mDataManager.getLoginPrefs().setNotificationSeen(Constants.NOTIFICATION_SEEN);
        }
        notifyNotificationRead(String.valueOf(notificationId));
    }

    private void getEnrolledCourse() {

//        Constants.USERNAME = actionUserName;
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

                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
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

                                            mActivity.finish();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {

                                        e.printStackTrace();
                                    }
                                });
                    }


                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
    }
    private void notifyNotificationRead(String id){
        mActivity.showLoading();
        mDataManager.readNotification(id, new OnResponseCallback<SuccessResponse>() {
            @Override
            public void onSuccess(SuccessResponse response) {
                mActivity.hideLoading();
                EventBus.getDefault().post(new EventNotificationCount(true));            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                mActivity.showShortToast(mActivity.getString(R.string.notify_read_request));
                mActivity.hideLoading();
            }
        });
    }
}
