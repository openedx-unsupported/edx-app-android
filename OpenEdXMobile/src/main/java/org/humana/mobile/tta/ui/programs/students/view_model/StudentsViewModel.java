package org.humana.mobile.tta.ui.programs.students.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
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
            mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, item.username);

        });
        gridUsersAdapter.setItemClickListener((view, item) -> {
            mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, item.username);

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

    private void setFilters() {
        filters = new ArrayList<>();

        filters.clear();

        if (tags.isEmpty() || allFilters == null || allFilters.isEmpty()) {
            return;
        }

        for (ProgramFilter filter : allFilters) {

            List<ProgramFilterTag> selectedTags = new ArrayList<>();
            for (ProgramFilterTag tag : filter.getTags()) {
                if (tags.contains(tag)) {
                    selectedTags.add(tag);
                }
            }

            if (!selectedTags.isEmpty()) {
                ProgramFilter pf = new ProgramFilter();
                pf.setDisplayName(filter.getDisplayName());
                pf.setInternalName(filter.getInternalName());
                pf.setId(filter.getId());
                pf.setOrder(filter.getOrder());
                pf.setShowIn(filter.getShowIn());
                pf.setTags(selectedTags);
                filters.add(pf);
            }
        }
    }

    public void getFilters() {

        mDataManager.getProgramFilters(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(), ShowIn.schedule.name(),
                new OnResponseCallback<List<ProgramFilter>>() {
                    @Override
                    public void onSuccess(List<ProgramFilter> data) {
                        List<ProgramFilter> removables = new ArrayList<>();
                        for (ProgramFilter filter : data) {
                            if (filter.getShowIn() == null || filter.getShowIn().isEmpty() ||
                                    !filter.getShowIn().contains("students")) {
                                removables.add(filter);
                            }
                        }
                        for (ProgramFilter filter : removables) {
                            data.remove(filter);
                        }

                        if (!data.isEmpty()) {
                            allFilters = data;
                            filtersVisible.set(true);
                            filtersAdapter.setItems(data);
                        } else {
                            filtersVisible.set(false);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        filtersVisible.set(false);
                    }
                });

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
                    itemBinding.txtPending.setText(String.format("%s units", String.valueOf(model.pendingCount)));
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
                    }
//                boolean tabletSize = mActivity.getResources().getBoolean(R.bool.isTablet);
//                if (!tabletSize){
//                    itemBinding.txtCompleted.setCompoundDrawables(null,null,null,null);
//                    itemBinding.txtPending.setCompoundDrawables(null, null,null,null);
//                }

                    if (!mDataManager.getLoginPrefs().getRole().equals("Instructor")) {
                        itemBinding.llStatus.setVisibility(View.GONE);
                    }

                    itemBinding.txtCompleted.setOnClickListener(v -> {
                        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                            Bundle b = new Bundle();
                            b.putString(Router.EXTRA_USERNAME, model.username);
                            ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);

//                EventBus.getDefault().post(new ShowStudentUnitsEvent(item));
                        } else {
                            mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, model.username);
                        }
                    });
                    itemBinding.txtPending.setOnClickListener(v -> {
                        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                            Bundle b = new Bundle();
                            b.putString(Router.EXTRA_USERNAME, model.username);
                            ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);

//                EventBus.getDefault().post(new ShowStudentUnitsEvent(item));
                        } else {
                            mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, model.username);
                        }
                    });

                    itemBinding.getRoot().setOnClickListener(v -> {
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
                itemBinding.txtCompleted.setText(String.format("%s hrs", String.valueOf(model.completedHours)));
                itemBinding.txtPending.setText(String.format("%s units", String.valueOf(model.pendingCount)));
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
                }
//                boolean tabletSize = mActivity.getResources().getBoolean(R.bool.isTablet);
//                if (!tabletSize){
//                    itemBinding.txtCompleted.setCompoundDrawables(null,null,null,null);
//                    itemBinding.txtPending.setCompoundDrawables(null, null,null,null);
//                }

                if (!mDataManager.getLoginPrefs().getRole().equals("Instructor")) {
                    itemBinding.llStatus.setVisibility(View.GONE);
                }

                itemBinding.txtCompleted.setOnClickListener(v -> {
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        Bundle b = new Bundle();
                        b.putString(Router.EXTRA_USERNAME, model.username);
                        ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);

//                EventBus.getDefault().post(new ShowStudentUnitsEvent(item));
                    } else {
                        mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, model.username);
                    }
                });
                itemBinding.txtPending.setOnClickListener(v -> {
                    if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                        Bundle b = new Bundle();
                        b.putString(Router.EXTRA_USERNAME, model.username);
                        ActivityUtil.gotoPage(mActivity, UserStatusActivity.class, b);

//                EventBus.getDefault().post(new ShowStudentUnitsEvent(item));
                    } else {
                        mDataManager.getEdxEnvironment().getRouter().showUserProfile(mActivity, model.username);
                    }
                });

                itemBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }


        }
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
        if (position >= 0) {
            ProgramUser p = users.get(position);
            p.pendingCount = period.pendingCount + event.pendingCount;
            gridUsersAdapter.notifyItemChanged(position);
        }
    }
}
