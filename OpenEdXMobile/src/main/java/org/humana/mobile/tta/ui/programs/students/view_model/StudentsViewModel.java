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
    public RecyclerView.LayoutManager linearLayoutManager;
    public List<ProgramUser> users;

    public List<ProgramFilter> allFilters;
    public List<ProgramFilter> filters;
    public List<ProgramFilterTag> tags;

    public FiltersAdapter filtersAdapter;
    public UsersAdapter usersAdapter;
    public GridUsersAdapter gridUsersAdapter;

    private static final int TAKE = 10;
    private static final int SKIP = 0;

    private boolean allLoaded;
    private boolean changesMade;
    private int take, skip;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableBoolean isTabView = new ObservableBoolean();


    public StudentsViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        usersAdapter = new UsersAdapter(mActivity);
        gridUsersAdapter = new GridUsersAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);
        users = new ArrayList<>();
        filters = new ArrayList<>();
        usersAdapter.setItems(users);
        gridUsersAdapter.setItems(users);
        usersAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()) {
                case R.id.ll_current_status:
                    mDataManager.getLoginPrefs().setCurrrentPeriod(item.current_period_id);
                    break;

                case R.id.ll_status:
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        Bundle b = new Bundle();
                        b.putString(Router.EXTRA_USERNAME, item.username);
                        ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);
                    } else {
                        mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, item.username);
                    }
                    break;

                default:
                    mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, item.username);
                    break;

            }

        });
        gridUsersAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()){
                case R.id.ll_current_status:
                    mDataManager.getLoginPrefs().setCurrrentPeriod(item.current_period_id);
                    mDataManager.getLoginPrefs().setCurrrentPeriodTitle(item.current_period_title);
                    break;

                case R.id.ll_status:
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        Bundle b = new Bundle();
                        b.putString(Router.EXTRA_USERNAME, item.username);
                        ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);
                    } else {
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

        mActivity.showLoading();
//        getFilters();
        fetchData();

    }

    @Override
    public void onResume() {
        super.onResume();
        boolean tabsize = getActivity().getResources().getBoolean(R.bool.isTablet);
        if (tabsize) {
            layoutManager = new GridLayoutManager(mActivity, 2);
            isTabView.set(true);
        } else {
            linearLayoutManager = new GridLayoutManager(mActivity, 1);
            isTabView.set(false);
        }

        changesMade = true;
//        fetchData();
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
                        usersAdapter.setLoadingDone();
                        gridUsersAdapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        usersAdapter.setLoadingDone();
                        gridUsersAdapter.setLoadingDone();
                        toggleEmptyVisibility();
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
            usersAdapter.reset(true);
            gridUsersAdapter.reset(true);
//            setFilters();
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
            usersAdapter.notifyItemRangeInserted(users.size() - n, n);
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

    public class UsersAdapter extends MxInfiniteAdapter<ProgramUser> {

        public UsersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramUser model,
                           @Nullable OnRecyclerItemClickListener<ProgramUser> listener) {


                if (binding instanceof TRowStudentTabViewBinding) {
                    TRowStudentTabViewBinding itemBinding = (TRowStudentTabViewBinding) binding;
                    itemBinding.txtCompleted.setText(String.format("%s hrs", String.valueOf(model.completedHours)));
                    itemBinding.txtPending.setText(String.format("%s units", String.valueOf(model.completedUnits)));
                    itemBinding.userName.setText(model.name);
                    if (model.profileImage != null) {
                        Glide.with(mActivity).load(
                                mDataManager.getEdxEnvironment().getConfig().getApiHostURL() +
                                        model.profileImage.getImageUrlFull())
                                .centerCrop()
                                .placeholder(R.drawable.profile)
                                .into(itemBinding.userImage);
                    }
                    if (model.education != null) {
                        itemBinding.textDegree.setText(model.education);
                    }else {
                        itemBinding.textDegree.setVisibility(View.GONE);
                    }

                    itemBinding.txtCurrentCompleted.setText(String.format("%s hrs", String.valueOf(model.current_hours)));
                    itemBinding.txtCurrentPending.setText(String.format("%s units", String.valueOf(model.currentUnits)));



//                boolean tabletSize = mActivity.getResources().getBoolean(R.bool.isTablet);
//                if (!tabletSize){
//                    itemBinding.txtCompleted.setCompoundDrawables(null,null,null,null);
//                    itemBinding.txtPending.setCompoundDrawables(null, null,null,null);
//                }

                    if (!mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        itemBinding.llStatus.setVisibility(View.GONE);
                    }

                    itemBinding.txtCompleted.setOnClickListener(v -> {
//                        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
//                            Bundle b = new Bundle();
//                            b.putString(Router.EXTRA_USERNAME, model.username);
//                            ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);
//
////                EventBus.getDefault().post(new ShowStudentUnitsEvent(item));
//                        } else {
//                            mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, model.username);
//                        }
                    });

                    itemBinding.imgInsta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (model.social_profile.size()>0) {
                                for (int i=0; i<model.social_profile.size(); i++) {
                                    if (model.social_profile.get(i).platform.equals("linkedin")) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(model.social_profile.get(i).social_link));
                                        mActivity.startActivity(browserIntent);
                                    }
                                }
                            }

                        }
                    });
                    itemBinding.imgTwitter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (model.social_profile.size()>0) {
                                for (int i=0; i<model.social_profile.size(); i++) {
                                    if (model.social_profile.get(i).platform.equals("twitter")) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(model.social_profile.get(i).social_link));
                                        mActivity.startActivity(browserIntent);
                                    }
                                }
                            }

                        }
                    });
                    itemBinding.imgFacebook.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (model.social_profile.size()>0) {
                                for (int i=0; i<model.social_profile.size(); i++) {
                                    if (model.social_profile.get(i).platform.equals("facebook")) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(model.social_profile.get(i).social_link));
                                        mActivity.startActivity(browserIntent);
                                    }
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
//                    Glide.with(mActivity).load(
//                            mDataManager.getEdxEnvironment().getConfig().getApiHostURL() +
//                                    model.profileImage.getImageUrlFull())
//                            .placeholder(R.drawable.profile)
//                            .into(itemBinding.userImage);


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
//                boolean tabletSize = mActivity.getResources().getBoolean(R.bool.isTablet);
//                if (!tabletSize){
//                    itemBinding.txtCompleted.setCompoundDrawables(null,null,null,null);
//                    itemBinding.txtPending.setCompoundDrawables(null, null,null,null);
//                }

                if (!mDataManager.getLoginPrefs().getRole().equals("Instructor")) {
                    itemBinding.llStatus.setVisibility(View.GONE);
                }
                itemBinding.txtCurrentCompleted.setText(String.format("%s hrs", String.valueOf(model.current_hours)));
                itemBinding.txtCurrentPending.setText(String.format("%s units", String.valueOf(model.currentUnits)));


                itemBinding.txtCompleted.setOnClickListener(v -> {

                });


                itemBinding.imgInsta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (model.social_profile.size()>0) {
                            for (int i=0; i<model.social_profile.size(); i++) {
                                if (model.social_profile.get(i).platform.equals("linkedin")) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(model.social_profile.get(i).social_link));
                                    mActivity.startActivity(browserIntent);
                                }
                            }
                        }

                    }
                }); itemBinding.imgTwitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (model.social_profile.size()>0 && model.social_profile != null) {
                            for (int i=0; i<model.social_profile.size(); i++) {
                                if (model.social_profile.get(i).platform.equals("twitter")) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(model.social_profile.get(i).social_link));
                                    mActivity.startActivity(browserIntent);
                                }
                            }
                        }

                    }
                }); itemBinding.imgFacebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.social_profile.size()>0 && model.social_profile != null) {
                            for (int i=0; i<model.social_profile.size(); i++) {
                                if (model.social_profile.get(i).platform.equals("facebook")) {

                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(model.social_profile.get(i).social_link));
                                    mActivity.startActivity(browserIntent);
                                }
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

    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("org.humana.mobile", 0);
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    public class FiltersAdapter extends MxFiniteAdapter<ProgramFilter> {
        public FiltersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramFilter model,
                           @Nullable OnRecyclerItemClickListener<ProgramFilter> listener) {
            if (binding instanceof TRowFilterDropDownBinding) {
                TRowFilterDropDownBinding dropDownBinding = (TRowFilterDropDownBinding) binding;

                List<DropDownFilterView.FilterItem> items = new ArrayList<>();
                items.add(new DropDownFilterView.FilterItem(model.getDisplayName(), null,
                        true, R.color.primary_cyan, R.drawable.t_background_tag_hollow
                ));
                for (ProgramFilterTag tag : model.getTags()) {
                    items.add(new DropDownFilterView.FilterItem(tag.getDisplayName(), tag,
                            false, R.color.white, R.drawable.t_background_tag_filled
                    ));
                }
                dropDownBinding.filterDropDown.setFilterItems(items);

                dropDownBinding.filterDropDown.setOnFilterItemListener((v, item, position, prev) -> {
                    if (prev != null && prev.getItem() != null) {
                        tags.remove((ProgramFilterTag) prev.getItem());
                    }
                    if (item.getItem() != null) {
                        tags.add((ProgramFilterTag) item.getItem());
                    }

                    changesMade = true;
                    allLoaded = false;
                    mActivity.showLoading();
                    getUsers();
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

        int position = usersAdapter.getItemPosition(period);
        if (position >= 0) {
            ProgramUser p = users.get(position);
            p.pendingCount = period.pendingCount + event.pendingCount;
            usersAdapter.notifyItemChanged(position);
        }
        int position2 = gridUsersAdapter.getItemPosition(period);
        if (position2 >= 0) {
            ProgramUser p = users.get(position2);
            p.pendingCount = period.pendingCount + event.pendingCount;
            gridUsersAdapter.notifyItemChanged(position2);
        }
    }
}
