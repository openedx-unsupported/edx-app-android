package org.edx.mobile.tta.ui.feed.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowSuggestedTeacherGridBinding;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.feed.SuggestedUser;
import org.edx.mobile.tta.event.UserFollowingChangedEvent;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.profile.OtherProfileActivity;
import org.edx.mobile.tta.utils.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class RecommendedUsersViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public SuggestedUsersAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    private List<SuggestedUser> users;
    private int take, skip;
    private boolean allLoaded;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        getSuggestedUsers();
        return true;
    };

    public RecommendedUsersViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        users = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;

        adapter = new SuggestedUsersAdapter(mActivity);
        adapter.setItems(users);
        adapter.setItemClickListener((view, item) -> {
            switch (view.getId()) {
                case R.id.follow_btn:
                    mActivity.showLoading();

                    mDataManager.followUnfollowUser(item.getUsername(), new OnResponseCallback<StatusResponse>() {
                        @Override
                        public void onSuccess(StatusResponse data) {
                            mActivity.hideLoading();
                            item.setFollowed(data.getStatus());
                            adapter.notifyItemChanged(adapter.getItemPosition(item));
                            EventBus.getDefault().post(new UserFollowingChangedEvent(item));

                            if (data.getStatus()){
                                mActivity.analytic.addMxAnalytics_db(item.getUsername(), Action.FollowUser,
                                        Nav.feed.name(), Source.Mobile, item.getUsername());
                            } else {
                                mActivity.analytic.addMxAnalytics_db(item.getUsername(), Action.UnfollowUser,
                                        Nav.feed.name(), Source.Mobile, item.getUsername());
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            mActivity.hideLoading();
                            mActivity.showLongSnack(e.getLocalizedMessage());
                        }
                    });
                    break;

                default:
                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_USERNAME, item.getUsername());
                    ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameters);
            }
        });

        mActivity.showLoading();
        getSuggestedUsers();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new GridLayoutManager(mActivity, 2);
    }

    private void getSuggestedUsers() {

        mDataManager.getSuggestedUsers(take, skip, new OnResponseCallback<List<SuggestedUser>>() {
            @Override
            public void onSuccess(List<SuggestedUser> data) {
                mActivity.hideLoading();
                if (data.size() < take) {
                    allLoaded = true;
                }
                populateUsers(data);
                adapter.setLoadingDone();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                allLoaded = true;
                adapter.setLoadingDone();
            }
        });

    }

    private void populateUsers(List<SuggestedUser> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (SuggestedUser user : data) {
            if (!users.contains(user)) {
                users.add(user);
                newItemsAdded = true;
                n++;
            }
        }
        if (newItemsAdded) {
            adapter.notifyItemRangeInserted(users.size() - n, n);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(UserFollowingChangedEvent event){
        if (users.contains(event.getUser())){
            int position = users.indexOf(event.getUser());
            users.get(position).setFollowed(event.getUser().isFollowed());
            adapter.notifyItemChanged(position);
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }

    public class SuggestedUsersAdapter extends MxInfiniteAdapter<SuggestedUser> {

        public SuggestedUsersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull SuggestedUser model, @Nullable OnRecyclerItemClickListener<SuggestedUser> listener) {
            if (binding instanceof TRowSuggestedTeacherGridBinding) {
                TRowSuggestedTeacherGridBinding teacherBinding = (TRowSuggestedTeacherGridBinding) binding;
                teacherBinding.setViewModel(model);
                Glide.with(getContext())
                        .load(model.getProfileImage().getImageUrlLarge())
                        .placeholder(R.drawable.profile_photo_placeholder)
                        .into(teacherBinding.userImage);

                if (model.isFollowed()) {
                    teacherBinding.followBtn.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.btn_selector_filled));
                    teacherBinding.followBtn.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
                    teacherBinding.followBtn.setText(mActivity.getString(R.string.following));
                } else {
                    teacherBinding.followBtn.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.btn_selector_hollow));
                    teacherBinding.followBtn.setTextColor(ContextCompat.getColor(mActivity, R.color.primary_cyan));
                    teacherBinding.followBtn.setText(mActivity.getString(R.string.follow));
                }

                teacherBinding.followBtn.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                teacherBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
