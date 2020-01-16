package org.humana.mobile.tta.ui.programs.students.view_model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowFilterDropDownBinding;
import org.humana.mobile.databinding.TRowStudentTabViewBinding;
import org.humana.mobile.databinding.TRowStudentsGridBinding;
import org.humana.mobile.tta.data.enums.ShowIn;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.data.model.program.ProgramUser;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
import org.humana.mobile.tta.ui.programs.userStatus.UserStatusActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.view.Router;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class StudentsViewModel extends BaseViewModel {

    public RecyclerView.LayoutManager layoutManager;
    public List<ProgramUser> users;

    public List<ProgramFilter> allFilters;
    public List<ProgramFilter> filters;
    public List<ProgramFilterTag> tags;

    public GridUsersAdapter gridUsersAdapter;

    private static final int TAKE = 10;
    private static final int SKIP = 0;

    private boolean allLoaded;
    private boolean changesMade;
    private int take, skip;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableBoolean isTabView = new ObservableBoolean();


    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        fetchData();
        return true;
    };

    public StudentsViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        gridUsersAdapter = new GridUsersAdapter(mActivity);
        users = new ArrayList<>();
        filters = new ArrayList<>();
        gridUsersAdapter.setItems(users);
        gridUsersAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.ll_current_status:
                    mDataManager.getLoginPrefs().setCurrrentPeriod(item.current_period_id);
                    mDataManager.getLoginPrefs().setCurrrentPeriodTitle(item.current_period_title);
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        Bundle b = new Bundle();
                        b.putString(Router.EXTRA_USERNAME, item.username);
                        ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);
                    } else if(mDataManager.getLoginPrefs().getUsername().equals(item.username)) {
                        Bundle b = new Bundle();
                        b.putString(Router.EXTRA_USERNAME, item.username);
                        ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);
                    } else {
                        mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity,
                                item.username);
                    }
                    break;

                case R.id.ll_status:
                    mDataManager.getLoginPrefs().setCurrrentPeriod(0L);
                    mDataManager.getLoginPrefs().setCurrrentPeriodTitle("Period");
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        Bundle b = new Bundle();
                        b.putString(Router.EXTRA_USERNAME, item.username);
                        ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);
                    }else if(mDataManager.getLoginPrefs().getUsername().equals(item.username)) {
                        Bundle b = new Bundle();
                        b.putString(Router.EXTRA_USERNAME, item.username);
                        ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);
                    }
                    else {
                        mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, item.username);
                    }
                    break;
                    default:
                        mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, item.username);
                        break;
            }

        });

        take = TAKE;
        skip = SKIP;

        boolean tabsize = getActivity().getResources().getBoolean(R.bool.isTablet);
        if (tabsize) {
            layoutManager = new GridLayoutManager(mActivity, 2);
            isTabView.set(true);
        } else {
            layoutManager = new GridLayoutManager(mActivity, 1);
            isTabView.set(false);
        }

        changesMade = true;
        mActivity.showLoading();
        fetchData();

    }

    @Override
    public void onResume() {
        super.onResume();

    }


    public void getUsers() {
        mDataManager.getUsers(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(),
                take, skip, new OnResponseCallback<List<ProgramUser>>() {
                    @Override
                    public void onSuccess(List<ProgramUser> data) {
                        mActivity.hideLoading();
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        populateStudents(data);
                        gridUsersAdapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        gridUsersAdapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });

    }

    private void fetchData() {

        if (changesMade) {
            skip = 0;
            changesMade = false;
            gridUsersAdapter.reset(true);
        }

        getUsers();

    }


    private void populateStudents(List<ProgramUser> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (ProgramUser user : data) {
            if (!users.contains(user)) {
                users.add(user);
                newItemsAdded = true;
                n++;
            }
        }

        if (newItemsAdded) {
            gridUsersAdapter.notifyItemRangeInserted(users.size() - n, n);
        }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (users == null || users.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }


    public class GridUsersAdapter extends MxInfiniteAdapter<ProgramUser> {

        public GridUsersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramUser model,
                           @Nullable OnRecyclerItemClickListener<ProgramUser> listener) {


            if (binding instanceof TRowStudentsGridBinding) {
                TRowStudentsGridBinding itemBinding = (TRowStudentsGridBinding) binding;
                itemBinding.txtCompleted.setText(String.format("%s "+ mActivity.getResources().getString(R.string.point_txt), String.valueOf(model.completedHours)));
                itemBinding.txtPending.setText(String.format("%s units", String.valueOf(model.completedUnits)));
                itemBinding.userName.setText(model.name);
                if (model.profileImage != null) {



                    Glide.with(mActivity).load(
                            mDataManager.getEdxEnvironment().getConfig().getApiHostURL() +
                                    model.profileImage.getImageUrlFull()).asBitmap().centerCrop()
                            .placeholder(R.drawable.profile_photo_placeholder)
                            .centerCrop()
                            .into(new BitmapImageViewTarget(itemBinding.userImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mActivity.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            itemBinding.userImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }
                if (model.education != null) {
                    itemBinding.textDegree.setText(model.education);
                }else {
                    itemBinding.textDegree.setVisibility(View.GONE);
                }


//                if (!mDataManager.getLoginPrefs().getRole().equals("Instructor")) {
//                    itemBinding.llStatus.setVisibility(View.GONE);
//                }
                itemBinding.txtCurrentCompleted.setText(String.format("%s "+mActivity.getResources().getString(R.string.point_txt),
                        String.valueOf(model.current_hours)));
                itemBinding.txtCurrentPending.setText(String.format("%s units", String.valueOf(model.currentUnits)));


                itemBinding.imgInsta.setOnClickListener(v -> {

                    if (model.social_profile.size()>0) {
                        for (int i=0; i<model.social_profile.size(); i++) {
                            if (model.social_profile.get(i).platform.equals("linkedin")) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(model.social_profile.get(i).social_link));
                                mActivity.startActivity(browserIntent);
                            }
                        }
                    }

                });
                itemBinding.imgTwitter.setOnClickListener(v -> {
                    if (model.social_profile.size()>0 && model.social_profile != null) {
                        for (int i=0; i<model.social_profile.size(); i++) {
                            if (model.social_profile.get(i).platform.equals("twitter")) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(model.social_profile.get(i).social_link));
                                mActivity.startActivity(browserIntent);
                            }
                        }
                    }

                });
                itemBinding.imgFacebook.setOnClickListener(v -> {
                    if (model.social_profile.size()>0 && model.social_profile != null) {
                        for (int i=0; i<model.social_profile.size(); i++) {
                            if (model.social_profile.get(i).platform.equals("facebook")) {

                                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(model.social_profile.get(i).social_link));
                                mActivity.startActivity(browserIntent);
                            }
                        }
                    }

                });

                itemBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                itemBinding.llStatus.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
                itemBinding.userCard.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                itemBinding.llCurrentStatus.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }


        }
    }



    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ProgramUser event) {
        ProgramUser period = new ProgramUser();
        int position2 = gridUsersAdapter.getItemPosition(period);
        if (position2 >= 0) {
            ProgramUser p = users.get(position2);
            p.pendingCount = period.pendingCount + event.pendingCount;
            gridUsersAdapter.notifyItemChanged(position2);
        }
    }
}
