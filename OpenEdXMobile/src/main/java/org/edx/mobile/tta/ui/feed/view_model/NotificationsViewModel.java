package org.edx.mobile.tta.ui.feed.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowNotificationBinding;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.enums.NotificationType;
import org.edx.mobile.tta.data.enums.SourceType;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.Notification;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.edx.mobile.tta.ui.course.CourseDashboardActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    private List<Notification> notifications;

    public NotificationsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean emptyVisible = new ObservableBoolean();

    private int take, skip;
    private boolean allLoaded;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        skip++;
        fetchNotifications();
        return true;
    };

    public NotificationsViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        notifications = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;

        adapter = new NotificationsAdapter(mActivity);
        adapter.setItems(notifications);
        adapter.setItemClickListener((view, item) -> {

            if (!item.isSeen()){
                item.setSeen(true);
                mDataManager.updateNotificationsInLocal(Collections.singletonList(item));
                adapter.notifyItemChanged(adapter.getItemPosition(item));
            }

            try {
                switch (NotificationType.valueOf(item.getType())){
                    case content:
                        mActivity.showLoading();
                        mDataManager.getContent(Long.parseLong(item.getRef_id()), new OnResponseCallback<Content>() {
                            @Override
                            public void onSuccess(Content data) {
                                mActivity.hideLoading();
                                showContentDashboard(data);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                mActivity.hideLoading();
                                mActivity.showLongSnack(e.getLocalizedMessage());
                            }
                        });

                        break;
                    case system:
                        mActivity.showLongSnack(item.getDescription());
                        break;
                    case profile:
                        mActivity.showLongSnack(item.getDescription());
                        break;
                    default:
                        mActivity.showLongSnack(item.getDescription());
                }
            } catch (IllegalArgumentException e) {
                mActivity.showLongSnack(item.getDescription());
            }

        });

        mActivity.showLoading();
        fetchNotifications();
    }

    private void fetchNotifications() {

        mDataManager.getNotifications(take, skip, new OnResponseCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> data) {
                mActivity.hideLoading();
                if (data.size() < take){
                    allLoaded = true;
                }
                populateNotifications(data);
                adapter.setLoadingDone();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                allLoaded = true;
                adapter.setLoadingDone();
                toggleEmptyVisibility();
            }
        });

    }

    private void populateNotifications(List<Notification> data) {
        boolean newItemsAdded = false;

        for (Notification notification: data){
            if (!notifications.contains(notification)){
                notifications.add(notification);
                newItemsAdded = true;
            }
        }

        if (newItemsAdded) {
            adapter.notifyDataSetChanged();
        }
        toggleEmptyVisibility();
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
        layoutManager = new LinearLayoutManager(mActivity);
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
                String time = DateUtil.getDayMonth(model.getCreated_time()) + " at " +
                        DateUtil.getHourMinute12(model.getCreated_time());
                notificationBinding.notificationDate.setText(time);

                if (model.isSeen()){
                    notificationBinding.notificationTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_3));
                    notificationBinding.notificationDate.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_3));
                } else {
                    notificationBinding.notificationTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_5));
                    notificationBinding.notificationDate.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_5));
                }

                notificationBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
