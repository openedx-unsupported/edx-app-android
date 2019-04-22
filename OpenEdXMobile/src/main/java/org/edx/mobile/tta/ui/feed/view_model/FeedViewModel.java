package org.edx.mobile.tta.ui.feed.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowFeedLikeBinding;
import org.edx.mobile.databinding.TRowSuggestedTeacherBinding;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.local.db.table.Feed;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.feed.SuggestedUser;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.feed.NotificationsFragment;
import org.edx.mobile.tta.utils.ActivityUtil;

import java.util.List;

public class FeedViewModel extends BaseViewModel {

    public FeedAdapter feedAdapter;
    public SuggestedUsersAdapter suggestedUsersAdapter;

    public ObservableBoolean suggestedUsersVisible = new ObservableBoolean();

    public FeedViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        feedAdapter = new FeedAdapter(context);
        suggestedUsersAdapter = new SuggestedUsersAdapter(mActivity);
        suggestedUsersAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.follow_btn:
                    mActivity.showLoading();

                    mDataManager.followUnfollowUser(item.getUsername(), new OnResponseCallback<StatusResponse>() {
                        @Override
                        public void onSuccess(StatusResponse data) {
                            mActivity.hideLoading();
                            if (view instanceof Button){
                                Button button = (Button) view;
                                if (data.getStatus()){
                                    button.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.btn_selector_filled));
                                    button.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
                                    button.setText(mActivity.getString(R.string.unfollow));

                                    mActivity.analytic.addMxAnalytics_db(item.getUsername(), Action.FollowUser,
                                            Nav.feed.name(), Source.Mobile, item.getUsername());

                                } else {
                                    button.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.btn_selector_hollow));
                                    button.setTextColor(ContextCompat.getColor(mActivity, R.color.primary_cyan));
                                    button.setText(mActivity.getString(R.string.follow));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            mActivity.hideLoading();
                            mActivity.showLongSnack(e.getLocalizedMessage());
                        }
                    });
                    break;
            }
        });
        getFeeds();
    }

    private void getFeeds() {
        mActivity.showLoading();

        mDataManager.getSuggestedUsers(10, 0, new OnResponseCallback<List<SuggestedUser>>() {
            @Override
            public void onSuccess(List<SuggestedUser> data) {
                mActivity.hideLoading();
                suggestedUsersVisible.set(true);
                suggestedUsersAdapter.setItems(data);
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                suggestedUsersVisible.set(false);
            }
        });

    }

    public void showNotifications(){
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new NotificationsFragment(),
                R.id.dashboard_fragment,
                NotificationsFragment.TAG,
                true,
                null
        );
    }

    public class SuggestedUsersAdapter extends MxFiniteAdapter<SuggestedUser> {
        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
        public SuggestedUsersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull SuggestedUser model, @Nullable OnRecyclerItemClickListener<SuggestedUser> listener) {
            if (binding instanceof TRowSuggestedTeacherBinding){
                TRowSuggestedTeacherBinding teacherBinding = (TRowSuggestedTeacherBinding) binding;
                teacherBinding.userName.setText(model.getName());
                Glide.with(getContext())
                        .load(model.getProfileImage().getImageUrlLarge())
                        .placeholder(R.drawable.profile_photo_placeholder)
                        .into(teacherBinding.userImage);

                teacherBinding.followBtn.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }

    public class FeedAdapter extends MxInfiniteAdapter<Feed> {
        public FeedAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Feed model, @Nullable OnRecyclerItemClickListener<Feed> listener) {
            if (binding instanceof TRowFeedLikeBinding){
                TRowFeedLikeBinding likeBinding = (TRowFeedLikeBinding) binding;
//                likeBinding.feedTitle.setText();
            }
        }

        @Override
        public int getItemLayout(int position) {
            return R.layout.t_row_feed_like;
        }
    }
}
