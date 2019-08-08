package org.edx.mobile.tta.ui.programs.units.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowNotificationBinding;
import org.edx.mobile.tta.data.local.db.table.Notification;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.custom.DropDownFilterView;
import org.edx.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class UnitsViewModel extends BaseViewModel {

    public List<DropDownFilterView.FilterItem> typeFilters;
    public List<DropDownFilterView.FilterItem> periodFilters;

    public DropDownFilterView.OnFilterClickListener typeListener = (v, item, position, prev) -> {

    };

    public DropDownFilterView.OnFilterClickListener periodListener = (v, item, position, prev) -> {

    };

    public NotificationsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public UnitsViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        adapter = new NotificationsAdapter(mActivity);

        fetchUnits();
        setFilters();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    private void setFilters(){

        periodFilters = new ArrayList<>();
        periodFilters.add(new DropDownFilterView.FilterItem("Periods", null, true,
                R.color.gray_5, R.drawable.t_background_tag_hollow));
        periodFilters.add(new DropDownFilterView.FilterItem("Period 1", null, false,
                R.color.white, R.drawable.t_background_tag_filled));
        periodFilters.add(new DropDownFilterView.FilterItem("Period 2", null, false,
                R.color.white, R.drawable.t_background_tag_filled));

        typeFilters = new ArrayList<>();
        typeFilters.add(new DropDownFilterView.FilterItem("Types", null, true,
                R.color.gray_5, R.drawable.t_background_tag_hollow));
        typeFilters.add(new DropDownFilterView.FilterItem("Type 1", null, false,
                R.color.white, R.drawable.t_background_tag_filled));
        typeFilters.add(new DropDownFilterView.FilterItem("Type 2", null, false,
                R.color.white, R.drawable.t_background_tag_filled));

    }

    private void fetchUnits() {

        List<Notification> notifications = new ArrayList<>();
        for (int i = 0 ; i < 20; i++){
            Notification notification = new Notification();
            notification.setTitle("NeTT Unit - " + (i+1));
            notification.setDescription("NeTT_2019");
            notifications.add(notification);
        }
        adapter.setItems(notifications);

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
                notificationBinding.notificationDate.setText(model.getDescription());

                Glide.with(mActivity)
                        .load("http://www.humana-india.org/images/GSN_1618_GOLD.JPG")
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(notificationBinding.notificationIcon);

                notificationBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
