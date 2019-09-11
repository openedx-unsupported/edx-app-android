package org.edx.mobile.tta.ui.programs.pendingUnits.viewModel;

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

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowFilterDropDownBinding;
import org.edx.mobile.databinding.TRowPendingUserGridBinding;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.model.program.ProgramFilterTag;
import org.edx.mobile.tta.data.model.program.ProgramUser;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.custom.DropDownFilterView;
import org.edx.mobile.tta.ui.programs.pendingUnits.PendingUnitsListActivity;
import org.edx.mobile.tta.utils.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PendingUsersViewModel extends BaseViewModel {

    public List<ProgramFilter> filterList;
    public List<ProgramUser> programUserList;
    public FiltersAdapter filtersAdapter;
    public UsersAdapter usersAdapter;

    public List<ProgramFilter> allFilters;
    public List<ProgramFilter> filters;
    public List<ProgramFilterTag> tags;


    private static final int TAKE = 0;
    private static final int SKIP = 10;

    private boolean allLoaded;
    private boolean changesMade;
    private int take, skip;

    public ObservableBoolean filtersVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();

    public RecyclerView.LayoutManager filterLayoutManager;
    public RecyclerView.LayoutManager layoutManager;

    public PendingUsersViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        usersAdapter = new UsersAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);
        programUserList = new ArrayList<>();
        usersAdapter.setItems(programUserList);

        filterLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        layoutManager = new GridLayoutManager(mActivity, 2);
        take = TAKE;

        skip = SKIP;

        mActivity.showLoading();
        getFilters();
        fetchData();


        usersAdapter.setItemClickListener((view, item) -> {

            if (item != null){
                Bundle b = new Bundle();
                b.putString("username", item.username);
                ActivityUtil.gotoPage(mActivity, PendingUnitsListActivity.class, b);
            }

        });
    }


    @Override
    public void onResume() {
        super.onResume();

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
            setFilters();
        }
    fetchUsers();

    }

    private void setFilters() {
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

        mDataManager.getProgramFilters(new OnResponseCallback<List<ProgramFilter>>() {
            @Override
            public void onSuccess(List<ProgramFilter> data) {
                List<ProgramFilter> removables = new ArrayList<>();
                for (ProgramFilter filter : data) {
                    if (filter.getShowIn() == null || filter.getShowIn().isEmpty() ||
                            !filter.getShowIn().contains("Students")) {
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


    public void fetchUsers() {
        mDataManager.getPendingUsers(mDataManager.getLoginPrefs().getProgramId(),
                mDataManager.getLoginPrefs().getSectionId(),
                take, skip, new OnResponseCallback<List<ProgramUser>>() {
                    @Override
                    public void onSuccess(List<ProgramUser> data) {
                        mActivity.hideLoading();
                        if (data.size() < take) {
                            allLoaded = true;
                        }
                        populateUsers(data);
                        usersAdapter.setLoadingDone();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        allLoaded = true;
                        usersAdapter.setLoadingDone();
                        toggleEmptyVisibility();
                    }
                });
    }


    private void populateUsers(List<ProgramUser> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (ProgramUser user : data) {
            if (!programUserList.contains(user)) {
                programUserList.add(user);
                newItemsAdded = true;
                n++;
            }
        }


        if (newItemsAdded) {
            usersAdapter.notifyItemRangeInserted(programUserList.size() - n, n);
        }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (programUserList == null || programUserList.isEmpty()) {
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
            if (binding instanceof TRowPendingUserGridBinding) {
                TRowPendingUserGridBinding itemBinding = (TRowPendingUserGridBinding) binding;
                itemBinding.userName.setText(model.username);
                itemBinding.textCount.setText(String.format("Pending : %d", model.pendingCount));

                if (model.profileImage != null){
                    Glide.with(mActivity).load(mDataManager.getEdxEnvironment().getConfig().getApiHostURL()+
                            model.profileImage.getImageUrlSmall())
                            .centerCrop()
                            .placeholder(R.drawable.profile_photo_placeholder)
                            .into(itemBinding.userImage);
                }

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
                    fetchUsers();
                });
            }
        }
    }
}
